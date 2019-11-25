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

package io.cdap.plugin.cloud.vision.transform;

import com.google.cloud.ServiceOptions;
import com.google.common.base.Strings;
import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Macro;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.data.schema.Schema;
import io.cdap.cdap.api.plugin.PluginConfig;
import io.cdap.cdap.etl.api.FailureCollector;
import io.cdap.plugin.cloud.vision.CloudVisionConstants;

import java.io.IOException;
import java.util.Objects;
import javax.annotation.Nullable;

/**
 * Defines a {@link PluginConfig} that Image Extractor transform can use.
 */
public class ImageExtractorTransformConfig extends PluginConfig {

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

  @Name(ImageExtractorConstants.PATH_FIELD)
  @Description("Field in the input schema containing the path to the image.")
  @Macro
  protected String pathField;

  @Name(ImageExtractorConstants.OUTPUT_FIELD)
  @Description("Field to store the extracted image features. If the specified output field name already exists " +
    "in the input record, it will be overwritten.")
  @Macro
  protected String outputField;

  @Name(ImageExtractorConstants.FEATURES)
  @Description("Features to extract from images.")
  @Macro
  protected String features;

  @Name(ImageExtractorConstants.SCHEMA)
  @Description("Schema of records output by the transform.")
  @Nullable
  private String schema;

  public ImageExtractorTransformConfig(String project, String serviceFilePath, String pathField, String outputField,
                                       String features, String schema) {
    this.project = project;
    this.serviceFilePath = serviceFilePath;
    this.pathField = pathField;
    this.outputField = outputField;
    this.features = features;
    this.schema = schema;
  }

  @Nullable
  public String getServiceFilePath() {
    return serviceFilePath;
  }

  public String getPathField() {
    return pathField;
  }

  public String getOutputField() {
    return outputField;
  }

  public String getFeatures() {
    return features;
  }

  @Nullable
  public String getSchema() {
    return schema;
  }

  public ImageFeature getImageFeature() {
    return Objects.requireNonNull(ImageFeature.fromDisplayName(features));
  }

  /**
   * Parses the json representation into a schema object.
   *
   * @return parsed schema object of json representation.
   * @throws RuntimeException if there was an exception parsing the schema.
   */
  @Nullable
  public Schema getParsedSchema() {
    try {
      return Strings.isNullOrEmpty(schema) ? null : Schema.parseJson(schema);
    } catch (IOException e) {
      // this should not happen, since schema string comes from UI
      throw new IllegalStateException(String.format("Could not parse schema string: '%s'", schema), e);
    }
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
   * Validates {@link ImageExtractorTransformConfig} instance.
   *
   * @param collector failure collector.
   */
  public void validate(FailureCollector collector) {
    if (!containsMacro(ImageExtractorConstants.PATH_FIELD) && Strings.isNullOrEmpty(pathField)) {
      collector.addFailure("Path field must be specified", null)
        .withConfigProperty(ImageExtractorConstants.PATH_FIELD);
    }
    if (!containsMacro(ImageExtractorConstants.OUTPUT_FIELD) && Strings.isNullOrEmpty(outputField)) {
      collector.addFailure("Output field must be specified", null)
        .withConfigProperty(ImageExtractorConstants.OUTPUT_FIELD);
    }
    // TODO validate schema. it must contain path & output fields?
  }

  /**
   * Validate that the provided schema is compatible with the inferred schema. The provided schema is compatible if
   * every field is compatible with the corresponding field in the inferred schema. A field is compatible if it is of
   * the same type or is a nullable version of that type. It is assumed that both schemas are record schemas.
   *
   * @param inferredSchema the inferred schema
   * @param providedSchema the provided schema to check compatibility
   * @param collector      failure collector
   * @throws IllegalArgumentException if the schemas are not type compatible
   */
  public static void validateFieldsMatch(Schema inferredSchema, Schema providedSchema, FailureCollector collector) {
    for (Schema.Field field : providedSchema.getFields()) {
      Schema.Field inferredField = inferredSchema.getField(field.getName());
      Schema inferredFieldSchema = inferredField.getSchema();
      Schema providedFieldSchema = field.getSchema();

      boolean isInferredFieldNullable = inferredFieldSchema.isNullable();
      boolean isProvidedFieldNullable = providedFieldSchema.isNullable();

      Schema inferredFieldNonNullableSchema = isInferredFieldNullable
        ? inferredFieldSchema.getNonNullable() : inferredFieldSchema;
      Schema providedFieldNonNullableSchema = isProvidedFieldNullable ?
        providedFieldSchema.getNonNullable() : providedFieldSchema;

      Schema.Type inferredType = inferredFieldNonNullableSchema.getType();
      Schema.LogicalType inferredLogicalType = inferredFieldNonNullableSchema.getLogicalType();
      Schema.Type providedType = providedFieldNonNullableSchema.getType();
      Schema.LogicalType providedLogicalType = providedFieldNonNullableSchema.getLogicalType();
      if (inferredType != providedType && inferredLogicalType != providedLogicalType) {
        String errorMessage = String.format("Expected field '%s' to be of type '%s', but it is of type '%s'",
                                            field.getName(), inferredFieldNonNullableSchema.getDisplayName(),
                                            providedFieldNonNullableSchema.getDisplayName());

        collector.addFailure(errorMessage, String.format("Change field '%s' to be a supported type", field.getName()))
          .withOutputSchemaField(field.getName(), null);
      }

      if (!isInferredFieldNullable && isProvidedFieldNullable) {
        String errorMessage = String.format("Field '%s' should not be nullable", field.getName());
        collector.addFailure(errorMessage, String.format("Change field '%s' to be non-nullable", field.getName()))
          .withOutputSchemaField(field.getName(), null);
      }
    }
  }
}
