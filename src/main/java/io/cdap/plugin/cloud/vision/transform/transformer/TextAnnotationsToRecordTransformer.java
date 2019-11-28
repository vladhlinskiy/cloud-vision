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

import java.util.List;
import java.util.stream.Collectors;


/**
 * Transforms text annotations of specified {@link AnnotateImageResponse} to {@link StructuredRecord} according to
 * the specified schema.
 */
public class TextAnnotationsToRecordTransformer extends ImageAnnotationToRecordTransformer {

  public TextAnnotationsToRecordTransformer(Schema schema, String outputFieldName) {
    super(schema, outputFieldName);
  }

  @Override
  public StructuredRecord transform(StructuredRecord input, AnnotateImageResponse annotateImageResponse) {
    return getOutputRecordBuilder(input)
      .set(outputFieldName, extractTextAnnotations(annotateImageResponse))
      .build();
  }

  private List<StructuredRecord> extractTextAnnotations(AnnotateImageResponse annotateImageResponse) {
    return annotateImageResponse.getTextAnnotationsList().stream()
      .map(this::extractTextAnnotationRecord)
      .collect(Collectors.toList());
  }

  private StructuredRecord extractTextAnnotationRecord(EntityAnnotation annotation) {
    Schema textSchema = getTextAnnotationSchema();
    StructuredRecord.Builder builder = StructuredRecord.builder(textSchema);
    if (textSchema.getField(ImageExtractorConstants.TextAnnotation.LOCALE_FIELD_NAME) != null) {
      builder.set(ImageExtractorConstants.TextAnnotation.LOCALE_FIELD_NAME, annotation.getLocale());
    }
    if (textSchema.getField(ImageExtractorConstants.TextAnnotation.DESCRIPTION_FIELD_NAME) != null) {
      builder.set(ImageExtractorConstants.TextAnnotation.DESCRIPTION_FIELD_NAME, annotation.getDescription());
    }
    Schema.Field positionField = textSchema.getField(ImageExtractorConstants.TextAnnotation.POSITION_FIELD_NAME);
    if (positionField != null) {
      Schema positionArraySchema = positionField.getSchema().isNullable() ? positionField.getSchema().getNonNullable()
        : positionField.getSchema();
      Schema positionSchema = positionArraySchema.getComponentSchema().isNullable()
        ? positionArraySchema.getComponentSchema().getNonNullable()
        : positionArraySchema.getComponentSchema();

      List<StructuredRecord> position = annotation.getBoundingPoly().getVerticesList().stream()
        .map(v -> extractVertex(v, positionSchema))
        .collect(Collectors.toList());
      builder.set(ImageExtractorConstants.TextAnnotation.POSITION_FIELD_NAME, position);
    }

    return builder.build();
  }

  /**
   * Retrieves Text Annotation's non-nullable component schema. Text Annotation's schema is retrieved instead of using
   * constant schema since users are free to choose to not include some of the fields.
   *
   * @return Text Annotation's non-nullable component schema.
   */
  private Schema getTextAnnotationSchema() {
    Schema textAnnotationsFieldSchema = schema.getField(outputFieldName).getSchema();
    Schema textAnnotationsComponentSchema = textAnnotationsFieldSchema.isNullable()
      ? textAnnotationsFieldSchema.getNonNullable().getComponentSchema()
      : textAnnotationsFieldSchema.getComponentSchema();

    return textAnnotationsComponentSchema.isNullable()
      ? textAnnotationsComponentSchema.getNonNullable()
      : textAnnotationsComponentSchema;
  }
}
