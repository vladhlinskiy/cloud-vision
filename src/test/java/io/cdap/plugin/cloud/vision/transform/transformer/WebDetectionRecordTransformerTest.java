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

package io.cdap.plugin.cloud.vision.transform.transformer;

import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.WebDetection;
import io.cdap.cdap.api.data.format.StructuredRecord;
import io.cdap.cdap.api.data.schema.Schema;
import io.cdap.plugin.cloud.vision.transform.ImageExtractorConstants;
import io.cdap.plugin.cloud.vision.transform.ImageFeature;
import org.junit.Assert;
import org.junit.Test;
import java.util.List;

/**
 * {@link WebDetectionToRecordTransformer} test.
 */
public class WebDetectionRecordTransformerTest extends BaseAnnotationsToRecordTransformerTest {

  private static final WebDetection.WebImage WEB_IMAGE_1 = WebDetection.WebImage.newBuilder()
    .setUrl("https://1000lugaresparair.files.wordpress.com/2017/11/quinten-de-graaf-278848.jpg")
    .setScore(0.442f)
    .build();

  private static final WebDetection.WebImage WEB_IMAGE_2 = WebDetection.WebImage.newBuilder()
    .setUrl("https://1000lugaresparair.files.wordpress.com/2017/11/278848.jpg")
    .setScore(0.442f)
    .build();

  private static final WebDetection.WebImage WEB_IMAGE_3 = WebDetection.WebImage.newBuilder()
    .setUrl("https://1000lugaresparair.com/some.jpg")
    .setScore(0.442f)
    .build();

  private static final WebDetection.WebPage WEB_PAGE = WebDetection.WebPage.newBuilder()
    .setUrl("https://1000lugaresparair.com/some-page")
    .setScore(0.95f)
    .setPageTitle("Page Title")
    .addFullMatchingImages(WEB_IMAGE_3)
    .addPartialMatchingImages(WEB_IMAGE_1)
    .build();

  private static final WebDetection.WebEntity WEB_ENTITY = WebDetection.WebEntity.newBuilder()
    .setEntityId("/m/02p7_j8")
    .setScore(1.442f)
    .setDescription("Carnival in Rio de Janeiro")
    .build();

  private static final WebDetection WEB_DETECTION = WebDetection.newBuilder()
    .addWebEntities(WEB_ENTITY)
    .addFullMatchingImages(WEB_IMAGE_1)
    .addPartialMatchingImages(WEB_IMAGE_2)
    .addVisuallySimilarImages(WEB_IMAGE_3)
    .addPagesWithMatchingImages(WEB_PAGE)
    .addBestGuessLabels(WebDetection.WebLabel.newBuilder().setLabel("Label").setLanguageCode("en"))
    .build();

  private static final AnnotateImageResponse RESPONSE = AnnotateImageResponse.newBuilder()
    .setWebDetection(WEB_DETECTION)
    .build();

  @Test
  @SuppressWarnings("ConstantConditions")
  public void testTransform() {
    String output = "extracted";
    Schema schema = Schema.recordOf("transformed-record-schema",
      Schema.Field.of("path", Schema.of(Schema.Type.STRING)),
      Schema.Field.of(output, ImageFeature.WEB_DETECTION.getSchema()));

    WebDetectionToRecordTransformer transformer = new WebDetectionToRecordTransformer(schema, output);
    StructuredRecord transformed = transformer.transform(INPUT_RECORD, RESPONSE);
    Assert.assertNotNull(transformed);
    StructuredRecord actual = transformed.get(output);
    assertWebDetectionEquals(WEB_DETECTION, actual);
  }

  @Test
  @SuppressWarnings("ConstantConditions")
  public void testTransformEmptyDetection() {
    String output = "extracted";
    Schema schema = Schema.recordOf("transformed-record-schema",
      Schema.Field.of("path", Schema.of(Schema.Type.STRING)),
      Schema.Field.of(output, ImageFeature.WEB_DETECTION.getSchema()));

    WebDetectionToRecordTransformer transformer = new WebDetectionToRecordTransformer(schema, output);

    WebDetection emptyWebDetection = WebDetection.newBuilder().build();
    AnnotateImageResponse response = AnnotateImageResponse.newBuilder()
      .setWebDetection(emptyWebDetection)
      .build();
    StructuredRecord transformed = transformer.transform(INPUT_RECORD, response);
    Assert.assertNotNull(transformed);
    StructuredRecord actual = transformed.get(output);
    assertWebDetectionEquals(emptyWebDetection, actual);
  }

  @Test
  @SuppressWarnings("ConstantConditions")
  public void testTransformSingleField() {
    String output = "extracted";
    Schema singleFieldSchema = Schema.recordOf("single-field", Schema.Field.of(
      ImageExtractorConstants.WebDetection.BEST_GUESS_LABELS_FIELD_NAME,
      Schema.arrayOf(ImageExtractorConstants.BestGuessLabel.SCHEMA)));

    Schema schema = Schema.recordOf("transformed-record-schema",
      Schema.Field.of("path", Schema.of(Schema.Type.STRING)),
      Schema.Field.of(output, singleFieldSchema));

    WebDetectionToRecordTransformer transformer = new WebDetectionToRecordTransformer(schema, output);
    StructuredRecord transformed = transformer.transform(INPUT_RECORD, RESPONSE);

    Assert.assertNotNull(transformed);
    StructuredRecord actual = transformed.get(output);
    // actual record has single-field schema
    Assert.assertEquals(singleFieldSchema, actual.getSchema());
    List<StructuredRecord> actualLabels = actual.get(ImageExtractorConstants.WebDetection.BEST_GUESS_LABELS_FIELD_NAME);
    for (int i = 0; i < WEB_DETECTION.getBestGuessLabelsList().size(); i++) {
      WebDetection.WebLabel expectedWebLabel = WEB_DETECTION.getBestGuessLabels(i);
      StructuredRecord actualWebLabel = actualLabels.get(i);
      assertWebLabelEquals(expectedWebLabel, actualWebLabel);
    }
  }

  private void assertWebDetectionEquals(WebDetection expected, StructuredRecord actual) {
    List<StructuredRecord> webEntities = actual.get(ImageExtractorConstants.WebDetection.ENTITIES_FIELD_NAME);
    Assert.assertNotNull(webEntities);
    Assert.assertEquals(expected.getWebEntitiesList().size(), webEntities.size());
    for (int i = 0; i < expected.getWebEntitiesList().size(); i++) {
      WebDetection.WebEntity expectedWebEntity = expected.getWebEntities(i);
      StructuredRecord actualWebEntity = webEntities.get(i);
      assertWebEntityEquals(expectedWebEntity, actualWebEntity);
    }

    List<StructuredRecord> fullMatchingImages =
      actual.get(ImageExtractorConstants.WebDetection.FULL_MATCHING_IMAGES_FIELD_NAME);
    Assert.assertNotNull(fullMatchingImages);
    Assert.assertEquals(expected.getFullMatchingImagesList().size(), fullMatchingImages.size());
    for (int i = 0; i < expected.getFullMatchingImagesList().size(); i++) {
      WebDetection.WebImage expectedWebImage = expected.getFullMatchingImages(i);
      StructuredRecord actualWebImage = fullMatchingImages.get(i);
      assertWebImageEquals(expectedWebImage, actualWebImage);
    }

    List<StructuredRecord> partialMatchingImages =
      actual.get(ImageExtractorConstants.WebDetection.PARTIAL_MATCHING_IMAGES_FIELD_NAME);
    Assert.assertNotNull(partialMatchingImages);
    Assert.assertEquals(expected.getPartialMatchingImagesList().size(), partialMatchingImages.size());
    for (int i = 0; i < expected.getPartialMatchingImagesList().size(); i++) {
      WebDetection.WebImage expectedWebImage = expected.getPartialMatchingImages(i);
      StructuredRecord actualWebImage = partialMatchingImages.get(i);
      assertWebImageEquals(expectedWebImage, actualWebImage);
    }

    List<StructuredRecord> visuallySimilarImages =
      actual.get(ImageExtractorConstants.WebDetection.VISUALLY_SIMILAR_IMAGES);
    Assert.assertNotNull(visuallySimilarImages);
    Assert.assertEquals(expected.getVisuallySimilarImagesList().size(), visuallySimilarImages.size());
    for (int i = 0; i < expected.getVisuallySimilarImagesList().size(); i++) {
      WebDetection.WebImage expectedWebImage = expected.getVisuallySimilarImages(i);
      StructuredRecord actualWebImage = visuallySimilarImages.get(i);
      assertWebImageEquals(expectedWebImage, actualWebImage);
    }

    List<StructuredRecord> bestGuessLabels =
      actual.get(ImageExtractorConstants.WebDetection.BEST_GUESS_LABELS_FIELD_NAME);
    Assert.assertNotNull(bestGuessLabels);
    Assert.assertEquals(expected.getBestGuessLabelsList().size(), bestGuessLabels.size());
    for (int i = 0; i < expected.getBestGuessLabelsList().size(); i++) {
      WebDetection.WebLabel expectedWebLabel = expected.getBestGuessLabels(i);
      StructuredRecord actualWebLabel = bestGuessLabels.get(i);
      assertWebLabelEquals(expectedWebLabel, actualWebLabel);
    }

    List<StructuredRecord> webPages =
      actual.get(ImageExtractorConstants.WebDetection.PAGES_WITH_MATCHING_IMAGES_FIELD_NAME);
    Assert.assertNotNull(webPages);
    Assert.assertEquals(expected.getPagesWithMatchingImagesList().size(), webPages.size());
    for (int i = 0; i < expected.getPagesWithMatchingImagesList().size(); i++) {
      WebDetection.WebPage expectedWebPage = expected.getPagesWithMatchingImages(i);
      StructuredRecord actualWebPage = webPages.get(i);
      assertWebPageEquals(expectedWebPage, actualWebPage);
    }
  }

  private void assertWebEntityEquals(WebDetection.WebEntity expected, StructuredRecord actual) {
    Assert.assertEquals(expected.getEntityId(), actual.get(ImageExtractorConstants.WebEntity.ENTITY_ID_FIELD_NAME));
    Assert.assertEquals(expected.getDescription(),
      actual.get(ImageExtractorConstants.WebEntity.DESCRIPTION_FIELD_NAME));
    Assert.assertEquals(expected.getScore(),
      actual.<Float>get(ImageExtractorConstants.WebEntity.SCORE_FIELD_NAME),
      DELTA);
  }

  private void assertWebImageEquals(WebDetection.WebImage expected, StructuredRecord actual) {
    Assert.assertEquals(expected.getUrl(), actual.get(ImageExtractorConstants.WebImage.URL_FIELD_NAME));
    Assert.assertEquals(expected.getScore(),
      actual.<Float>get(ImageExtractorConstants.WebImage.SCORE_FIELD_NAME),
      DELTA);
  }

  private void assertWebLabelEquals(WebDetection.WebLabel expected, StructuredRecord actual) {
    Assert.assertEquals(expected.getLabel(), actual.get(ImageExtractorConstants.BestGuessLabel.LABEL_FIELD_NAME));
    Assert.assertEquals(expected.getLanguageCode(),
      actual.get(ImageExtractorConstants.BestGuessLabel.LANGUAGE_CODE_FIELD_NAME));
  }

  private void assertWebPageEquals(WebDetection.WebPage expected, StructuredRecord actual) {
    Assert.assertEquals(expected.getUrl(), actual.get(ImageExtractorConstants.WebPage.URL_FIELD_NAME));
    Assert.assertEquals(expected.getPageTitle(), actual.get(ImageExtractorConstants.WebPage.PAGE_TITLE_FIELD_NAME));
    Assert.assertEquals(expected.getScore(),
      actual.<Float>get(ImageExtractorConstants.WebPage.SCORE_FIELD_NAME),
      DELTA);
    List<StructuredRecord> fullMatchingImages =
      actual.get(ImageExtractorConstants.WebPage.FULL_MATCHING_IMAGES_FIELD_NAME);
    Assert.assertNotNull(fullMatchingImages);
    Assert.assertEquals(expected.getFullMatchingImagesList().size(), fullMatchingImages.size());
    for (int i = 0; i < expected.getFullMatchingImagesList().size(); i++) {
      WebDetection.WebImage expectedWebImage = expected.getFullMatchingImages(i);
      StructuredRecord actualWebImage = fullMatchingImages.get(i);
      assertWebImageEquals(expectedWebImage, actualWebImage);
    }

    List<StructuredRecord> partialMatchingImages =
      actual.get(ImageExtractorConstants.WebPage.PARTIAL_MATCHING_IMAGES_FIELD_NAME);
    Assert.assertNotNull(partialMatchingImages);
    Assert.assertEquals(expected.getPartialMatchingImagesList().size(), partialMatchingImages.size());
    for (int i = 0; i < expected.getPartialMatchingImagesList().size(); i++) {
      WebDetection.WebImage expectedWebImage = expected.getPartialMatchingImages(i);
      StructuredRecord actualWebImage = partialMatchingImages.get(i);
      assertWebImageEquals(expectedWebImage, actualWebImage);
    }
  }
}
