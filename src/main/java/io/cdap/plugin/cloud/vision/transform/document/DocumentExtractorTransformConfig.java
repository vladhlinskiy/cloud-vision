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

package io.cdap.plugin.cloud.vision.transform.document;

import com.google.common.base.Strings;
import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Macro;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.plugin.PluginConfig;
import io.cdap.cdap.etl.api.FailureCollector;
import io.cdap.plugin.cloud.vision.transform.ExtractorTransformConfig;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/**
 * Defines a {@link PluginConfig} that Document Extractor transform can use.
 */
public class DocumentExtractorTransformConfig extends ExtractorTransformConfig {

  @Name(DocumentExtractorTransformConstants.CONTENT_FIELD)
  @Description("Field in the input schema containing the file content, represented as a stream of bytes.")
  @Macro
  @Nullable
  private String contentField;

  @Name(DocumentExtractorTransformConstants.MIME_TYPE)
  @Description("The type of the file. Currently only 'application/pdf', 'image/tiff' and 'image/gif' are supported. " +
    "Wildcards are not supported.")
  @Macro
  private String mimeType;

  @Name(DocumentExtractorTransformConstants.PAGES)
  @Description("Pages in the file to perform image annotation.")
  @Macro
  private String pages;

  public DocumentExtractorTransformConfig(String project, String serviceFilePath, String pathField, String outputField,
                                          String features, @Nullable String languageHints,
                                          @Nullable String aspectRatios, @Nullable Boolean includeGeoResults,
                                          @Nullable String productSet, @Nullable String productCategories,
                                          @Nullable String boundingPolygon, @Nullable String filter,
                                          @Nullable String schema, @Nullable String contentField, String mimeType,
                                          String pages) {
    super(project, serviceFilePath, pathField, outputField, features, languageHints, aspectRatios, includeGeoResults,
      productSet, productCategories, boundingPolygon, filter, schema);
    this.contentField = contentField;
    this.mimeType = mimeType;
    this.pages = pages;
  }

  @Nullable
  public String getContentField() {
    return contentField;
  }

  public String getMimeType() {
    return mimeType;
  }

  public String getPages() {
    return pages;
  }

  public List<Integer> getPagesList() {
    if (Strings.isNullOrEmpty(pages)) {
      return Collections.emptyList();
    }

    return Arrays.stream(pages.split(","))
      .map(Integer::valueOf)
      .collect(Collectors.toList());
  }

  /**
   * Validates {@link DocumentExtractorTransformConfig} instance.
   *
   * @param collector failure collector.
   */
  public void validate(FailureCollector collector) {
    super.validate(collector);
    // TODO contentField + pathField
    // TODO ensure that "features" field exists
  }
}
