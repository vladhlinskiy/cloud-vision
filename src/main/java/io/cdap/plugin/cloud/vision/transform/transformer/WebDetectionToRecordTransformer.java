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
import java.util.List;
import java.util.stream.Collectors;


/**
 * Transforms web detection of specified {@link AnnotateImageResponse} to {@link StructuredRecord} according
 * to the specified schema.
 */
public class WebDetectionToRecordTransformer extends ImageAnnotationToRecordTransformer {

  public WebDetectionToRecordTransformer(Schema schema, String outputFieldName) {
    super(schema, outputFieldName);
  }

  @Override
  public StructuredRecord transform(StructuredRecord input, AnnotateImageResponse annotateImageResponse) {
    WebDetection webDetection = annotateImageResponse.getWebDetection();
    return getOutputRecordBuilder(input)
      .set(outputFieldName, extractWebDetection(webDetection))
      .build();
  }

  private StructuredRecord extractWebDetection(WebDetection webDetection) {
    Schema webDetectionSchema = getWebDetectionSchema();
    StructuredRecord.Builder builder = StructuredRecord.builder(webDetectionSchema);

    Schema.Field entitiesField = webDetectionSchema.getField(ImageExtractorConstants.WebDetection.ENTITIES_FIELD_NAME);
    if (entitiesField != null) {
      Schema entitiesArraySchema = entitiesField.getSchema().isNullable() ? entitiesField.getSchema().getNonNullable()
        : entitiesField.getSchema();
      Schema entitySchema = entitiesArraySchema.getComponentSchema().isNullable()
        ? entitiesArraySchema.getComponentSchema().getNonNullable()
        : entitiesArraySchema.getComponentSchema();

      List<StructuredRecord> entities = webDetection.getWebEntitiesList().stream()
        .map(v -> extractEntity(v, entitySchema))
        .collect(Collectors.toList());
      builder.set(ImageExtractorConstants.WebDetection.ENTITIES_FIELD_NAME, entities);
    }

    Schema.Field fullMatchingImagesField =
      webDetectionSchema.getField(ImageExtractorConstants.WebDetection.FULL_MATCHING_IMAGES_FIELD_NAME);
    if (fullMatchingImagesField != null) {
      Schema fullMatchingImagesArraySchema = fullMatchingImagesField.getSchema().isNullable()
        ? fullMatchingImagesField.getSchema().getNonNullable()
        : fullMatchingImagesField.getSchema();
      Schema webImageSchema = fullMatchingImagesArraySchema.getComponentSchema().isNullable()
        ? fullMatchingImagesArraySchema.getComponentSchema().getNonNullable()
        : fullMatchingImagesArraySchema.getComponentSchema();

      List<StructuredRecord> webImages = webDetection.getFullMatchingImagesList().stream()
        .map(v -> extractWebImage(v, webImageSchema))
        .collect(Collectors.toList());
      builder.set(ImageExtractorConstants.WebDetection.FULL_MATCHING_IMAGES_FIELD_NAME, webImages);
    }

    Schema.Field partialMatchingImagesField =
      webDetectionSchema.getField(ImageExtractorConstants.WebDetection.PARTIAL_MATCHING_IMAGES_FIELD_NAME);
    if (partialMatchingImagesField != null) {
      Schema partialMatchingImagesArraySchema = partialMatchingImagesField.getSchema().isNullable()
        ? partialMatchingImagesField.getSchema().getNonNullable()
        : partialMatchingImagesField.getSchema();
      Schema webImageSchema = partialMatchingImagesArraySchema.getComponentSchema().isNullable()
        ? partialMatchingImagesArraySchema.getComponentSchema().getNonNullable()
        : partialMatchingImagesArraySchema.getComponentSchema();

      List<StructuredRecord> webImages = webDetection.getPartialMatchingImagesList().stream()
        .map(v -> extractWebImage(v, webImageSchema))
        .collect(Collectors.toList());
      builder.set(ImageExtractorConstants.WebDetection.PARTIAL_MATCHING_IMAGES_FIELD_NAME, webImages);
    }

    Schema.Field pagesField =
      webDetectionSchema.getField(ImageExtractorConstants.WebDetection.PAGES_WITH_MATCHING_IMAGES_FIELD_NAME);
    if (pagesField != null) {
      Schema pagesArraySchema = pagesField.getSchema().isNullable()
        ? pagesField.getSchema().getNonNullable()
        : pagesField.getSchema();
      Schema pageSchema = pagesArraySchema.getComponentSchema().isNullable()
        ? pagesArraySchema.getComponentSchema().getNonNullable()
        : pagesArraySchema.getComponentSchema();

      List<StructuredRecord> pages = webDetection.getPagesWithMatchingImagesList().stream()
        .map(v -> extractWebPage(v, pageSchema))
        .collect(Collectors.toList());
      builder.set(ImageExtractorConstants.WebDetection.PAGES_WITH_MATCHING_IMAGES_FIELD_NAME, pages);
    }

    Schema.Field visuallySimilarImagesField =
      webDetectionSchema.getField(ImageExtractorConstants.WebDetection.VISUALLY_SIMILAR_IMAGES);
    if (visuallySimilarImagesField != null) {
      Schema visuallySimilarImagesArraySchema = visuallySimilarImagesField.getSchema().isNullable()
        ? visuallySimilarImagesField.getSchema().getNonNullable()
        : visuallySimilarImagesField.getSchema();
      Schema webImageSchema = visuallySimilarImagesArraySchema.getComponentSchema().isNullable()
        ? visuallySimilarImagesArraySchema.getComponentSchema().getNonNullable()
        : visuallySimilarImagesArraySchema.getComponentSchema();

      List<StructuredRecord> webImages = webDetection.getVisuallySimilarImagesList().stream()
        .map(v -> extractWebImage(v, webImageSchema))
        .collect(Collectors.toList());
      builder.set(ImageExtractorConstants.WebDetection.VISUALLY_SIMILAR_IMAGES, webImages);
    }
    Schema.Field labelsField =
      webDetectionSchema.getField(ImageExtractorConstants.WebDetection.BEST_GUESS_LABELS_FIELD_NAME);
    if (labelsField != null) {
      Schema labelsArraySchema = labelsField.getSchema().isNullable()
        ? labelsField.getSchema().getNonNullable()
        : labelsField.getSchema();
      Schema labelSchema = labelsArraySchema.getComponentSchema().isNullable()
        ? labelsArraySchema.getComponentSchema().getNonNullable()
        : labelsArraySchema.getComponentSchema();

      List<StructuredRecord> webLabels = webDetection.getBestGuessLabelsList().stream()
        .map(v -> extractWebLabel(v, labelSchema))
        .collect(Collectors.toList());
      builder.set(ImageExtractorConstants.WebDetection.BEST_GUESS_LABELS_FIELD_NAME, webLabels);
    }

    return builder.build();
  }

  private StructuredRecord extractEntity(WebDetection.WebEntity webEntity, Schema schema) {
    StructuredRecord.Builder builder = StructuredRecord.builder(schema);
    if (schema.getField(ImageExtractorConstants.WebEntity.ENTITY_ID_FIELD_NAME) != null) {
      builder.set(ImageExtractorConstants.WebEntity.ENTITY_ID_FIELD_NAME, webEntity.getEntityId());
    }
    if (schema.getField(ImageExtractorConstants.WebEntity.SCORE_FIELD_NAME) != null) {
      builder.set(ImageExtractorConstants.WebEntity.SCORE_FIELD_NAME, webEntity.getScore());
    }
    if (schema.getField(ImageExtractorConstants.WebEntity.DESCRIPTION_FIELD_NAME) != null) {
      builder.set(ImageExtractorConstants.WebEntity.DESCRIPTION_FIELD_NAME, webEntity.getDescription());
    }

    return builder.build();
  }

  private StructuredRecord extractWebImage(WebDetection.WebImage webImage, Schema schema) {
    StructuredRecord.Builder builder = StructuredRecord.builder(schema);
    if (schema.getField(ImageExtractorConstants.WebImage.URL_FIELD_NAME) != null) {
      builder.set(ImageExtractorConstants.WebImage.URL_FIELD_NAME, webImage.getUrl());
    }
    if (schema.getField(ImageExtractorConstants.WebImage.SCORE_FIELD_NAME) != null) {
      builder.set(ImageExtractorConstants.WebImage.SCORE_FIELD_NAME, webImage.getScore());
    }

    return builder.build();
  }

  private StructuredRecord extractWebLabel(WebDetection.WebLabel webLabel, Schema schema) {
    StructuredRecord.Builder builder = StructuredRecord.builder(schema);
    if (schema.getField(ImageExtractorConstants.BestGuessLabel.LABEL_FIELD_NAME) != null) {
      builder.set(ImageExtractorConstants.BestGuessLabel.LABEL_FIELD_NAME, webLabel.getLabel());
    }
    if (schema.getField(ImageExtractorConstants.BestGuessLabel.LANGUAGE_CODE_FIELD_NAME) != null) {
      builder.set(ImageExtractorConstants.BestGuessLabel.LANGUAGE_CODE_FIELD_NAME, webLabel.getLanguageCode());
    }

    return builder.build();
  }

  private StructuredRecord extractWebPage(WebDetection.WebPage webPage, Schema schema) {
    StructuredRecord.Builder builder = StructuredRecord.builder(schema);
    if (schema.getField(ImageExtractorConstants.WebPage.URL_FIELD_NAME) != null) {
      builder.set(ImageExtractorConstants.WebPage.URL_FIELD_NAME, webPage.getUrl());
    }
    if (schema.getField(ImageExtractorConstants.WebPage.PAGE_TITLE_FIELD_NAME) != null) {
      builder.set(ImageExtractorConstants.WebPage.PAGE_TITLE_FIELD_NAME, webPage.getPageTitle());
    }
    if (schema.getField(ImageExtractorConstants.WebPage.SCORE_FIELD_NAME) != null) {
      builder.set(ImageExtractorConstants.WebPage.SCORE_FIELD_NAME, webPage.getScore());
    }

    Schema.Field fullMatchingImagesField =
      schema.getField(ImageExtractorConstants.WebPage.FULL_MATCHING_IMAGES_FIELD_NAME);
    if (fullMatchingImagesField != null) {
      Schema fullMatchingImagesArraySchema = fullMatchingImagesField.getSchema().isNullable()
        ? fullMatchingImagesField.getSchema().getNonNullable()
        : fullMatchingImagesField.getSchema();
      Schema webImageSchema = fullMatchingImagesArraySchema.getComponentSchema().isNullable()
        ? fullMatchingImagesArraySchema.getComponentSchema().getNonNullable()
        : fullMatchingImagesArraySchema.getComponentSchema();

      List<StructuredRecord> webImages = webPage.getFullMatchingImagesList().stream()
        .map(v -> extractWebImage(v, webImageSchema))
        .collect(Collectors.toList());
      builder.set(ImageExtractorConstants.WebPage.FULL_MATCHING_IMAGES_FIELD_NAME, webImages);
    }

    Schema.Field partialMatchingImagesField =
      schema.getField(ImageExtractorConstants.WebPage.PARTIAL_MATCHING_IMAGES_FIELD_NAME);
    if (partialMatchingImagesField != null) {
      Schema partialMatchingImagesArraySchema = partialMatchingImagesField.getSchema().isNullable()
        ? partialMatchingImagesField.getSchema().getNonNullable()
        : partialMatchingImagesField.getSchema();
      Schema webImageSchema = partialMatchingImagesArraySchema.getComponentSchema().isNullable()
        ? partialMatchingImagesArraySchema.getComponentSchema().getNonNullable()
        : partialMatchingImagesArraySchema.getComponentSchema();

      List<StructuredRecord> webImages = webPage.getPartialMatchingImagesList().stream()
        .map(v -> extractWebImage(v, webImageSchema))
        .collect(Collectors.toList());
      builder.set(ImageExtractorConstants.WebPage.PARTIAL_MATCHING_IMAGES_FIELD_NAME, webImages);
    }

    return builder.build();
  }

  /**
   * Retrieves Web Detection's non-nullable component schema. Schema retrieved instead of using constant schema
   * since users are free to choose to not include some of the fields
   *
   * @return Web Detection's non-nullable component schema.
   */
  private Schema getWebDetectionSchema() {
    Schema handwritingAnnotationsFieldSchema = schema.getField(outputFieldName).getSchema();
    return handwritingAnnotationsFieldSchema.isNullable() ? handwritingAnnotationsFieldSchema.getNonNullable()
      : handwritingAnnotationsFieldSchema;
  }
}
