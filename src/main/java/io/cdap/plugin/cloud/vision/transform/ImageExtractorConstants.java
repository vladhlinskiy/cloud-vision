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

import io.cdap.plugin.cloud.vision.CloudVisionConstants;

/**
 * Cloud Vision Image Extractor constants.
 */
public class ImageExtractorConstants extends CloudVisionConstants {

  private ImageExtractorConstants() {
    throw new AssertionError("Should not instantiate static utility class.");
  }

  /**
   * Image Extractor Transform plugin name.
   */
  public static final String PLUGIN_NAME = "ImageExtractor";

  /**
   * Configuration property name used to specify field in the input schema containing the path to the image.
   */
  public static final String PATH_FIELD = "pathField";

  /**
   * Configuration property name used to specify field to store the extracted image features.
   */
  public static final String OUTPUT_FIELD = "outputField";

  /**
   * Configuration property name used to specify the features to extract from images.
   */
  public static final String FEATURES = "features";

  /**
   * Configuration property name used to specify hints to detect the language of the text in the images.
   */
  public static final String LANGUAGE_HINTS = "languageHints";

  /**
   * Configuration property name used to specify schema of records output by the transform.
   */
  public static final String SCHEMA = "schema";
}
