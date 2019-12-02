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
import com.google.cloud.vision.v1.Property;
import io.cdap.cdap.api.data.format.StructuredRecord;
import io.cdap.cdap.api.data.schema.Schema;
import io.cdap.plugin.cloud.vision.transform.ImageExtractorConstants;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Transforms label annotations of specified {@link AnnotateImageResponse} to {@link StructuredRecord} according to the
 * specified schema.
 */
public class LabelAnnotationsToRecordTransformer extends ImageAnnotationToRecordTransformer {

  public LabelAnnotationsToRecordTransformer(Schema schema, String outputFieldName) {
    super(schema, outputFieldName);
  }

  @Override
  public StructuredRecord transform(StructuredRecord input, AnnotateImageResponse annotateImageResponse) {
    return getOutputRecordBuilder(input)
      .set(outputFieldName, extractLabelAnnotations(annotateImageResponse))
      .build();
  }

  protected List<StructuredRecord> extractLabelAnnotations(AnnotateImageResponse annotateImageResponse) {
    return annotateImageResponse.getLabelAnnotationsList().stream()
      .map(this::extractAnnotation)
      .map(StructuredRecord.Builder::build)
      .collect(Collectors.toList());
  }

  protected StructuredRecord.Builder extractAnnotation(EntityAnnotation annotation) {
    Schema labelSchema = getEntityAnnotationSchema();
    StructuredRecord.Builder builder = StructuredRecord.builder(labelSchema);
    if (labelSchema.getField(ImageExtractorConstants.LabelEntityAnnotation.MID_FIELD_NAME) != null) {
      builder.set(ImageExtractorConstants.LabelEntityAnnotation.MID_FIELD_NAME, annotation.getMid());
    }
    if (labelSchema.getField(ImageExtractorConstants.LabelEntityAnnotation.LOCALE_FIELD_NAME) != null) {
      builder.set(ImageExtractorConstants.LabelEntityAnnotation.LOCALE_FIELD_NAME, annotation.getLocale());
    }
    if (labelSchema.getField(ImageExtractorConstants.LabelEntityAnnotation.DESCRIPTION_FIELD_NAME) != null) {
      builder.set(ImageExtractorConstants.LabelEntityAnnotation.DESCRIPTION_FIELD_NAME, annotation.getDescription());
    }
    if (labelSchema.getField(ImageExtractorConstants.LabelEntityAnnotation.SCORE_FIELD_NAME) != null) {
      builder.set(ImageExtractorConstants.LabelEntityAnnotation.SCORE_FIELD_NAME, annotation.getScore());
    }
    if (labelSchema.getField(ImageExtractorConstants.LabelEntityAnnotation.TOPICALITY_FIELD_NAME) != null) {
      builder.set(ImageExtractorConstants.LabelEntityAnnotation.TOPICALITY_FIELD_NAME, annotation.getTopicality());
    }
    Schema.Field locField = labelSchema.getField(ImageExtractorConstants.LabelEntityAnnotation.LOCATIONS_FIELD_NAME);
    if (locField != null) {
      Schema locationArraySchema = locField.getSchema().isNullable() ? locField.getSchema().getNonNullable()
        : locField.getSchema();
      Schema locationSchema = locationArraySchema.getComponentSchema().isNullable()
        ? locationArraySchema.getComponentSchema().getNonNullable()
        : locationArraySchema.getComponentSchema();

      List<StructuredRecord> location = annotation.getLocationsList().stream()
        .map(v -> extractLocation(v, locationSchema))
        .collect(Collectors.toList());
      builder.set(ImageExtractorConstants.LabelEntityAnnotation.LOCATIONS_FIELD_NAME, location);
    }
    Schema.Field propField = labelSchema.getField(ImageExtractorConstants.LabelEntityAnnotation.PROPERTIES_FIELD_NAME);
    if (propField != null) {
      Schema propertyArraySchema = propField.getSchema().isNullable() ? propField.getSchema().getNonNullable()
        : propField.getSchema();
      Schema propertySchema = propertyArraySchema.getComponentSchema().isNullable()
        ? propertyArraySchema.getComponentSchema().getNonNullable()
        : propertyArraySchema.getComponentSchema();

      List<StructuredRecord> location = annotation.getPropertiesList().stream()
        .map(v -> extractProperty(v, propertySchema))
        .collect(Collectors.toList());
      builder.set(ImageExtractorConstants.LabelEntityAnnotation.PROPERTIES_FIELD_NAME, location);
    }

    return builder;
  }

  protected StructuredRecord extractLocation(LocationInfo locationInfo, Schema schema) {
    StructuredRecord.Builder builder = StructuredRecord.builder(schema);
    if (schema.getField(ImageExtractorConstants.LocationInfo.LATITUDE_FIELD_NAME) != null) {
      double latitude = locationInfo.getLatLng().getLatitude();
      builder.set(ImageExtractorConstants.LocationInfo.LATITUDE_FIELD_NAME, latitude);
    }
    if (schema.getField(ImageExtractorConstants.LocationInfo.LONGITUDE_FIELD_NAME) != null) {
      double longitude = locationInfo.getLatLng().getLongitude();
      builder.set(ImageExtractorConstants.LocationInfo.LONGITUDE_FIELD_NAME, longitude);
    }

    return builder.build();
  }

  protected StructuredRecord extractProperty(Property property, Schema schema) {
    StructuredRecord.Builder builder = StructuredRecord.builder(schema);
    if (schema.getField(ImageExtractorConstants.Property.NAME_FIELD_NAME) != null) {
      builder.set(ImageExtractorConstants.Property.NAME_FIELD_NAME, property.getName());
    }
    if (schema.getField(ImageExtractorConstants.Property.VALUE_FIELD_NAME) != null) {
      builder.set(ImageExtractorConstants.Property.VALUE_FIELD_NAME, property.getValue());
    }
    if (schema.getField(ImageExtractorConstants.Property.UINT_64_VALUE_FIELD_NAME) != null) {
      builder.set(ImageExtractorConstants.Property.UINT_64_VALUE_FIELD_NAME, property.getUint64Value());
    }

    return builder.build();
  }

  /**
   * Retrieves Entity Annotation's non-nullable component schema. Entity Annotation's schema is retrieved instead of
   * using constant schema since users are free to choose to not include some of the fields.
   *
   * @return Entity Annotation's non-nullable component schema.
   */
  protected Schema getEntityAnnotationSchema() {
    Schema labelAnnotationsFieldSchema = schema.getField(outputFieldName).getSchema();
    Schema labelAnnotationsComponentSchema = labelAnnotationsFieldSchema.isNullable()
      ? labelAnnotationsFieldSchema.getNonNullable().getComponentSchema()
      : labelAnnotationsFieldSchema.getComponentSchema();

    return labelAnnotationsComponentSchema.isNullable()
      ? labelAnnotationsComponentSchema.getNonNullable()
      : labelAnnotationsComponentSchema;
  }
}
