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
import com.google.cloud.vision.v1.EntityAnnotation;
import io.cdap.cdap.api.data.format.StructuredRecord;
import io.cdap.cdap.api.data.schema.Schema;
import io.cdap.plugin.cloud.vision.transform.ImageExtractorConstants;
import io.cdap.plugin.cloud.vision.transform.ImageFeature;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * {@link LabelAnnotationsToRecordTransformer} test.
 */
public class LabelAnnotationsToRecordTransformerTest extends BaseAnnotationsToRecordTransformerTest {

  private static final EntityAnnotation LABEL_ANNOTATION = EntityAnnotation.newBuilder()
    .setMid("/m/0dx1j")
    .setDescription("Some Label")
    .setScore(0.87f)
    .setTopicality(0.21f)
    .build();

  private static final AnnotateImageResponse RESPONSE = AnnotateImageResponse.newBuilder()
    .addLabelAnnotations(LABEL_ANNOTATION)
    .build();

  @Test
  @SuppressWarnings("ConstantConditions")
  public void testTransform() {
    String outputFieldName = "extracted";
    Schema schema = Schema.recordOf("transformed-record-schema",
                                    Schema.Field.of("path", Schema.of(Schema.Type.STRING)),
                                    Schema.Field.of(outputFieldName, ImageFeature.LABELS.getSchema()));

    LabelAnnotationsToRecordTransformer transformer = new LabelAnnotationsToRecordTransformer(schema, outputFieldName);
    StructuredRecord transformed = transformer.transform(INPUT_RECORD, RESPONSE);

    Assert.assertNotNull(transformed);
    List<StructuredRecord> actualExtracted = transformed.get(outputFieldName);
    Assert.assertNotNull(actualExtracted);
    Assert.assertEquals(1, actualExtracted.size());
    StructuredRecord actual = actualExtracted.get(SINGLE_FEATURE_INDEX);
    assertAnnotationEquals(LABEL_ANNOTATION, actual);
  }

  @Test
  @SuppressWarnings("ConstantConditions")
  public void testTransformEmptyAnnotation() {
    String outputFieldName = "extracted";
    Schema schema = Schema.recordOf("transformed-record-schema",
                                    Schema.Field.of("path", Schema.of(Schema.Type.STRING)),
                                    Schema.Field.of(outputFieldName, ImageFeature.LABELS.getSchema()));

    LabelAnnotationsToRecordTransformer transformer = new LabelAnnotationsToRecordTransformer(schema, outputFieldName);

    EntityAnnotation emptyAnnotation = EntityAnnotation.newBuilder().build();
    AnnotateImageResponse emptyLabelAnnotation = AnnotateImageResponse.newBuilder()
      .addLabelAnnotations(emptyAnnotation)
      .build();
    StructuredRecord transformed = transformer.transform(INPUT_RECORD, emptyLabelAnnotation);

    Assert.assertNotNull(transformed);
    List<StructuredRecord> actualExtracted = transformed.get(outputFieldName);
    Assert.assertNotNull(actualExtracted);
    Assert.assertEquals(1, actualExtracted.size());
    StructuredRecord actual = actualExtracted.get(SINGLE_FEATURE_INDEX);
    assertAnnotationEquals(emptyAnnotation, actual);
  }

  @Test
  @SuppressWarnings("ConstantConditions")
  public void testTransformSingleField() {
    String outputFieldName = "extracted";
    Schema labelAnnotationSingleFieldSchema = Schema.recordOf(
      "single-label-field",
      Schema.Field.of(ImageExtractorConstants.LabelAnnotation.DESCRIPTION_FIELD_NAME, Schema.of(Schema.Type.STRING)));
    Schema schema = Schema.recordOf("transformed-record-schema",
                                    Schema.Field.of("path", Schema.of(Schema.Type.STRING)),
                                    Schema.Field.of(outputFieldName, Schema.arrayOf(labelAnnotationSingleFieldSchema)));

    LabelAnnotationsToRecordTransformer transformer = new LabelAnnotationsToRecordTransformer(schema, outputFieldName);
    StructuredRecord transformed = transformer.transform(INPUT_RECORD, RESPONSE);

    Assert.assertNotNull(transformed);
    List<StructuredRecord> actualExtracted = transformed.get(outputFieldName);
    Assert.assertNotNull(actualExtracted);
    Assert.assertEquals(1, actualExtracted.size());
    StructuredRecord actual = actualExtracted.get(SINGLE_FEATURE_INDEX);
    // actual record has single-field schema
    Assert.assertEquals(labelAnnotationSingleFieldSchema, actual.getSchema());
    Assert.assertEquals(LABEL_ANNOTATION.getDescription(),
                        actual.get(ImageExtractorConstants.LabelAnnotation.DESCRIPTION_FIELD_NAME));
  }

  private void assertAnnotationEquals(EntityAnnotation expected, StructuredRecord actual) {
    Assert.assertEquals(expected.getMid(), actual.get(ImageExtractorConstants.LabelAnnotation.MID_FIELD_NAME));
    Assert.assertEquals(expected.getDescription(),
                        actual.get(ImageExtractorConstants.LabelAnnotation.DESCRIPTION_FIELD_NAME));
    Assert.assertEquals(expected.getScore(),
                        actual.<Float>get(ImageExtractorConstants.LabelAnnotation.SCORE_FIELD_NAME),
                        DELTA);
    Assert.assertEquals(expected.getTopicality(),
                        actual.<Float>get(ImageExtractorConstants.LabelAnnotation.TOPICALITY_FIELD_NAME),
                        DELTA);
  }
}
