/*
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

import org.junit.Test;
import org.sonar.api.Plugin;
import org.sonar.api.SonarQubeVersion;

import static org.assertj.core.api.Assertions.assertThat;

public class HipChatPluginTest {

  HipChatPlugin underTest = new HipChatPlugin();

  @Test
  public void define_extensions() {
    Plugin.Context context = new Plugin.Context(SonarQubeVersion.V5_6);

    underTest.define(context);

    assertThat(context.getExtensions()).hasSize(
      // nb of components
      2 +

      // nb of properties
      3);
  }

}
