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
import com.google.cloud.vision.v1.ImageSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides handy methods to access Google Cloud Vision.
 */
public class CloudVisionClient implements AutoCloseable {

  private final ImageExtractorTransformConfig config;
  private ImageAnnotatorClient client;

  public CloudVisionClient(ImageExtractorTransformConfig config) {
    this.config = config;
  }

  public AnnotateImageResponse extractFaces(String gcsPath) throws Exception {
    ensureClientInitialized();

    List<AnnotateImageRequest> requests = new ArrayList<>();
    ImageSource imgSource = ImageSource.newBuilder().setGcsImageUri(gcsPath).build();
    Image img = Image.newBuilder().setSource(imgSource).build();
    Feature feat = Feature.newBuilder().setType(Feature.Type.FACE_DETECTION).build();

    AnnotateImageRequest request =
      AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
    requests.add(request);

    BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
    List<AnnotateImageResponse> responses = response.getResponsesList();

    // todo avoid hardcode. investigate an ability using batches
    return responses.get(0);
  }


  private void ensureClientInitialized() throws IOException {
    if (client != null) {
      return;
    }

    Credentials credentials = loadServiceAccountCredentials(config.getServiceFilePath());
    ImageAnnotatorSettings imageAnnotatorSettings = ImageAnnotatorSettings.newBuilder()
      .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
      .build();
    client = ImageAnnotatorClient.create(imageAnnotatorSettings);
  }

  private ServiceAccountCredentials loadServiceAccountCredentials(String path) throws IOException {
    File credentialsPath = new File(path);
    try (FileInputStream serviceAccountStream = new FileInputStream(credentialsPath)) {
      return ServiceAccountCredentials.fromStream(serviceAccountStream);
    }
  }

  @Override
  public void close() throws Exception {

  }
}
