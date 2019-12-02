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

import com.google.cloud.vision.v1.Likelihood;
import io.cdap.cdap.api.data.schema.Schema;
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

  /**
   * A vertex represents a 2D point in the image. {@link com.google.cloud.vision.v1.Vertex} mapped to a record with
   * following fields.
   */
  public static class Vertex {

    /**
     * X coordinate.
     */
    public static final String X_FIELD_NAME = "x";

    /**
     * Y coordinate.
     */
    public static final String Y_FIELD_NAME = "y";

    public static final Schema SCHEMA = Schema.recordOf("vertex-record",
      Schema.Field.of(X_FIELD_NAME, Schema.of(Schema.Type.INT)),
      Schema.Field.of(Y_FIELD_NAME, Schema.of(Schema.Type.INT)));
  }

  /**
   * {@link com.google.cloud.vision.v1.FaceAnnotation.Landmark} mapped to a record with following fields.
   */
  public static class FaceLandmark {

    /**
     * Face landmark type.
     */
    public static final String TYPE_FIELD_NAME = "type";

    /**
     * X coordinate.
     */
    public static final String X_FIELD_NAME = "x";

    /**
     * Y coordinate.
     */
    public static final String Y_FIELD_NAME = "y";

    /**
     * Z coordinate.
     */
    public static final String Z_FIELD_NAME = "z";

    public static final Schema SCHEMA = Schema.recordOf("face-landmark-record",
      Schema.Field.of(TYPE_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(X_FIELD_NAME, Schema.of(Schema.Type.FLOAT)),
      Schema.Field.of(Y_FIELD_NAME, Schema.of(Schema.Type.FLOAT)),
      Schema.Field.of(Z_FIELD_NAME, Schema.of(Schema.Type.FLOAT)));
  }

  /**
   * {@link com.google.cloud.vision.v1.FaceAnnotation} mapped to a record with following fields.
   */
  public static class FaceAnnotation {

    /**
     * Roll angle, which indicates the amount of clockwise/anti-clockwise rotation of the face relative to the image
     * vertical about the axis perpendicular to the face. Range [-180,180].
     */
    public static final String ROLL_ANGLE_FIELD_NAME = "rollAngle";

    /**
     * Yaw angle, which indicates the leftward/rightward angle that the face is pointing relative to the vertical plane
     * perpendicular to the image. Range [-180,180].
     */
    public static final String PAN_ANGLE_FIELD_NAME = "panAngle";

    /**
     * Pitch angle, which indicates the upwards/downwards angle that the face is pointing relative to the image's
     * horizontal plane. Range [-180,180].
     */
    public static final String TILT_ANGLE_FIELD_NAME = "tiltAngle";

    /**
     * Detection confidence. Range [0, 1].
     */
    public static final String DETECTION_CONFIDENCE_FIELD_NAME = "detectionConfidence";

    /**
     * Face landmarking confidence. Range [0, 1].
     */
    public static final String LANDMARKING_CONFIDENCE_FIELD_NAME = "landmarkingConfidence";

    /**
     * Anger likelihood. Possible values are defined by {@link Likelihood}.
     */
    public static final String ANGER_FIELD_NAME = "anger";

    /**
     * Joy likelihood. Possible values are defined by {@link Likelihood}.
     */
    public static final String JOY_FIELD_NAME = "joy";

    /**
     * Surprise likelihood. Possible values are defined by {@link Likelihood}.
     */
    public static final String SURPRISE_FIELD_NAME = "surprise";

    /**
     * Blurred likelihood. Possible values are defined by {@link Likelihood}.
     */
    public static final String BLURRED_FIELD_NAME = "blurred";

    /**
     * Under exposed likelihood. Possible values are defined by {@link Likelihood}.
     */
    public static final String UNDER_EXPOSED_FIELD_NAME = "underExposed";

    /**
     * Sorrow likelihood. Possible values are defined by {@link Likelihood}.
     */
    public static final String SORROW_FIELD_NAME = "sorrow";

    /**
     * Headwear likelihood. Possible values are defined by {@link Likelihood}.
     */
    public static final String HEADWEAR_FIELD_NAME = "headwear";

    /**
     * The bounding polygon around the face. The bounding box is computed to "frame" the face in accordance with human
     * expectations. It is based on the landmarker results. Note that one or more x and/or y coordinates may not be
     * generated if only a partial face appears in the image to be annotated.
     */
    public static final String POSITION_FIELD_NAME = "position";

    /**
     * The bounding polygon which is tighter than the {@link FaceAnnotation#POSITION_FIELD_NAME}, and encloses only the
     * skin part of the face. Typically, it is used to eliminate the face from any image analysis that detects the
     * "amount of skin" visible in an image. It is not based on the landmarker results, only on the initial face
     * detection, hence the <code>fd</code> (face detection) prefix.
     */
    public static final String FD_POSITION_FIELD_NAME = "fdPosition";

    /**
     * Detected face landmarks.
     */
    public static final String LANDMARKS_FIELD_NAME = "landmarks";

    public static final Schema SCHEMA = Schema.recordOf(
      "face-annotation-component-record",
      Schema.Field.of(ROLL_ANGLE_FIELD_NAME, Schema.of(Schema.Type.FLOAT)),
      Schema.Field.of(PAN_ANGLE_FIELD_NAME, Schema.of(Schema.Type.FLOAT)),
      Schema.Field.of(TILT_ANGLE_FIELD_NAME, Schema.of(Schema.Type.FLOAT)),
      Schema.Field.of(DETECTION_CONFIDENCE_FIELD_NAME, Schema.of(Schema.Type.FLOAT)),
      Schema.Field.of(LANDMARKING_CONFIDENCE_FIELD_NAME, Schema.of(Schema.Type.FLOAT)),
      Schema.Field.of(ANGER_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(JOY_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(SURPRISE_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(BLURRED_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(UNDER_EXPOSED_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(SORROW_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(HEADWEAR_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(POSITION_FIELD_NAME, Schema.arrayOf(Vertex.SCHEMA)),
      Schema.Field.of(FD_POSITION_FIELD_NAME, Schema.arrayOf(Vertex.SCHEMA)),
      Schema.Field.of(LANDMARKS_FIELD_NAME, Schema.arrayOf(FaceLandmark.SCHEMA))
    );
  }

  /**
   * {@link com.google.cloud.vision.v1.EntityAnnotation} mapped to a record with following fields.
   */
  public static class TextAnnotation {

    /**
     * The language code for the locale in which the entity textual {@value TextAnnotation#DESCRIPTION_FIELD_NAME} is
     * expressed.
     */
    public static final String LOCALE_FIELD_NAME = "locale";

    /**
     * Entity textual description, expressed in its {@value TextAnnotation#LOCALE_FIELD_NAME} language.
     */
    public static final String DESCRIPTION_FIELD_NAME = "description";

    /**
     * Image region to which this entity belongs.
     */
    public static final String POSITION_FIELD_NAME = "position";

    public static final Schema SCHEMA = Schema.recordOf(
      "text-annotation-component-record",
      Schema.Field.of(LOCALE_FIELD_NAME, Schema.nullableOf(Schema.of(Schema.Type.STRING))),
      Schema.Field.of(DESCRIPTION_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(POSITION_FIELD_NAME, Schema.arrayOf(Vertex.SCHEMA)));
  }

  /**
   * A single symbol representation. {@link com.google.cloud.vision.v1.Symbol} mapped to a record with following fields.
   */
  public static class TextSymbol {

    /**
     * The actual UTF-8 representation of the symbol.
     */
    public static final String TEXT_FIELD_NAME = "text";

    /**
     * Confidence of the OCR results for the symbol. Range [0, 1].
     */
    public static final String CONFIDENCE_FIELD_NAME = "confidence";

    /**
     * A list of detected languages together with confidence.
     */
    public static final String DETECTED_LANGUAGES_FIELD_NAME = "detectedLanguages";

    /**
     * Detected start or end of a text segment.
     */
    public static final String DETECTED_BREAK_FIELD_NAME = "detectedBreak";

    /**
     * The bounding box for the symbol. The vertices are in the order of top-left, top-right, bottom-right, bottom-left.
     * When a rotation of the bounding box is detected the rotation is represented as around the top-left corner as
     * defined when the text is read in the 'natural' orientation.
     * For example:
     * when the text is horizontal it might look like:
     * 0----1
     * |    |
     * 3----2
     * when it's rotated 180 degrees around the top-left corner it becomes:
     * 2----3
     * |    |
     * 1----0
     * and the vertice order will still be (0, 1, 2, 3).
     */
    public static final String BOUNDING_BOX_FIELD_NAME = "boundingBox";

    public static final Schema SCHEMA = Schema.recordOf(
      "document-text-page-symbol-record",
      Schema.Field.of(TEXT_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(CONFIDENCE_FIELD_NAME, Schema.of(Schema.Type.FLOAT)),
      Schema.Field.of(DETECTED_LANGUAGES_FIELD_NAME, Schema.nullableOf(Schema.arrayOf(DetectedLanguage.SCHEMA))),
      Schema.Field.of(DETECTED_BREAK_FIELD_NAME, Schema.nullableOf(Schema.of(Schema.Type.STRING))),
      Schema.Field.of(BOUNDING_BOX_FIELD_NAME, Schema.arrayOf(Vertex.SCHEMA)));
  }

  /**
   * A word representation. {@link com.google.cloud.vision.v1.Word} mapped to a record with following fields.
   */
  public static class TextWord {

    /**
     * The actual UTF-8 representation of the word.
     */
    public static final String TEXT_FIELD_NAME = "text";

    /**
     * Confidence of the OCR results for the word. Range [0, 1].
     */
    public static final String CONFIDENCE_FIELD_NAME = "confidence";

    /**
     * List of symbols in the word. The order of the symbols follows the natural reading order.
     */
    public static final String SYMBOLS_FIELD_NAME = "symbols";

    /**
     * A list of detected languages together with confidence.
     */
    public static final String DETECTED_LANGUAGES_FIELD_NAME = "detectedLanguages";

    /**
     * Detected start or end of a text segment.
     */
    public static final String DETECTED_BREAK_FIELD_NAME = "detectedBreak";

    /**
     * The bounding box for the word. The vertices are in the order of top-left, top-right, bottom-right, bottom-left.
     * When a rotation of the bounding box is detected the rotation is represented as around the top-left corner as
     * defined when the text is read in the 'natural' orientation.
     * For example:
     * when the text is horizontal it might look like:
     * 0----1
     * |    |
     * 3----2
     * when it's rotated 180 degrees around the top-left corner it becomes:
     * 2----3
     * |    |
     * 1----0
     * and the vertice order will still be (0, 1, 2, 3).
     */
    public static final String BOUNDING_BOX_FIELD_NAME = "boundingBox";

    public static final Schema SCHEMA = Schema.recordOf(
      "document-text-page-word-record",
      Schema.Field.of(TEXT_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(CONFIDENCE_FIELD_NAME, Schema.of(Schema.Type.FLOAT)),
      Schema.Field.of(SYMBOLS_FIELD_NAME, Schema.arrayOf(TextSymbol.SCHEMA)),
      Schema.Field.of(DETECTED_LANGUAGES_FIELD_NAME, Schema.nullableOf(Schema.arrayOf(DetectedLanguage.SCHEMA))),
      Schema.Field.of(DETECTED_BREAK_FIELD_NAME, Schema.nullableOf(Schema.of(Schema.Type.STRING))),
      Schema.Field.of(BOUNDING_BOX_FIELD_NAME, Schema.arrayOf(Vertex.SCHEMA)));
  }

  /**
   * A paragraph representation. {@link com.google.cloud.vision.v1.Paragraph} mapped to a record with following fields.
   */
  public static class TextParagraph {

    /**
     * The actual UTF-8 representation of the paragraph.
     */
    public static final String TEXT_FIELD_NAME = "text";

    /**
     * Confidence of the OCR results for the paragraph. Range [0, 1].
     */
    public static final String CONFIDENCE_FIELD_NAME = "confidence";

    /**
     * List of words in this paragraph.
     */
    public static final String WORDS_FIELD_NAME = "words";

    /**
     * A list of detected languages together with confidence.
     */
    public static final String DETECTED_LANGUAGES_FIELD_NAME = "detectedLanguages";

    /**
     * Detected start or end of a text segment.
     */
    public static final String DETECTED_BREAK_FIELD_NAME = "detectedBreak";

    /**
     * The bounding box for the paragraph. The vertices are in the order of top-left, top-right, bottom-right,
     * bottom-left. When a rotation of the bounding box is detected the rotation is represented as around the top-left
     * corner as defined when the text is read in the 'natural' orientation.
     * For example:
     * when the text is horizontal it might look like:
     * 0----1
     * |    |
     * 3----2
     * when it's rotated 180 degrees around the top-left corner it becomes:
     * 2----3
     * |    |
     * 1----0
     * and the vertice order will still be (0, 1, 2, 3).
     */
    public static final String BOUNDING_BOX_FIELD_NAME = "boundingBox";

    public static final Schema SCHEMA = Schema.recordOf(
      "document-text-page-paragraph-record",
      Schema.Field.of(TEXT_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(CONFIDENCE_FIELD_NAME, Schema.of(Schema.Type.FLOAT)),
      Schema.Field.of(WORDS_FIELD_NAME, Schema.arrayOf(TextWord.SCHEMA)),
      Schema.Field.of(DETECTED_LANGUAGES_FIELD_NAME, Schema.nullableOf(Schema.arrayOf(DetectedLanguage.SCHEMA))),
      Schema.Field.of(DETECTED_BREAK_FIELD_NAME, Schema.nullableOf(Schema.of(Schema.Type.STRING))),
      Schema.Field.of(BOUNDING_BOX_FIELD_NAME, Schema.arrayOf(Vertex.SCHEMA)));
  }

  /**
   * Logical element on the page. {@link com.google.cloud.vision.v1.Block} mapped to a record with following fields.
   */
  public static class TextBlock {

    /**
     * The actual UTF-8 representation of the block.
     */
    public static final String TEXT_FIELD_NAME = "text";

    /**
     * Detected block type (text, image etc) for this block.
     */
    public static final String BLOCK_TYPE_FIELD_NAME = "blockType";

    /**
     * Confidence of the OCR results of the block. Range [0, 1].
     */
    public static final String CONFIDENCE_FIELD_NAME = "confidence";

    /**
     * List of paragraphs in this block (if this blocks is of type text).
     */
    public static final String PARAGRAPHS_FIELD_NAME = "paragraphs";

    /**
     * A list of detected languages together with confidence.
     */
    public static final String DETECTED_LANGUAGES_FIELD_NAME = "detectedLanguages";

    /**
     * Detected start or end of a text segment.
     */
    public static final String DETECTED_BREAK_FIELD_NAME = "detectedBreak";

    /**
     * The bounding box for the block. The vertices are in the order of top-left, top-right, bottom-right, bottom-left.
     * When a rotation of the bounding box is detected the rotation is represented as around the top-left corner as
     * defined when the text is read in the 'natural' orientation.
     * For example:
     * when the text is horizontal it might look like:
     * 0----1
     * |    |
     * 3----2
     * when it's rotated 180 degrees around the top-left corner it becomes:
     * 2----3
     * |    |
     * 1----0
     * and the vertice order will still be (0, 1, 2, 3).
     */
    public static final String BOUNDING_BOX_FIELD_NAME = "boundingBox";

    public static final Schema SCHEMA = Schema.recordOf(
      "document-text-page-block-record",
      Schema.Field.of(TEXT_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(BLOCK_TYPE_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(CONFIDENCE_FIELD_NAME, Schema.of(Schema.Type.FLOAT)),
      Schema.Field.of(PARAGRAPHS_FIELD_NAME, Schema.arrayOf(TextParagraph.SCHEMA)),
      Schema.Field.of(DETECTED_LANGUAGES_FIELD_NAME, Schema.nullableOf(Schema.arrayOf(DetectedLanguage.SCHEMA))),
      Schema.Field.of(DETECTED_BREAK_FIELD_NAME, Schema.nullableOf(Schema.of(Schema.Type.STRING))),
      Schema.Field.of(BOUNDING_BOX_FIELD_NAME, Schema.arrayOf(Vertex.SCHEMA)));
  }

  /**
   * Detected page from OCR. {@link com.google.cloud.vision.v1.Page} mapped to a record with following fields.
   */
  public static class TextPage {

    /**
     * The actual UTF-8 representation of the page.
     */
    public static final String TEXT_FIELD_NAME = "text";

    /**
     * Page width. For PDFs the unit is points. For images (including TIFFs) the unit is pixels.
     */
    public static final String WIDTH_FIELD_NAME = "width";

    /**
     * Page height. For PDFs the unit is points. For images (including TIFFs) the unit is pixels.
     */
    public static final String HEIGHT_FIELD_NAME = "height";

    /**
     * Confidence of the OCR results on the page. Range [0, 1].
     */
    public static final String CONFIDENCE_FIELD_NAME = "confidence";

    /**
     * List of blocks of text, images etc on this page.
     */
    public static final String BLOCKS_FIELD_NAME = "blocks";

    /**
     * A list of detected languages together with confidence.
     */
    public static final String DETECTED_LANGUAGES_FIELD_NAME = "detectedLanguages";

    /**
     * Detected start or end of a text segment.
     */
    public static final String DETECTED_BREAK_FIELD_NAME = "detectedBreak";

    public static final Schema SCHEMA = Schema.recordOf(
      "document-text-page-record",
      Schema.Field.of(TEXT_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(WIDTH_FIELD_NAME, Schema.of(Schema.Type.INT)),
      Schema.Field.of(HEIGHT_FIELD_NAME, Schema.of(Schema.Type.INT)),
      Schema.Field.of(CONFIDENCE_FIELD_NAME, Schema.of(Schema.Type.FLOAT)),
      Schema.Field.of(BLOCKS_FIELD_NAME, Schema.arrayOf(TextBlock.SCHEMA)),
      Schema.Field.of(DETECTED_LANGUAGES_FIELD_NAME, Schema.nullableOf(Schema.arrayOf(DetectedLanguage.SCHEMA))),
      Schema.Field.of(DETECTED_BREAK_FIELD_NAME, Schema.nullableOf(Schema.of(Schema.Type.STRING))));
  }

  /**
   * Detected page from OCR. {@link com.google.cloud.vision.v1.TextAnnotation.DetectedLanguage} mapped to a record with
   * following fields.
   */
  public static class DetectedLanguage {

    /**
     * The BCP-47 language code, such as "en-US" or "sr-Latn". For more information, see
     * http://www.unicode.org/reports/tr35/#Unicode_locale_identifier.
     */
    public static final String CODE_FIELD_NAME = "code";

    /**
     * Confidence of detected language. Range [0, 1].
     */
    public static final String CONFIDENCE_FIELD_NAME = "confidence";

    public static final Schema SCHEMA = Schema.recordOf(
      "detected-language-record",
      Schema.Field.of(CODE_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(CONFIDENCE_FIELD_NAME, Schema.of(Schema.Type.FLOAT)));
  }

  /**
   * TextAnnotation contains a structured representation of OCR extracted text.
   * The hierarchy of an OCR extracted text structure is like this:
   * TextAnnotation -> Page -> Block -> Paragraph -> Word -> Symbol
   * Each structural component, starting from Page, may further have their own
   * properties.
   * {@link com.google.cloud.vision.v1.TextAnnotation} mapped to a record with following fields.
   */
  public static class FullTextAnnotation {

    /**
     * UTF-8 text detected on the pages.
     */
    public static final String TEXT_FIELD_NAME = "text";

    /**
     * List of pages detected by OCR.
     */
    public static final String PAGES_FIELD_NAME = "pages";

    public static final Schema SCHEMA = Schema.recordOf(
      "document-text-annotation-component-record",
      Schema.Field.of(TEXT_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(PAGES_FIELD_NAME, Schema.arrayOf(TextPage.SCHEMA)));
  }

  /**
   * Single crop hint that is used to generate a new crop when serving an image.
   * {@link com.google.cloud.vision.v1.CropHint} mapped to a record with following fields.
   */
  public static class CropHintAnnotation {

    /**
     * The bounding polygon for the crop region. The coordinates of the bounding box are in the original image's scale.
     */
    public static final String POSITION_FIELD_NAME = "position";

    /**
     * Confidence of this being a salient region.  Range [0, 1].
     */
    public static final String CONFIDENCE_FIELD_NAME = "confidence";

    /**
     * Fraction of importance of this salient region with respect to the original image.
     */
    public static final String IMPORTANCE_FRACTION_FIELD_NAME = "importanceFraction";

    public static final Schema SCHEMA = Schema.recordOf(
      "crop-hint-annotation-component-record",
      Schema.Field.of(POSITION_FIELD_NAME, Schema.arrayOf(Vertex.SCHEMA)),
      Schema.Field.of(CONFIDENCE_FIELD_NAME, Schema.of(Schema.Type.FLOAT)),
      Schema.Field.of(IMPORTANCE_FRACTION_FIELD_NAME, Schema.of(Schema.Type.FLOAT)));
  }

  /**
   * Detected entity location information.
   */
  public static class LocationInfo {

    /**
     * Latitude.
     */
    public static final String LATITUDE_FIELD_NAME = "latitude";

    /**
     * Longitude.
     */
    public static final String LONGITUDE_FIELD_NAME = "longitude";

    public static final Schema SCHEMA = Schema.recordOf(
      "location-info-record",
      Schema.Field.of(LATITUDE_FIELD_NAME, Schema.of(Schema.Type.DOUBLE)),
      Schema.Field.of(LONGITUDE_FIELD_NAME, Schema.of(Schema.Type.DOUBLE))
    );
  }

  /**
   * A Property consists of a user-supplied name/value pair.
   */
  public static class Property {

    /**
     * Name of the property.
     */
    public static final String NAME_FIELD_NAME = "name";

    /**
     * Value of the property.
     */
    public static final String VALUE_FIELD_NAME = "value";

    /**
     * Value of numeric properties.
     */
    public static final String UINT_64_VALUE_FIELD_NAME = "uint64Value";

    public static final Schema SCHEMA = Schema.recordOf(
      "property-record",
      Schema.Field.of(NAME_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(VALUE_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(UINT_64_VALUE_FIELD_NAME, Schema.of(Schema.Type.LONG))
    );
  }

  /**
   * Label detection results mapped to a record with following fields.
   */
  public static class LabelEntityAnnotation {

    /**
     * Opaque entity ID. Some IDs may be available in
     * <a href="https://developers.google.com/knowledge-graph/">Google Knowledge Graph Search API</a>
     */
    public static final String MID_FIELD_NAME = "mid";

    /**
     * The language code for the locale in which the entity textual description is expressed.
     */
    public static final String LOCALE_FIELD_NAME = "locale";

    /**
     * Entity textual description.
     */
    public static final String DESCRIPTION_FIELD_NAME = "description";

    /**
     * Overall score of the result. Range [0, 1].
     */
    public static final String SCORE_FIELD_NAME = "score";

    /**
     * The relevancy of the ICA (Image Content Annotation) label to the image. For example, the relevancy of "tower" is
     * likely higher to an image containing the detected "Eiffel Tower" than to an image containing a detected distant
     * towering building, even though the confidence that there is a tower in each image may be the same. Range [0, 1].
     */
    public static final String TOPICALITY_FIELD_NAME = "topicality";

    /**
     * The location information for the detected entity. Multiple location elements can be present because one
     * location may indicate the location of the scene in the image, and another location may indicate the location of
     * the place where the image was taken. Location information is usually present for landmarks.
     */
    public static final String LOCATIONS_FIELD_NAME = "locations";

    /**
     * Some entities may have optional user-supplied Property (name/value) fields, such a score or string that qualifies
     * the entity.
     */
    public static final String PROPERTIES_FIELD_NAME = "properties";

    public static final Schema SCHEMA = Schema.recordOf(
      "label-entity-annotation-component-record",
      Schema.Field.of(MID_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(LOCALE_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(DESCRIPTION_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(SCORE_FIELD_NAME, Schema.of(Schema.Type.FLOAT)),
      Schema.Field.of(TOPICALITY_FIELD_NAME, Schema.of(Schema.Type.FLOAT)),
      Schema.Field.of(LOCATIONS_FIELD_NAME, Schema.arrayOf(LocationInfo.SCHEMA)),
      Schema.Field.of(PROPERTIES_FIELD_NAME, Schema.arrayOf(Property.SCHEMA))
    );
  }

  /**
   * Landmark detection({@link ImageFeature#LANDMARKS}) results mapped to a record with following fields.
   */
  public static class LandmarkEntityAnnotation extends LabelEntityAnnotation {

    /**
     * Image region to which this entity belongs.
     */
    public static final String POSITION_FIELD_NAME = "position";

    public static final Schema SCHEMA = Schema.recordOf(
      "landmark-entity-annotation-component-record",
      Schema.Field.of(MID_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(LOCALE_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(DESCRIPTION_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(SCORE_FIELD_NAME, Schema.of(Schema.Type.FLOAT)),
      Schema.Field.of(TOPICALITY_FIELD_NAME, Schema.of(Schema.Type.FLOAT)),
      Schema.Field.of(LOCATIONS_FIELD_NAME, Schema.arrayOf(LocationInfo.SCHEMA)),
      Schema.Field.of(POSITION_FIELD_NAME, Schema.arrayOf(Vertex.SCHEMA)),
      Schema.Field.of(PROPERTIES_FIELD_NAME, Schema.arrayOf(Property.SCHEMA))
    );
  }

  /**
   * Color information consists of RGB channels, score, and the fraction of the image that the color occupies in the
   * image.
   */
  public static class ColorInfo {

    /**
     * The fraction of pixels the color occupies in the image. Value in range [0, 1].
     */
    public static final String PIXEL_FRACTION_FIELD_NAME = "pixelFraction";

    /**
     * Image-specific score for this color. Value in range [0, 1].
     */
    public static final String SCORE_FIELD_NAME = "score";

    /**
     * The amount of red in the color as a value in the interval [0, 1].
     */
    public static final String RED_FIELD_NAME = "red";

    /**
     * The amount of green in the color as a value in the interval [0, 1].
     */
    public static final String GREEN_FIELD_NAME = "green";

    /**
     * The amount of blue in the color as a value in the interval [0, 1].
     */
    public static final String BLUE_FIELD_NAME = "blue";

    /**
     * The fraction of this color that should be applied to the pixel.
     */
    public static final String ALPHA_FIELD_NAME = "alpha";

    public static final Schema SCHEMA = Schema.recordOf(
      "dominant-colors-annotation-component-record",
      Schema.Field.of(PIXEL_FRACTION_FIELD_NAME, Schema.of(Schema.Type.FLOAT)),
      Schema.Field.of(SCORE_FIELD_NAME, Schema.of(Schema.Type.FLOAT)),
      Schema.Field.of(RED_FIELD_NAME, Schema.of(Schema.Type.FLOAT)),
      Schema.Field.of(GREEN_FIELD_NAME, Schema.of(Schema.Type.FLOAT)),
      Schema.Field.of(BLUE_FIELD_NAME, Schema.of(Schema.Type.FLOAT)),
      Schema.Field.of(ALPHA_FIELD_NAME, Schema.of(Schema.Type.FLOAT)));
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

  /**
   * TODO document
   */
  public static class LocalizedObjectAnnotation {
    /**
     * TODO document
     */
    public static final String MID_FIELD_NAME = "mid";
    public static final String NAME_FIELD_NAME = "name";
    public static final String SCORE_FIELD_NAME = "score";
    public static final String POSITION_FIELD_NAME = "position";

    public static final Schema SCHEMA = Schema.recordOf(
      "localized-object-annotation-component-record",
      Schema.Field.of(MID_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(NAME_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(SCORE_FIELD_NAME, Schema.of(Schema.Type.FLOAT)),
      Schema.Field.of(POSITION_FIELD_NAME, Schema.arrayOf(Vertex.SCHEMA))
    );
  }

  /**
   * TODO document
   */
  public static class SafeSearchAnnotation {
    public static final String ADULT_FIELD_NAME = "adult";
    public static final String SPOOF_FIELD_NAME = "spoof";
    public static final String MEDICAL_FIELD_NAME = "medical";
    public static final String VIOLENCE_FIELD_NAME = "violence";
    public static final String RACY_FIELD_NAME = "racy";

    public static final Schema SCHEMA = Schema.recordOf(
      "safe-search-annotation-record",
      Schema.Field.of(ADULT_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(SPOOF_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(MEDICAL_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(VIOLENCE_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(RACY_FIELD_NAME, Schema.of(Schema.Type.STRING))
    );
  }

  /**
   * TODO document
   */
  public static class WebEntity {
    public static final String ENTITY_ID_FIELD_NAME = "entityId";
    public static final String SCORE_FIELD_NAME = "score";
    public static final String DESCRIPTION_FIELD_NAME = "description";

    public static final Schema SCHEMA = Schema.recordOf(
      "web-entity-record",
      Schema.Field.of(ENTITY_ID_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(SCORE_FIELD_NAME, Schema.of(Schema.Type.FLOAT)),
      Schema.Field.of(DESCRIPTION_FIELD_NAME, Schema.of(Schema.Type.STRING))
    );
  }

  /**
   * TODO document
   */
  public static class PageWithMatchingImages {
    public static final String URL_FIELD_NAME = "url";
    public static final String PAGE_TITLE_FIELD_NAME = "pageTitle";
    public static final String SCORE_FIELD_NAME = "score";
    public static final String FULL_MATCHING_IMAGES_FIELD_NAME = "fullMatchingImages";

    public static final Schema SCHEMA = Schema.recordOf(
      "page-with-matching-images-record",
      Schema.Field.of(URL_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(PAGE_TITLE_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(SCORE_FIELD_NAME, Schema.of(Schema.Type.FLOAT)),
      Schema.Field.of(FULL_MATCHING_IMAGES_FIELD_NAME, Schema.arrayOf(WebImage.SCHEMA))
    );
  }

  /**
   * TODO document
   */
  public static class BestGuessLabel {
    public static final String LABEL_FIELD_NAME = "label";
    public static final String LANGUAGE_CODE_FIELD_NAME = "languageCode";

    public static final Schema SCHEMA = Schema.recordOf(
      "best-guess-label-record",
      Schema.Field.of(LABEL_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(LANGUAGE_CODE_FIELD_NAME, Schema.of(Schema.Type.STRING))
    );
  }

  /**
   * TODO document
   */
  public static class WebImage {
    public static final String URL_FIELD_NAME = "url";
    public static final String SCORE_FIELD_NAME = "score";

    public static final Schema SCHEMA = Schema.recordOf(
      "web-image-record",
      Schema.Field.of(URL_FIELD_NAME, Schema.of(Schema.Type.STRING)),
      Schema.Field.of(SCORE_FIELD_NAME, Schema.of(Schema.Type.FLOAT))
    );
  }

  /**
   * TODO document
   */
  public static class WebDetection {
    public static final String ENTITIES_FIELD_NAME = "webEntities";
    public static final String FULL_MATCHING_IMAGES_FIELD_NAME = "fullMatchingImages";
    public static final String PARTIAL_MATCHING_IMAGES_FIELD_NAME = "partialMatchingImages";
    public static final String PAGES_WITH_MATCHING_IMAGES_FIELD_NAME = "pagesWithMatchingImages";
    public static final String VISUALLY_SIMILAR_IMAGES = "visuallySimilarImages";
    public static final String BEST_GUESS_LABELS_FIELD_NAME = "bestGuessLabels";

    public static final Schema SCHEMA = Schema.recordOf(
      "web-detection-record",
      Schema.Field.of(ENTITIES_FIELD_NAME, Schema.arrayOf(WebEntity.SCHEMA)),
      Schema.Field.of(FULL_MATCHING_IMAGES_FIELD_NAME, Schema.arrayOf(WebImage.SCHEMA)),
      Schema.Field.of(PARTIAL_MATCHING_IMAGES_FIELD_NAME, Schema.arrayOf(WebImage.SCHEMA)),
      Schema.Field.of(PAGES_WITH_MATCHING_IMAGES_FIELD_NAME, Schema.arrayOf(PageWithMatchingImages.SCHEMA)),
      Schema.Field.of(VISUALLY_SIMILAR_IMAGES, Schema.arrayOf(WebImage.SCHEMA)),
      Schema.Field.of(BEST_GUESS_LABELS_FIELD_NAME, Schema.arrayOf(BestGuessLabel.SCHEMA))
    );
  }
}
