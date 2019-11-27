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
import com.google.cloud.vision.v1.FaceAnnotation;
import com.google.cloud.vision.v1.Vertex;
import io.cdap.cdap.api.data.format.StructuredRecord;
import io.cdap.cdap.api.data.schema.Schema;
import io.cdap.plugin.cloud.vision.transform.ImageExtractorConstants;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Transforms face annotations of specified {@link AnnotateImageResponse} to {@link StructuredRecord} according to
 * the specified schema.
 */
public class FaceAnnotationsToRecordTransformer extends ImageAnnotationToRecordTransformer {

  public FaceAnnotationsToRecordTransformer(Schema schema, String outputFieldName) {
    super(schema, outputFieldName);
  }

  @Override
  public StructuredRecord transform(StructuredRecord input, AnnotateImageResponse annotateImageResponse) {
    return getOutputRecordBuilder(input)
      .set(outputFieldName, extractFaceAnnotations(annotateImageResponse))
      .build();
  }

  private List<StructuredRecord> extractFaceAnnotations(AnnotateImageResponse annotateImageResponse) {
    return annotateImageResponse.getFaceAnnotationsList().stream()
      .map(this::extractFaceAnnotationRecord)
      .collect(Collectors.toList());
  }

  private StructuredRecord extractFaceAnnotationRecord(FaceAnnotation annotation) {
    // here we retrieve face annotation schema instead of using constant schema since users are free to choose to not
    // include some of the fields
    Schema faceSchema = getFaceAnnotationSchema();
    StructuredRecord.Builder builder = StructuredRecord.builder(faceSchema);

    if (faceSchema.getField(ImageExtractorConstants.FaceAnnotation.ANGER_FIELD_NAME) != null) {
      builder.set(ImageExtractorConstants.FaceAnnotation.ANGER_FIELD_NAME, annotation.getAngerLikelihood().name());
    }
    if (faceSchema.getField(ImageExtractorConstants.FaceAnnotation.JOY_FIELD_NAME) != null) {
      builder.set(ImageExtractorConstants.FaceAnnotation.JOY_FIELD_NAME, annotation.getJoyLikelihood().name());
    }
    if (faceSchema.getField(ImageExtractorConstants.FaceAnnotation.SURPRISE_FIELD_NAME) != null) {
      String surprise = annotation.getSurpriseLikelihood().name();
      builder.set(ImageExtractorConstants.FaceAnnotation.SURPRISE_FIELD_NAME, surprise);
    }
    Schema.Field positionField = faceSchema.getField(ImageExtractorConstants.FaceAnnotation.POSITION_FIELD_NAME);
    if (positionField != null) {
      // here we retrieve schema instead of using constant schema since users are free to choose to not include some of
      // the fields
      Schema positionArraySchema = positionField.getSchema().isNullable() ? positionField.getSchema().getNonNullable()
        : positionField.getSchema();
      Schema positionSchema = positionArraySchema.getComponentSchema().isNullable()
        ? positionArraySchema.getComponentSchema().getNonNullable()
        : positionArraySchema.getComponentSchema();

      List<StructuredRecord> position = annotation.getBoundingPoly().getVerticesList().stream()
        .map(v -> extractVertex(v, positionSchema))
        .collect(Collectors.toList());
      builder.set(ImageExtractorConstants.FaceAnnotation.POSITION_FIELD_NAME, position);
    }

    return builder.build();
  }

  private StructuredRecord extractVertex(Vertex vertex, Schema schema) {
    StructuredRecord.Builder builder = StructuredRecord.builder(schema);
    if (schema.getField(ImageExtractorConstants.Vertex.X_FIELD_NAME) != null) {
      builder.set(ImageExtractorConstants.Vertex.X_FIELD_NAME, vertex.getX());
    }
    if (schema.getField(ImageExtractorConstants.Vertex.Y_FIELD_NAME) != null) {
      builder.set(ImageExtractorConstants.Vertex.Y_FIELD_NAME, vertex.getY());
    }

    return builder.build();
  }

  /**
   * Retrieves Face Annotation's non-nullable component schema.
   *
   * @return Face Annotation's non-nullable component schema.
   */
  private Schema getFaceAnnotationSchema() {
    Schema faceAnnotationsFieldSchema = schema.getField(outputFieldName).getSchema();
    Schema faceAnnotationsComponentSchema = faceAnnotationsFieldSchema.isNullable()
      ? faceAnnotationsFieldSchema.getNonNullable().getComponentSchema()
      : faceAnnotationsFieldSchema.getComponentSchema();

    return faceAnnotationsComponentSchema.isNullable()
      ? faceAnnotationsComponentSchema.getNonNullable()
      : faceAnnotationsComponentSchema;
  }
}
