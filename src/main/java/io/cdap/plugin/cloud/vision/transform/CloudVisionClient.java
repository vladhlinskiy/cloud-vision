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

import com.google.api.client.util.Strings;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageAnnotatorSettings;
import com.google.cloud.vision.v1.ImageContext;
import com.google.cloud.vision.v1.ImageSource;
import com.google.cloud.vision.v1.ProductSearchParams;
import io.cdap.plugin.cloud.vision.exception.CloudVisionExecutionException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;

/**
 * Provides handy methods to access Google Cloud Vision.
 */
public class CloudVisionClient {

  private static final int SINGLE_RESPONSE_INDEX = 0;

  private final ImageExtractorTransformConfig config;

  public CloudVisionClient(ImageExtractorTransformConfig config) {
    this.config = config;
  }

  public AnnotateImageResponse extractFeature(String gcsPath) throws Exception {
    String serviceAccountFilePath = config.getServiceAccountFilePath();
    Credentials credentials = serviceAccountFilePath == null ? null : loadCredentials(serviceAccountFilePath);
    ImageAnnotatorSettings.Builder imageAnnotatorSettings = ImageAnnotatorSettings.newBuilder();
    if (credentials != null) {
      imageAnnotatorSettings.setCredentialsProvider(FixedCredentialsProvider.create(credentials));
    }
    try (ImageAnnotatorClient client = ImageAnnotatorClient.create(imageAnnotatorSettings.build())) {
      ImageSource imgSource = ImageSource.newBuilder().setGcsImageUri(gcsPath).build();
      Image img = Image.newBuilder().setSource(imgSource).build();

      Feature.Type featureType = config.getImageFeature().getFeatureType();
      Feature feature = Feature.newBuilder().setType(featureType).build();
      AnnotateImageRequest.Builder request = AnnotateImageRequest.newBuilder().addFeatures(feature).setImage(img);
      if (config.getImageFeature() == ImageFeature.TEXT && !Strings.isNullOrEmpty(config.getLanguageHints())) {
        // Text detection parameters
        ImageContext context = ImageContext.newBuilder().addAllLanguageHints(config.getLanguages()).build();
        request.setImageContext(context);
      } else if (config.getImageFeature() == ImageFeature.PRODUCT_SEARCH) {
        // Product search parameters
        ProductSearchParams.Builder productSearchParams = ProductSearchParams.newBuilder()
          .setProductSet(config.getProductSet())
          .addProductCategories(config.getProductCategory().getName());
        if (!Strings.isNullOrEmpty(config.getFilter())) {
          productSearchParams.setFilter(config.getFilter());
        }
        ImageContext context = ImageContext.newBuilder().setProductSearchParams(productSearchParams).build();
        request.setImageContext(context);
      }

      BatchAnnotateImagesResponse response = client.batchAnnotateImages(Collections.singletonList(request.build()));
      AnnotateImageResponse annotateImageResponse = response.getResponses(SINGLE_RESPONSE_INDEX);
      if (annotateImageResponse.hasError()) {
        String errorMessage = String.format("Unable to extract '%s' feature of image '%s' due to: '%s'", featureType,
          gcsPath, annotateImageResponse.getError().getMessage());
        throw new CloudVisionExecutionException(errorMessage);
      }

      return annotateImageResponse;
    }
  }

  private ServiceAccountCredentials loadCredentials(String path) throws IOException {
    File credentialsPath = new File(path);
    try (FileInputStream serviceAccountStream = new FileInputStream(credentialsPath)) {
      return ServiceAccountCredentials.fromStream(serviceAccountStream);
    }
  }
}
