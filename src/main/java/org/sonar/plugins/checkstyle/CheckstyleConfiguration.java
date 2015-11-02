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

import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.BatchExtension;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.InputFileUtils;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.utils.SonarException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

public class CheckstyleConfiguration implements BatchExtension {

  private static final Logger LOG = LoggerFactory.getLogger(CheckstyleConfiguration.class);
  public static final String PROPERTY_GENERATE_XML = "sonar.checkstyle.generateXml";

  private final CheckstyleProfileExporter confExporter;
  private final RulesProfile profile;
  private final Settings conf;
  private final ProjectFileSystem fileSystem;

  public CheckstyleConfiguration(Settings conf, CheckstyleProfileExporter confExporter, RulesProfile profile, ProjectFileSystem fileSystem) {
    this.conf = conf;
    this.confExporter = confExporter;
    this.profile = profile;
    this.fileSystem = fileSystem;
  }

  public File getXMLDefinitionFile() {
    Writer writer = null;
    File xmlFile = new File(fileSystem.getSonarWorkingDirectory(), "checkstyle.xml");
    try {
      writer = new OutputStreamWriter(new FileOutputStream(xmlFile, false), StandardCharsets.UTF_8);
      confExporter.exportProfile(profile, writer);
      writer.flush();
      return xmlFile;

    } catch (IOException e) {
      throw new SonarException("Fail to save the Checkstyle configuration to " + xmlFile.getPath(), e);

    } finally {
      IOUtils.closeQuietly(writer);
    }
  }

  public List<File> getSourceFiles() {
    return InputFileUtils.toFiles(fileSystem.mainFiles(CheckstyleConstants.JAVA_KEY));
  }

  public File getTargetXMLReport() {
    if (conf.getBoolean(PROPERTY_GENERATE_XML)) {
      return new File(fileSystem.getSonarWorkingDirectory(), "checkstyle-result.xml");
    }
    return null;
  }

  public com.puppycrawl.tools.checkstyle.api.Configuration getCheckstyleConfiguration() throws CheckstyleException {
    File xmlConfig = getXMLDefinitionFile();

    LOG.info("Checkstyle configuration: " + xmlConfig.getAbsolutePath());
    com.puppycrawl.tools.checkstyle.api.Configuration configuration = toCheckstyleConfiguration(xmlConfig);
    defineCharset(configuration);
    return configuration;
  }

  static com.puppycrawl.tools.checkstyle.api.Configuration toCheckstyleConfiguration(File xmlConfig) throws CheckstyleException {
    return ConfigurationLoader.loadConfiguration(xmlConfig.getAbsolutePath(), new PropertiesExpander(new Properties()));
  }

  private void defineCharset(com.puppycrawl.tools.checkstyle.api.Configuration configuration) {
    com.puppycrawl.tools.checkstyle.api.Configuration[] modules = configuration.getChildren();
    for (com.puppycrawl.tools.checkstyle.api.Configuration module : modules) {
      if (("Checker".equals(module.getName()) || "com.puppycrawl.tools.checkstyle.Checker".equals(module.getName())) && module instanceof DefaultConfiguration) {
        Charset charset = getCharset();
        LOG.info("Checkstyle charset: " + charset.name());
        ((DefaultConfiguration) module).addAttribute("charset", charset.name());
      }
    }
  }

  public Charset getCharset() {
    Charset charset = fileSystem.getSourceCharset();
    if (charset == null) {
      String charsetName = System.getProperty("file.encoding");
      if (charsetName != null) {
        charset = Charset.forName(charsetName);
      } else {
        charset = StandardCharsets.UTF_8;
      }
    }
    return charset;
  }

}
