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
   * Configuration property name used to specify the features to extract from images.
   */
  public static final String FEATURES = "features";

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

  /**
   * TODO document
   */
  public static class TextAnnotation {
    public static final String DESCRIPTION_FIELD_NAME = "description";
    public static final String POSITION_FIELD_NAME = "position";

    public static final Schema SCHEMA = Schema.recordOf(
      "text-annotation-component-record",
      Schema.Field.of(DESCRIPTION_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(POSITION_FIELD_NAME, Schema.arrayOf(Vertex.SCHEMA)));
  }

  /**
   * TODO document
   */
  public static class TextSymbol {
    public static final String TEXT_FIELD_NAME = "text";
    public static final String CONFIDENCE_FIELD_NAME = "confidence";

    public static final Schema SCHEMA = Schema.recordOf(
      "document-text-page-symbol-record",
      Schema.Field.of(TEXT_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(CONFIDENCE_FIELD_NAME, Schema.of(Schema.Type.FLOAT)));
  }

  /**
   * TODO document
   */
  public static class TextWord {
    public static final String TEXT_FIELD_NAME = "text";
    public static final String CONFIDENCE_FIELD_NAME = "confidence";
    public static final String SYMBOLS_FIELD_NAME = "symbols";

    public static final Schema SCHEMA = Schema.recordOf(
      "document-text-page-word-record",
      Schema.Field.of(TEXT_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(CONFIDENCE_FIELD_NAME, Schema.of(Schema.Type.FLOAT)),
      Schema.Field.of(SYMBOLS_FIELD_NAME, Schema.arrayOf(TextSymbol.SCHEMA)));
  }

  /**
   * TODO document
   */
  public static class TextParagraph {
    public static final String TEXT_FIELD_NAME = "text";
    public static final String CONFIDENCE_FIELD_NAME = "confidence";
    public static final String WORDS_FIELD_NAME = "words";

    public static final Schema SCHEMA = Schema.recordOf(
      "document-text-page-paragraph-record",
      Schema.Field.of(TEXT_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(CONFIDENCE_FIELD_NAME, Schema.of(Schema.Type.FLOAT)),
      Schema.Field.of(WORDS_FIELD_NAME, Schema.arrayOf(TextWord.SCHEMA)));
  }

  /**
   * TODO document
   */
  public static class TextBlock {
    public static final String TEXT_FIELD_NAME = "text";
    public static final String PARAGRAPHS_FIELD_NAME = "paragraphs";

    public static final Schema SCHEMA = Schema.recordOf(
      "document-text-page-block-record",
      Schema.Field.of(TEXT_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(PARAGRAPHS_FIELD_NAME, Schema.arrayOf(TextParagraph.SCHEMA)));
  }

  /**
   * TODO document
   */
  public static class TextPage {
    public static final String TEXT_FIELD_NAME = "text";
    public static final String BLOCKS_FIELD_NAME = "blocks";

    public static final Schema SCHEMA = Schema.recordOf(
      "document-text-page-record",
      Schema.Field.of(TEXT_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(BLOCKS_FIELD_NAME, Schema.arrayOf(TextBlock.SCHEMA)));
  }

  /**
   * TODO document
   */
  public static class HandwritingAnnotation {
    public static final String TEXT_FIELD_NAME = "text";
    public static final String PAGES_FIELD_NAME = "pages";

    public static final Schema SCHEMA = Schema.recordOf(
      "document-text-annotation-component-record",
      Schema.Field.of(TEXT_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(PAGES_FIELD_NAME, Schema.arrayOf(TextPage.SCHEMA)));
  }

  /**
   * TODO document
   */
  public static class CropHintAnnotation {
    public static final String POSITION_FIELD_NAME = "position";

    public static final Schema SCHEMA = Schema.recordOf(
      "crop-hint-annotation-component-record",
      Schema.Field.of(POSITION_FIELD_NAME, Schema.arrayOf(Vertex.SCHEMA)));
  }

  /**
   * TODO document
   */
  public static class ColorInfo {
    public static final String PIXEL_FRACTION_FIELD_NAME = "pixelFraction";
    public static final String SCORE_FIELD_NAME = "score";
    public static final String RED_FIELD_NAME = "red";
    public static final String GREEN_FIELD_NAME = "green";
    public static final String BLUE_FIELD_NAME = "blue";

    public static final Schema SCHEMA = Schema.recordOf(
      "dominant-colors-annotation-component-record",
      Schema.Field.of(PIXEL_FRACTION_FIELD_NAME, Schema.of(Schema.Type.FLOAT)),
      Schema.Field.of(SCORE_FIELD_NAME, Schema.of(Schema.Type.FLOAT)),
      Schema.Field.of(RED_FIELD_NAME, Schema.of(Schema.Type.FLOAT)),
      Schema.Field.of(GREEN_FIELD_NAME, Schema.of(Schema.Type.FLOAT)),
      Schema.Field.of(BLUE_FIELD_NAME, Schema.of(Schema.Type.FLOAT)));
  }

  /**
   * TODO document
   */
  public static class LandmarkLocation {
    public static final String LATITUDE_FIELD_NAME = "latitude";
    public static final String LONGITUDE_FIELD_NAME = "longitude";

    public static final Schema SCHEMA = Schema.recordOf(
      "landmark-location-record",
      Schema.Field.of(LATITUDE_FIELD_NAME, Schema.of(Schema.Type.FLOAT)),
      Schema.Field.of(LONGITUDE_FIELD_NAME, Schema.of(Schema.Type.FLOAT))
    );
  }

  /**
   * TODO document
   */
  public static class LandmarkAnnotation {
    /**
     * TODO document
     */
    public static final String MID_FIELD_NAME = "mid";
    public static final String DESCRIPTION_FIELD_NAME = "description";
    public static final String SCORE_FIELD_NAME = "score";
    public static final String POSITION_FIELD_NAME = "position";
    public static final String LOCATION_FIELD_NAME = "locationInfo";

    public static final Schema SCHEMA = Schema.recordOf(
      "landmark-annotation-component-record",
      Schema.Field.of(MID_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(DESCRIPTION_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(SCORE_FIELD_NAME, Schema.of(Schema.Type.FLOAT)),
      Schema.Field.of(POSITION_FIELD_NAME, Schema.arrayOf(Vertex.SCHEMA)),
      Schema.Field.of(LOCATION_FIELD_NAME, Schema.arrayOf(LandmarkLocation.SCHEMA))
    );
  }

  /**
   * TODO document
   */
  public static class LogoAnnotation {
    /**
     * TODO document
     */
    public static final String MID_FIELD_NAME = "mid";
    public static final String DESCRIPTION_FIELD_NAME = "description";
    public static final String SCORE_FIELD_NAME = "score";
    public static final String POSITION_FIELD_NAME = "position";

    public static final Schema SCHEMA = Schema.recordOf(
      "logo-annotation-component-record",
      Schema.Field.of(MID_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(DESCRIPTION_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(SCORE_FIELD_NAME, Schema.of(Schema.Type.FLOAT)),
      Schema.Field.of(POSITION_FIELD_NAME, Schema.arrayOf(Vertex.SCHEMA))
    );
  }
}
