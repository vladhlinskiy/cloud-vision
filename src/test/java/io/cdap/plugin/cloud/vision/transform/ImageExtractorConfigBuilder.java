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

import io.cdap.plugin.cloud.vision.CloudVisionConfigBuilder;

import javax.annotation.Nullable;

/**
 * Builder class that provides handy methods to construct {@link ImageExtractorTransformConfig} for testing.
 */
public class ImageExtractorConfigBuilder extends CloudVisionConfigBuilder<ImageExtractorConfigBuilder> {

  private String pathField;
  private String outputField;
  private String features;

  @Nullable
  private String languageHints;

  @Nullable
  private String schema;

  public static ImageExtractorConfigBuilder builder() {
    return new ImageExtractorConfigBuilder();
  }

  public static ImageExtractorConfigBuilder builder(ImageExtractorTransformConfig original) {
    return new ImageExtractorConfigBuilder()
      .setProject(original.getProject())
      .setServiceFilePath(original.getServiceAccountFilePath())
      .setPathField(original.getPathField())
      .setOutputField(original.getOutputField())
      .setFeatures(original.getFeatures())
      .setLanguageHints(original.getLanguageHints())
      .setSchema(original.getSchema());
  }

  public ImageExtractorConfigBuilder setPathField(String pathField) {
    this.pathField = pathField;
    return this;
  }

  public ImageExtractorConfigBuilder setOutputField(String outputField) {
    this.outputField = outputField;
    return this;
  }

  public ImageExtractorConfigBuilder setFeatures(String features) {
    this.features = features;
    return this;
  }

  public ImageExtractorConfigBuilder setLanguageHints(@Nullable String languageHints) {
    this.languageHints = languageHints;
    return this;
  }

  public ImageExtractorConfigBuilder setSchema(@Nullable String schema) {
    this.schema = schema;
    return this;
  }

  public ImageExtractorTransformConfig build() {
    return new ImageExtractorTransformConfig(project, serviceFilePath, pathField, outputField, features, languageHints,
                                             schema);
  }
}
