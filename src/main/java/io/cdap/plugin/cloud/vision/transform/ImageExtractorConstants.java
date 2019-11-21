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

import io.cdap.cdap.api.data.schema.Schema;

/**
 * Cloud Vision Image Extractor constants.
 */
public class ImageExtractorConstants {

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
   * Configuration property name used to specify schema of records output by the transform.
   */
  public static final String SCHEMA = "schema";

  /**
   * TODO document
   */
  public static class Vertex {
    public static final String X_FIELD_NAME = "x";
    public static final String Y_FIELD_NAME = "y";

    public static final Schema SCHEMA = Schema.recordOf("vertex-record",
                                                        Schema.Field.of(X_FIELD_NAME, Schema.of(Schema.Type.INT)),
                                                        Schema.Field.of(Y_FIELD_NAME, Schema.of(Schema.Type.INT)));
  }

  /**
   * TODO document
   */
  public static class FaceAnnotation {
    public static final String ANGER_FIELD_NAME = "anger";
    public static final String JOY_FIELD_NAME = "joy";
    public static final String SURPRISE_FIELD_NAME = "surprise";
    public static final String POSITION_FIELD_NAME = "position";

    public static final Schema SCHEMA = Schema.recordOf(
      "face-annotation-component-record",
      Schema.Field.of(ANGER_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(JOY_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(SURPRISE_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(POSITION_FIELD_NAME, Schema.arrayOf(Vertex.SCHEMA)));
  }
}
