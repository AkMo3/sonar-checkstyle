/*
 * SonarQube Checkstyle Plugin
 * Copyright (C) 2012 SonarSource
 * sonarqube@googlegroups.com
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.checkstyle;

public final class CheckstyleConstants {

  public static final String REPOSITORY_NAME = "Checkstyle";
  public static final String PLUGIN_KEY = "checkstyle";
  public static final String PLUGIN_NAME = REPOSITORY_NAME;
  public static final String REPOSITORY_KEY = PLUGIN_KEY;

  public static final String FILTERS_KEY = "sonar.checkstyle.filters";

  public static final String FILTERS_DEFAULT_VALUE =
    "<module name=\"SuppressionCommentFilter\" />" +
      "<module name=\"SuppressWarningsFilter\" />";

  public static final String JAVA_KEY = "java";

  private CheckstyleConstants() {
  }
}
