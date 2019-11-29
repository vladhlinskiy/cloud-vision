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

  private List<StructuredRecord> extractLabelAnnotations(AnnotateImageResponse annotateImageResponse) {
    return annotateImageResponse.getLabelAnnotationsList().stream()
      .map(this::extractLabelAnnotation)
      .collect(Collectors.toList());
  }

  private StructuredRecord extractLabelAnnotation(EntityAnnotation annotation) {
    Schema labelSchema = getLabelAnnotationSchema();
    StructuredRecord.Builder builder = StructuredRecord.builder(labelSchema);
    if (labelSchema.getField(ImageExtractorConstants.LabelAnnotation.MID_FIELD_NAME) != null) {
      builder.set(ImageExtractorConstants.LabelAnnotation.MID_FIELD_NAME, annotation.getMid());
    }
    if (labelSchema.getField(ImageExtractorConstants.LabelAnnotation.DESCRIPTION_FIELD_NAME) != null) {
      builder.set(ImageExtractorConstants.LabelAnnotation.DESCRIPTION_FIELD_NAME, annotation.getDescription());
    }
    if (labelSchema.getField(ImageExtractorConstants.LabelAnnotation.SCORE_FIELD_NAME) != null) {
      builder.set(ImageExtractorConstants.LabelAnnotation.SCORE_FIELD_NAME, annotation.getScore());
    }
    if (labelSchema.getField(ImageExtractorConstants.LabelAnnotation.TOPICALITY_FIELD_NAME) != null) {
      builder.set(ImageExtractorConstants.LabelAnnotation.TOPICALITY_FIELD_NAME, annotation.getTopicality());
    }

    return builder.build();
  }


  /**
   * Retrieves Label Annotation's non-nullable component schema. Label Annotation's schema is retrieved instead of using
   * constant schema since users are free to choose to not include some of the fields.
   *
   * @return Label Annotation's non-nullable component schema.
   */
  private Schema getLabelAnnotationSchema() {
    Schema labelAnnotationsFieldSchema = schema.getField(outputFieldName).getSchema();
    Schema labelAnnotationsComponentSchema = labelAnnotationsFieldSchema.isNullable()
      ? labelAnnotationsFieldSchema.getNonNullable().getComponentSchema()
      : labelAnnotationsFieldSchema.getComponentSchema();

    return labelAnnotationsComponentSchema.isNullable()
      ? labelAnnotationsComponentSchema.getNonNullable()
      : labelAnnotationsComponentSchema;
  }
}
