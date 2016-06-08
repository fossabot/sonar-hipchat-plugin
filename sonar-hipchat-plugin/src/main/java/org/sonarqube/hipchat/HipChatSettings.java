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

import java.util.Arrays;
import java.util.List;
import javax.annotation.CheckForNull;
import org.sonar.api.PropertyType;
import org.sonar.api.ce.ComputeEngineSide;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Qualifiers;

import static java.lang.String.valueOf;

@ComputeEngineSide
public class HipChatSettings {

  public static final String HIPCHAT_CATEGORY = "HipChat";
  public static final String PROPERTY_DISABLED = "sonar.hipchat.disabled";
  public static final String PROPERTY_ROOM = "sonar.hipchat.room";
  public static final String PROPERTY_TOKEN = "sonar.hipchat.token.secured";

  private final Settings settings;

  public HipChatSettings(Settings settings) {
    this.settings = settings;
  }

  public boolean isEnabled() {
    return !settings.getBoolean(PROPERTY_DISABLED) && getRoom() != null && getAccessToken() != null;
  }

  @CheckForNull
  public String getRoom() {
    return settings.getString(PROPERTY_ROOM);
  }

  @CheckForNull
  public String getAccessToken() {
    return settings.getString(PROPERTY_TOKEN);
  }

  public static List<PropertyDefinition> definitions() {
    int index = 1;
    return Arrays.asList(
      PropertyDefinition.builder(PROPERTY_DISABLED)
        .name("Disable Notifications")
        .type(PropertyType.BOOLEAN)
        .category(HIPCHAT_CATEGORY)
        .defaultValue(valueOf(false))
        .index(index++)
        .onQualifiers(Qualifiers.PROJECT)
        .build(),

      PropertyDefinition.builder(PROPERTY_ROOM)
        .name("HipChat Room")
        .description("The HipChat room to send notifications to, for example '2206095'")
        .type(PropertyType.STRING)
        .category(HIPCHAT_CATEGORY)
        .index(index++)
        .onQualifiers(Qualifiers.PROJECT)
        .build(),

      PropertyDefinition.builder(PROPERTY_TOKEN)
        .name("Authentication Token")
        .description("The HipToken token for sending notifications to room")
        .type(PropertyType.STRING)
        .category(HIPCHAT_CATEGORY)
        .index(index++)
        .onQualifiers(Qualifiers.PROJECT)
        .build()
      );
  }
}
