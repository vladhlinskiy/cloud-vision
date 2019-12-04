/*
 * Copyright © 2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.plugin.cloud.vision;

import com.google.cloud.ServiceOptions;
import com.google.common.base.Strings;
import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Macro;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.data.schema.Schema;
import io.cdap.cdap.api.plugin.PluginConfig;
import io.cdap.cdap.etl.api.FailureCollector;
import io.cdap.plugin.common.Constants;
import io.cdap.plugin.common.IdUtils;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import javax.annotation.Nullable;

/**
 * Defines a {@link PluginConfig} that File Path batch source can use.
 */
public class FilePathSourceConfig extends PluginConfig {

  public static final Schema SCHEMA = Schema.recordOf("schema", Schema.Field.of(CloudVisionConstants.PATH_FIELD_NAME,
                                                                                Schema.of(Schema.Type.STRING)));

  @Name(Constants.Reference.REFERENCE_NAME)
  @Description(Constants.Reference.REFERENCE_NAME_DESCRIPTION)
  private String referenceName;

  @Name(CloudVisionConstants.PROJECT)
  @Description("Google Cloud Project ID, which uniquely identifies a project. "
    + "It can be found on the Dashboard in the Google Cloud Platform Console.")
  @Macro
  @Nullable
  protected String project;

  @Name(CloudVisionConstants.SERVICE_ACCOUNT_FILE_PATH)
  @Description("Path on the local file system of the service account key used "
    + "for authorization. Can be set to 'auto-detect' when running on a Dataproc cluster. "
    + "When running on other clusters, the file must be present on every node in the cluster.")
  @Macro
  @Nullable
  protected String serviceFilePath;

  @Name(CloudVisionConstants.PATH)
  @Description("The path to the directory where the files whose paths are to be emitted are located.")
  @Macro
  protected String path;

  @Name(CloudVisionConstants.RECURSIVE)
  @Description("Whether the plugin should recursively traverse the directory for subdirectories.")
  @Macro
  protected boolean recursive;

  @Name(CloudVisionConstants.LAST_MODIFIED)
  @Description("A way to filter files to be returned based on their last modified timestamp.")
  @Macro
  @Nullable
  protected String lastModified;

  @Name(CloudVisionConstants.SPLIT_BY)
  @Description("Determines splitting mechanisms. Choose amongst default (uses the default splitting mechanism of " +
    "file input format), directory (by each sub directory).")
  @Macro
  protected String splitBy;

  public FilePathSourceConfig(String referenceName, String project, String serviceFilePath, String path,
                              boolean recursive, String lastModified, String splitBy) {
    this.referenceName = referenceName;
    this.project = project;
    this.serviceFilePath = serviceFilePath;
    this.path = path;
    this.recursive = recursive;
    this.lastModified = lastModified;
    this.splitBy = splitBy;
  }

  public String getReferenceName() {
    return referenceName;
  }

  public String getPath() {
    return path;
  }

  public boolean isRecursive() {
    return recursive;
  }

  @Nullable
  public String getLastModified() {
    return lastModified;
  }

  @Nullable
  public Long getLastModifiedEpochMilli() {
    return Strings.isNullOrEmpty(lastModified) ? null : Instant.parse(lastModified).toEpochMilli();
  }

  public String getSplitBy() {
    return splitBy;
  }

  public SplittingMechanism getSplittingMechanism() {
    return Objects.requireNonNull(SplittingMechanism.fromDisplayName(splitBy));
  }

  public String getProject() {
    String projectId = tryGetProject();
    if (projectId == null) {
      throw new IllegalArgumentException(
        "Could not detect Google Cloud project id from the environment. Please specify a project id.");
    }
    return projectId;
  }

  @Nullable
  public String tryGetProject() {
    if (containsMacro(CloudVisionConstants.PROJECT) && Strings.isNullOrEmpty(project)) {
      return null;
    }
    String projectId = project;
    if (Strings.isNullOrEmpty(project) || CloudVisionConstants.AUTO_DETECT.equals(project)) {
      projectId = ServiceOptions.getDefaultProjectId();
    }
    return projectId;
  }

  @Nullable
  public String getServiceAccountFilePath() {
    if (containsMacro(CloudVisionConstants.SERVICE_ACCOUNT_FILE_PATH) || Strings.isNullOrEmpty(serviceFilePath)
      || CloudVisionConstants.AUTO_DETECT.equals(serviceFilePath)) {
      return null;
    }
    return serviceFilePath;
  }

  /**
   * Validates {@link FilePathSourceConfig} instance.
   *
   * @param collector failure collector.
   */
  public void validate(FailureCollector collector) {
    if (Strings.isNullOrEmpty(referenceName)) {
      collector.addFailure("Reference name must be specified", null)
        .withConfigProperty(Constants.Reference.REFERENCE_NAME);
    } else {
      IdUtils.validateReferenceName(referenceName, collector);
    }
    if (!containsMacro(CloudVisionConstants.PATH) && Strings.isNullOrEmpty(path)) {
      collector.addFailure("Path must be specified", null)
        .withConfigProperty(CloudVisionConstants.PATH);
    }
    if (!containsMacro(CloudVisionConstants.LAST_MODIFIED) && !Strings.isNullOrEmpty(lastModified)) {
      try {
        getLastModifiedEpochMilli();
      } catch (DateTimeParseException e) {
        collector.addFailure("Timestamp string is expected to be in the following format: " +
                               "\"yyyy-MM-dd'T'HH:mm:ss.SSSZ\". For example: \"2019-10-02T13:12:55.123Z\".", null)
          .withConfigProperty(CloudVisionConstants.LAST_MODIFIED);
      }
    }
    if (!containsMacro(CloudVisionConstants.SPLIT_BY)) {
      if (Strings.isNullOrEmpty(splitBy)) {
        collector.addFailure("Splitting mechanism must be specified", null)
          .withConfigProperty(CloudVisionConstants.SPLIT_BY);
      } else if (SplittingMechanism.fromDisplayName(splitBy) == null) {
        collector.addFailure("Invalid splitting mechanism name", null)
          .withConfigProperty(CloudVisionConstants.SPLIT_BY);
      }
    }
  }
}
