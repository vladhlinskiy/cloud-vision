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

import java.util.List;
import java.util.Map;
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

  private List<Map<String, String>> extractLabelAnnotations(AnnotateImageResponse annotateImageResponse) {
    return annotateImageResponse.getLabelAnnotationsList().stream()
      .map(this::extractLabelAnnotation)
      .collect(Collectors.toList());
  }

  private Map<String, String> extractLabelAnnotation(EntityAnnotation annotation) {
    return annotation.getAllFields().entrySet().stream()
      .collect(Collectors.toMap(e -> e.getKey().getJsonName(), e -> e.getValue().toString()));
  }
}
