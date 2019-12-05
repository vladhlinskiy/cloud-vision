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

import com.google.cloud.vision.v1.AnnotateFileRequest;
import com.google.cloud.vision.v1.AnnotateFileResponse;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateFilesResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.GcsSource;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.InputConfig;
import io.cdap.plugin.cloud.vision.exception.CloudVisionExecutionException;
import io.cdap.plugin.cloud.vision.transform.CloudVisionClient;
import java.util.Collections;

/**
 * Provides handy methods to access Google Cloud Vision.
 */
public class DocumentAnnotatorClient extends CloudVisionClient {

  private final DocumentExtractorTransformConfig config;

  public DocumentAnnotatorClient(DocumentExtractorTransformConfig config) {
    super(config);
    this.config = config;
  }

  public AnnotateImageResponse extractDocumentFeature(String gcsPath) throws Exception {
    try (ImageAnnotatorClient client = createImageAnnotatorClient()) {
      Feature.Type featureType = config.getImageFeature().getFeatureType();
      Feature feature = Feature.newBuilder().setType(featureType).build();

      InputConfig inputConfig = InputConfig.newBuilder()
        .setGcsSource(GcsSource.newBuilder().setUri(gcsPath))
        .setMimeType(config.getMimeType())
        .build();

      // TODO image context
      AnnotateFileRequest requestsElement =
        AnnotateFileRequest.newBuilder()
          .setInputConfig(inputConfig)
          .addFeatures(feature)
          .addAllPages(config.getPagesList())
          .build();
      BatchAnnotateFilesResponse response = client.batchAnnotateFiles(Collections.singletonList(requestsElement));

      // TODO check response list
      AnnotateFileResponse annotateFileResponse = response.getResponses(SINGLE_RESPONSE_INDEX);
      // TODO check response list
      AnnotateImageResponse annotateImageResponse = annotateFileResponse.getResponses(SINGLE_RESPONSE_INDEX);

      if (annotateImageResponse.hasError()) {
        String errorMessage = String.format("Unable to extract '%s' feature of image '%s' due to: '%s'", featureType,
          gcsPath, annotateImageResponse.getError().getMessage());
        throw new CloudVisionExecutionException(errorMessage);
      }

      return annotateImageResponse;
    }
  }
}
