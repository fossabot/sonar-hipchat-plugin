/*
 * HipChat Plugin for SonarQube
 * Copyright (C) 2016-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonarqube.hipchat;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Locale;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.sonar.api.ce.posttask.PostProjectAnalysisTask;
import org.sonar.api.ce.posttask.QualityGate;
import org.sonar.api.i18n.I18n;
import org.sonar.api.platform.Server;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import static java.lang.String.format;

public class HipChatNotifier implements PostProjectAnalysisTask {

  private static final Logger LOGGER = Loggers.get(HipChatNotifier.class);
  private static final String ERROR_COLOR = "red";
  private static final String WARNING_COLOR = "yellow";

  private final I18n i18n;
  private final HipChatSettings settings;
  private final Server server;
  // TODO configure proxy
  private final OkHttpClient httpClient = new OkHttpClient.Builder().build();

  public HipChatNotifier(I18n i18n, HipChatSettings settings, Server server) {
    this.i18n = i18n;
    this.settings = settings;
    this.server = server;
  }

  @Override
  public void finished(ProjectAnalysis analysis) {
    QualityGate gate = analysis.getQualityGate();
    if (settings.isEnabled()) {
      if (gate != null && gate.getStatus() != QualityGate.Status.OK) {
        LOGGER.info("Send notification to HipChat");
        sendMessageOnFailedGate(analysis);
      } else {
        LOGGER.info("Quality gate is ok, no need to send notification to HipChat");
      }
    }
  }

  private void sendMessageOnFailedGate(ProjectAnalysis analysis) {
    QualityGate gate = analysis.getQualityGate();
    StringBuilder message = new StringBuilder();
    message
      .append("Quality Gate is ")
      .append(gate.getStatus())
      .append(" on project: <a href=\"")
      .append(server.getPublicRootUrl())
      .append("/overview?id=")
      .append(URLEncoder.encode(analysis.getProject().getKey()))
      .append("\">")
      // TODO escape HTML
      .append(analysis.getProject().getName())
      .append("</a><ul>");
    for (QualityGate.Condition condition : gate.getConditions()) {
      if (condition.getStatus() != QualityGate.EvaluationStatus.NO_VALUE && condition.getStatus() != QualityGate.EvaluationStatus.OK) {
        String l10nKey = format("metric.%s.name", condition.getMetricKey());
        // TODO use Metric.getName or default key if bundle message not defined
        String metricName = i18n.message(Locale.ENGLISH, l10nKey, condition.getMetricKey());
        message.append("<li>").append(metricName);
        if (condition.isOnLeakPeriod()) {
          message.append(" on leak period");
        }
        // TODO display threshold
        message.append(" is ").append(condition.getStatus()).append(": ").append(condition.getValue()).append("</li>");
      }
    }
    message.append("</ul>");

    RequestBody formBody = new FormBody.Builder()
      .add("color", statusToColor(gate.getStatus()))
      .add("message", message.toString())
      .add("message_format", "html")
      .build();

    Request request = new Request.Builder()
      // TODO support hipchat server
      .url(format("https://api.hipchat.com/v2/room/%s/notification", settings.getRoom()))
      .addHeader("Authorization", "Bearer " + settings.getAccessToken())
      .post(formBody)
      .build();
    try {
      Response response = httpClient.newCall(request).execute();
      if (!response.isSuccessful()) {
        LOGGER.error("Can not send HipChat notification. Got code " + response.code() + "[" + response.message() + "]");
      }

    } catch (IOException e) {
      LOGGER.error("Can not send HipChat notification", e);
    }
  }

  private static String statusToColor(QualityGate.Status status) {
    return status == QualityGate.Status.ERROR ? ERROR_COLOR : WARNING_COLOR;
  }
}
