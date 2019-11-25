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
import com.google.cloud.vision.v1.Block;
import com.google.cloud.vision.v1.Page;
import com.google.cloud.vision.v1.Paragraph;
import com.google.cloud.vision.v1.Symbol;
import com.google.cloud.vision.v1.TextAnnotation;
import com.google.cloud.vision.v1.Word;
import io.cdap.cdap.api.data.format.StructuredRecord;
import io.cdap.cdap.api.data.schema.Schema;
import io.cdap.plugin.cloud.vision.transform.ImageExtractorConstants;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Transforms handwriting annotation of specified {@link AnnotateImageResponse} to {@link StructuredRecord} according
 * to the specified schema.
 */
public class DocumentTextAnnotationsToRecordTransformer extends ImageAnnotationToRecordTransformer {

  public DocumentTextAnnotationsToRecordTransformer(Schema schema, String outputFieldName) {
    super(schema, outputFieldName);
  }

  @Override
  public StructuredRecord transform(StructuredRecord input, AnnotateImageResponse annotateImageResponse) {
    TextAnnotation annotation = annotateImageResponse.getFullTextAnnotation();
    return getOutputRecordBuilder(input)
      .set(outputFieldName, extractHandwritingAnnotation(annotation))
      .build();
  }

  private StructuredRecord extractHandwritingAnnotation(TextAnnotation annotation) {
    // here we retrieve handwriting annotation schema instead of using constant schema since users are free to choose
    // to not include some of the fields
    Schema hwSchema = getHandwritingAnnotationSchema();
    StructuredRecord.Builder builder = StructuredRecord.builder(hwSchema);
    if (hwSchema.getField(ImageExtractorConstants.HandwritingAnnotation.TEXT_FIELD_NAME) != null) {
      builder.set(ImageExtractorConstants.HandwritingAnnotation.TEXT_FIELD_NAME, annotation.getText());
    }

    Schema.Field pagesField = hwSchema.getField(ImageExtractorConstants.HandwritingAnnotation.PAGES_FIELD_NAME);
    if (pagesField != null) {
      // here we retrieve schema instead of using constant schema since users are free to choose to not include some of
      // the fields
      Schema pagesArraySchema = pagesField.getSchema().isNullable() ? pagesField.getSchema().getNonNullable()
        : pagesField.getSchema();
      Schema pageSchema = pagesArraySchema.getComponentSchema().isNullable()
        ? pagesArraySchema.getComponentSchema().getNonNullable()
        : pagesArraySchema.getComponentSchema();

      List<StructuredRecord> pages = annotation.getPagesList().stream()
        .map(v -> extractPage(v, pageSchema))
        .collect(Collectors.toList());
      builder.set(ImageExtractorConstants.HandwritingAnnotation.PAGES_FIELD_NAME, pages);
    }

    return builder.build();
  }

  private StructuredRecord extractPage(Page page, Schema schema) {
    StructuredRecord.Builder builder = StructuredRecord.builder(schema);
    if (schema.getField(ImageExtractorConstants.TextPage.TEXT_FIELD_NAME) != null) {
      String pageText = page.getBlocksList().stream()
        .map(Block::getParagraphsList)
        .flatMap(List::stream)
        .map(Paragraph::getWordsList)
        .flatMap(List::stream)
        .map(Word::getSymbolsList)
        .flatMap(List::stream)
        .map(Symbol::getText)
        .collect(Collectors.joining());
      builder.set(ImageExtractorConstants.TextPage.TEXT_FIELD_NAME, pageText);
    }
    Schema.Field blocksField = schema.getField(ImageExtractorConstants.TextPage.BLOCKS_FIELD_NAME);
    if (blocksField != null) {
      // here we retrieve schema instead of using constant schema since users are free to choose to not include some of
      // the fields
      Schema blocksArraySchema = blocksField.getSchema().isNullable() ? blocksField.getSchema().getNonNullable()
        : blocksField.getSchema();
      Schema blockSchema = blocksArraySchema.getComponentSchema().isNullable()
        ? blocksArraySchema.getComponentSchema().getNonNullable()
        : blocksArraySchema.getComponentSchema();
      List<StructuredRecord> blocks = page.getBlocksList().stream()
        .map(v -> extractBlock(v, blockSchema))
        .collect(Collectors.toList());
      builder.set(ImageExtractorConstants.TextPage.BLOCKS_FIELD_NAME, blocks);
    }

    return builder.build();
  }

  private StructuredRecord extractBlock(Block block, Schema schema) {
    StructuredRecord.Builder builder = StructuredRecord.builder(schema);
    if (schema.getField(ImageExtractorConstants.TextBlock.TEXT_FIELD_NAME) != null) {
      String blockText = block.getParagraphsList().stream()
        .map(Paragraph::getWordsList)
        .flatMap(List::stream)
        .map(Word::getSymbolsList)
        .flatMap(List::stream)
        .map(Symbol::getText)
        .collect(Collectors.joining());
      builder.set(ImageExtractorConstants.TextBlock.TEXT_FIELD_NAME, blockText);
    }
    Schema.Field paragraphsField = schema.getField(ImageExtractorConstants.TextBlock.PARAGRAPHS_FIELD_NAME);
    if (paragraphsField != null) {
      // here we retrieve schema instead of using constant schema since users are free to choose to not include some of
      // the fields
      Schema paragraphsArraySchema = paragraphsField.getSchema().isNullable()
        ? paragraphsField.getSchema().getNonNullable()
        : paragraphsField.getSchema();
      Schema paragraphSchema = paragraphsArraySchema.getComponentSchema().isNullable()
        ? paragraphsArraySchema.getComponentSchema().getNonNullable()
        : paragraphsArraySchema.getComponentSchema();
      List<StructuredRecord> paragraphs = block.getParagraphsList().stream()
        .map(v -> extractParagraph(v, paragraphSchema))
        .collect(Collectors.toList());
      builder.set(ImageExtractorConstants.TextBlock.PARAGRAPHS_FIELD_NAME, paragraphs);
    }

    return builder.build();
  }

  private StructuredRecord extractParagraph(Paragraph paragraph, Schema schema) {
    StructuredRecord.Builder builder = StructuredRecord.builder(schema);
    if (schema.getField(ImageExtractorConstants.TextParagraph.TEXT_FIELD_NAME) != null) {
      String paragraphText = paragraph.getWordsList().stream()
        .map(Word::getSymbolsList)
        .flatMap(List::stream)
        .map(Symbol::getText)
        .collect(Collectors.joining());
      builder.set(ImageExtractorConstants.TextParagraph.TEXT_FIELD_NAME, paragraphText);
    }
    if (schema.getField(ImageExtractorConstants.TextParagraph.CONFIDENCE_FIELD_NAME) != null) {
      builder.set(ImageExtractorConstants.TextParagraph.CONFIDENCE_FIELD_NAME, paragraph.getConfidence());
    }
    Schema.Field wordsField = schema.getField(ImageExtractorConstants.TextParagraph.WORDS_FIELD_NAME);
    if (wordsField != null) {
      // here we retrieve schema instead of using constant schema since users are free to choose to not include some of
      // the fields
      Schema wordsArraySchema = wordsField.getSchema().isNullable() ? wordsField.getSchema().getNonNullable()
        : wordsField.getSchema();
      Schema wordSchema = wordsArraySchema.getComponentSchema().isNullable()
        ? wordsArraySchema.getComponentSchema().getNonNullable()
        : wordsArraySchema.getComponentSchema();
      List<StructuredRecord> words = paragraph.getWordsList().stream()
        .map(v -> extractWord(v, wordSchema))
        .collect(Collectors.toList());
      builder.set(ImageExtractorConstants.TextParagraph.WORDS_FIELD_NAME, words);
    }

    return builder.build();
  }

  private StructuredRecord extractWord(Word word, Schema schema) {
    StructuredRecord.Builder builder = StructuredRecord.builder(schema);
    if (schema.getField(ImageExtractorConstants.TextWord.TEXT_FIELD_NAME) != null) {
      String paragraphText = word.getSymbolsList().stream()
        .map(Symbol::getText)
        .collect(Collectors.joining());
      builder.set(ImageExtractorConstants.TextWord.TEXT_FIELD_NAME, paragraphText);
    }
    if (schema.getField(ImageExtractorConstants.TextWord.CONFIDENCE_FIELD_NAME) != null) {
      builder.set(ImageExtractorConstants.TextWord.CONFIDENCE_FIELD_NAME, word.getConfidence());
    }
    Schema.Field symbolsField = schema.getField(ImageExtractorConstants.TextWord.SYMBOLS_FIELD_NAME);
    if (symbolsField != null) {
      // here we retrieve schema instead of using constant schema since users are free to choose to not include some of
      // the fields
      Schema symbolsArraySchema = symbolsField.getSchema().isNullable() ? symbolsField.getSchema().getNonNullable()
        : symbolsField.getSchema();
      Schema symbolSchema = symbolsArraySchema.getComponentSchema().isNullable()
        ? symbolsArraySchema.getComponentSchema().getNonNullable()
        : symbolsArraySchema.getComponentSchema();
      List<StructuredRecord> words = word.getSymbolsList().stream()
        .map(v -> extractSymbol(v, symbolSchema))
        .collect(Collectors.toList());
      builder.set(ImageExtractorConstants.TextWord.SYMBOLS_FIELD_NAME, words);
    }

    return builder.build();
  }

  private StructuredRecord extractSymbol(Symbol symbol, Schema schema) {
    StructuredRecord.Builder builder = StructuredRecord.builder(schema);
    if (schema.getField(ImageExtractorConstants.TextSymbol.TEXT_FIELD_NAME) != null) {
      builder.set(ImageExtractorConstants.TextSymbol.TEXT_FIELD_NAME, symbol.getText());
    }
    if (schema.getField(ImageExtractorConstants.TextSymbol.CONFIDENCE_FIELD_NAME) != null) {
      builder.set(ImageExtractorConstants.TextSymbol.CONFIDENCE_FIELD_NAME, symbol.getConfidence());
    }

    return builder.build();
  }

  /**
   * Retrieves Handwriting Annotation's non-nullable component schema.
   *
   * @return Handwriting Annotation's non-nullable component schema.
   */
  private Schema getHandwritingAnnotationSchema() {
    Schema handwritingAnnotationsFieldSchema = schema.getField(outputFieldName).getSchema();
    return handwritingAnnotationsFieldSchema.isNullable() ? handwritingAnnotationsFieldSchema.getNonNullable()
      : handwritingAnnotationsFieldSchema;
  }
}
