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
import com.google.cloud.vision.v1.LocalizedObjectAnnotation;
import io.cdap.cdap.api.data.format.StructuredRecord;
import io.cdap.cdap.api.data.schema.Schema;
import io.cdap.plugin.cloud.vision.transform.ImageExtractorConstants;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Transforms localized object annotations of specified {@link AnnotateImageResponse} to {@link StructuredRecord}
 * according to the specified schema.
 */
public class LocalizedObjectAnnotationsToRecordTransformer extends ImageAnnotationToRecordTransformer {

  public LocalizedObjectAnnotationsToRecordTransformer(Schema schema, String outputFieldName) {
    super(schema, outputFieldName);
  }

  @Override
  public StructuredRecord transform(StructuredRecord input, AnnotateImageResponse annotateImageResponse) {
    return getOutputRecordBuilder(input)
      .set(outputFieldName, extractLocalizedObjectAnnotations(annotateImageResponse))
      .build();
  }

  private List<StructuredRecord> extractLocalizedObjectAnnotations(AnnotateImageResponse annotateImageResponse) {
    return annotateImageResponse.getLocalizedObjectAnnotationsList().stream()
      .map(this::extractLocalizedObjectAnnotationRecord)
      .collect(Collectors.toList());
  }

  private StructuredRecord extractLocalizedObjectAnnotationRecord(LocalizedObjectAnnotation annotation) {
    // here we retrieve localized object annotation schema instead of using constant schema since users are free to
    // choose to not include some of the fields
    Schema objSchema = getLocalizedObjectAnnotationSchema();
    StructuredRecord.Builder builder = StructuredRecord.builder(objSchema);

    if (objSchema.getField(ImageExtractorConstants.LocalizedObjectAnnotation.MID_FIELD_NAME) != null) {
      builder.set(ImageExtractorConstants.LocalizedObjectAnnotation.MID_FIELD_NAME, annotation.getMid());
    }
    if (objSchema.getField(ImageExtractorConstants.LocalizedObjectAnnotation.NAME_FIELD_NAME) != null) {
      builder.set(ImageExtractorConstants.LocalizedObjectAnnotation.NAME_FIELD_NAME, annotation.getName());
    }
    if (objSchema.getField(ImageExtractorConstants.LocalizedObjectAnnotation.SCORE_FIELD_NAME) != null) {
      builder.set(ImageExtractorConstants.LocalizedObjectAnnotation.SCORE_FIELD_NAME, annotation.getScore());
    }

    Schema.Field posField = objSchema.getField(ImageExtractorConstants.LocalizedObjectAnnotation.POSITION_FIELD_NAME);
    if (posField != null) {
      // here we retrieve schema instead of using constant schema since users are free to choose to not include some of
      // the fields
      Schema positionArraySchema = posField.getSchema().isNullable() ? posField.getSchema().getNonNullable()
        : posField.getSchema();
      Schema positionSchema = positionArraySchema.getComponentSchema().isNullable()
        ? positionArraySchema.getComponentSchema().getNonNullable()
        : positionArraySchema.getComponentSchema();

      List<StructuredRecord> position = annotation.getBoundingPoly().getVerticesList().stream()
        .map(v -> extractVertex(v, positionSchema))
        .collect(Collectors.toList());
      builder.set(ImageExtractorConstants.LocalizedObjectAnnotation.POSITION_FIELD_NAME, position);
    }

    return builder.build();
  }

  /**
   * Retrieves Localized Object Annotation's non-nullable component schema.
   *
   * @return Localized Object Annotation's non-nullable component schema.
   */
  private Schema getLocalizedObjectAnnotationSchema() {
    Schema localizedObjectAnnotationsFieldSchema = schema.getField(outputFieldName).getSchema();
    Schema localizedObjectAnnotationsComponentSchema = localizedObjectAnnotationsFieldSchema.isNullable()
      ? localizedObjectAnnotationsFieldSchema.getNonNullable().getComponentSchema()
      : localizedObjectAnnotationsFieldSchema.getComponentSchema();

    return localizedObjectAnnotationsComponentSchema.isNullable()
      ? localizedObjectAnnotationsComponentSchema.getNonNullable()
      : localizedObjectAnnotationsComponentSchema;
  }
}
