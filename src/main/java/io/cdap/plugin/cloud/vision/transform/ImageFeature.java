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

import com.google.cloud.vision.v1.Feature;
import io.cdap.cdap.api.data.schema.Schema;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/**
 * The features to extract from images.
 */
public enum ImageFeature {

  FACE("Face", Feature.Type.FACE_DETECTION, Schema.arrayOf(ImageExtractorConstants.FaceAnnotation.SCHEMA)),
  TEXT("Text", Feature.Type.TEXT_DETECTION, Schema.arrayOf(ImageExtractorConstants.TextAnnotation.SCHEMA)),
  CROP_HINTS("Crop Hints", Feature.Type.CROP_HINTS, Schema.arrayOf(ImageExtractorConstants.CropHintAnnotation.SCHEMA)),
  HANDWRITING(
    "Handwriting",
    Feature.Type.DOCUMENT_TEXT_DETECTION,
    ImageExtractorConstants.HandwritingAnnotation.SCHEMA
  ),
  IMAGE_PROPERTIES(
    "Image Properties",
    Feature.Type.IMAGE_PROPERTIES,
    Schema.arrayOf(ImageExtractorConstants.ColorInfo.SCHEMA)
  ),
  LABELS(
    "Labels",
    Feature.Type.LABEL_DETECTION,
    Schema.arrayOf(Schema.mapOf(Schema.of(Schema.Type.STRING), Schema.of(Schema.Type.STRING)))
  ),
  LANDMARKS(
    "Landmarks",
    Feature.Type.LANDMARK_DETECTION,
    Schema.arrayOf(ImageExtractorConstants.LandmarkAnnotation.SCHEMA)
  ),
  LOGOS("Logos", Feature.Type.LOGO_DETECTION, Schema.arrayOf(ImageExtractorConstants.LogoAnnotation.SCHEMA)),
  // Object localization is used to detect multiple objects
  OBJECT_LOCALIZATION(
    "Object Localization",
    Feature.Type.OBJECT_LOCALIZATION,
    Schema.arrayOf(ImageExtractorConstants.LocalizedObjectAnnotation.SCHEMA)
  ),
  EXPLICIT_CONTENT(
    "Explicit Content",
    Feature.Type.SAFE_SEARCH_DETECTION,
    ImageExtractorConstants.SafeSearchAnnotation.SCHEMA
  ),
  WEB_DETECTION("Web Detection", Feature.Type.WEB_DETECTION, ImageExtractorConstants.WebDetection.SCHEMA),
  // TODO Product Search support
  PRODUCT_SEARCH("Product Search", Feature.Type.PRODUCT_SEARCH, null);

  private static final Map<String, ImageFeature> byDisplayName = Arrays.stream(values())
    .collect(Collectors.toMap(ImageFeature::getDisplayName, Function.identity()));

  private final String displayName;
  private final Feature.Type featureType;
  private final Schema schema;

  ImageFeature(String displayName, Feature.Type featureType, Schema schema) {
    this.displayName = displayName;
    this.featureType = featureType;
    this.schema = schema;
  }

  @Nullable
  public static ImageFeature fromDisplayName(String displayName) {
    return byDisplayName.get(displayName);
  }

  public String getDisplayName() {
    return displayName;
  }

  public Feature.Type getFeatureType() {
    return featureType;
  }

  public Schema getSchema() {
    return schema;
  }
}
