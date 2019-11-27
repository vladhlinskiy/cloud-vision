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
import com.google.cloud.vision.v1.LocationInfo;
import com.google.cloud.vision.v1.Vertex;
import io.cdap.cdap.api.data.format.StructuredRecord;
import io.cdap.cdap.api.data.schema.Schema;
import io.cdap.plugin.cloud.vision.transform.ImageExtractorConstants;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Transforms landmark annotations of specified {@link AnnotateImageResponse} to {@link StructuredRecord} according to
 * the specified schema.
 */
public class LandmarkAnnotationsToRecordTransformer extends ImageAnnotationToRecordTransformer {

  public LandmarkAnnotationsToRecordTransformer(Schema schema, String outputFieldName) {
    super(schema, outputFieldName);
  }

  @Override
  public StructuredRecord transform(StructuredRecord input, AnnotateImageResponse annotateImageResponse) {
    return getOutputRecordBuilder(input)
      .set(outputFieldName, extractLandmarkAnnotations(annotateImageResponse))
      .build();
  }

  private List<StructuredRecord> extractLandmarkAnnotations(AnnotateImageResponse annotateImageResponse) {
    return annotateImageResponse.getLandmarkAnnotationsList().stream()
      .map(this::extractLandmarkAnnotationRecord)
      .collect(Collectors.toList());
  }

  private StructuredRecord extractLandmarkAnnotationRecord(EntityAnnotation annotation) {
    // here we retrieve landmark annotation schema instead of using constant schema since users are free to choose to not
    // include some of the fields
    Schema landmarkSchema = getLandmarkAnnotationSchema();
    StructuredRecord.Builder builder = StructuredRecord.builder(landmarkSchema);

    if (landmarkSchema.getField(ImageExtractorConstants.LandmarkAnnotation.MID_FIELD_NAME) != null) {
      builder.set(ImageExtractorConstants.LandmarkAnnotation.MID_FIELD_NAME, annotation.getMid());
    }
    if (landmarkSchema.getField(ImageExtractorConstants.LandmarkAnnotation.DESCRIPTION_FIELD_NAME) != null) {
      builder.set(ImageExtractorConstants.LandmarkAnnotation.DESCRIPTION_FIELD_NAME, annotation.getDescription());
    }
    if (landmarkSchema.getField(ImageExtractorConstants.LandmarkAnnotation.SCORE_FIELD_NAME) != null) {
      builder.set(ImageExtractorConstants.LandmarkAnnotation.SCORE_FIELD_NAME, annotation.getScore());
    }

    Schema.Field positionField = landmarkSchema.getField(ImageExtractorConstants.LandmarkAnnotation.POSITION_FIELD_NAME);
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
      builder.set(ImageExtractorConstants.LandmarkAnnotation.POSITION_FIELD_NAME, position);
    }

    Schema.Field locField = landmarkSchema.getField(ImageExtractorConstants.LandmarkAnnotation.LOCATION_INFO_FIELD_NAME);
    if (locField != null) {
      // here we retrieve schema instead of using constant schema since users are free to choose to not include some of
      // the fields
      Schema locationArraySchema = locField.getSchema().isNullable() ? locField.getSchema().getNonNullable()
        : locField.getSchema();
      Schema locationSchema = locationArraySchema.getComponentSchema().isNullable()
        ? locationArraySchema.getComponentSchema().getNonNullable()
        : locationArraySchema.getComponentSchema();

      List<StructuredRecord> location = annotation.getLocationsList().stream()
        .map(v -> extractLocation(v, locationSchema))
        .collect(Collectors.toList());
      builder.set(ImageExtractorConstants.LandmarkAnnotation.LOCATION_INFO_FIELD_NAME, location);
    }

    return builder.build();
  }

  private StructuredRecord extractLocation(LocationInfo locationInfo, Schema schema) {
    StructuredRecord.Builder builder = StructuredRecord.builder(schema);
    if (schema.getField(ImageExtractorConstants.LandmarkLocation.LATITUDE_FIELD_NAME) != null) {
      double latitude = locationInfo.getLatLng().getLatitude();
      builder.set(ImageExtractorConstants.LandmarkLocation.LATITUDE_FIELD_NAME, latitude);
    }
    if (schema.getField(ImageExtractorConstants.LandmarkLocation.LONGITUDE_FIELD_NAME) != null) {
      double longitude = locationInfo.getLatLng().getLongitude();
      builder.set(ImageExtractorConstants.LandmarkLocation.LONGITUDE_FIELD_NAME, longitude);
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
   * Retrieves Landmark Annotation's non-nullable component schema.
   *
   * @return Landmark Annotation's non-nullable component schema.
   */
  private Schema getLandmarkAnnotationSchema() {
    Schema landmarkAnnotationsFieldSchema = schema.getField(outputFieldName).getSchema();
    Schema landmarkAnnotationsComponentSchema = landmarkAnnotationsFieldSchema.isNullable()
      ? landmarkAnnotationsFieldSchema.getNonNullable().getComponentSchema()
      : landmarkAnnotationsFieldSchema.getComponentSchema();

    return landmarkAnnotationsComponentSchema.isNullable()
      ? landmarkAnnotationsComponentSchema.getNonNullable()
      : landmarkAnnotationsComponentSchema;
  }
}
