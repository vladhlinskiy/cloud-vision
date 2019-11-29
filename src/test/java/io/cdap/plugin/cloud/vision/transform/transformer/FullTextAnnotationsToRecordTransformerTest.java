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
import io.cdap.plugin.cloud.vision.transform.ImageFeature;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * {@link FullTextAnnotationsToRecordTransformer} test.
 */
public class FullTextAnnotationsToRecordTransformerTest extends BaseAnnotationsToRecordTransformerTest {

  private static final Symbol SYMBOL = Symbol.newBuilder()
    .setText("A")
    .setConfidence(0.99f)
    .setBoundingBox(POSITION)
    .setProperty(
      TextAnnotation.TextProperty.newBuilder()
        .addDetectedLanguages(TextAnnotation.DetectedLanguage.newBuilder().setConfidence(0.5f).setLanguageCode("en"))
    )
    .build();

  private static final Word WORD = Word.newBuilder()
    .addSymbols(SYMBOL)
    .setConfidence(0.89f)
    .setBoundingBox(POSITION)
    .setProperty(
      TextAnnotation.TextProperty.newBuilder()
        .addDetectedLanguages(TextAnnotation.DetectedLanguage.newBuilder().setConfidence(0.7f).setLanguageCode("ru"))
    )
    .build();

  private static final Paragraph PARAGRAPH = Paragraph.newBuilder()
    .addWords(WORD)
    .setConfidence(0.79f)
    .setBoundingBox(POSITION)
    .setProperty(
      TextAnnotation.TextProperty.newBuilder()
        .addDetectedLanguages(TextAnnotation.DetectedLanguage.newBuilder().setConfidence(0.6f).setLanguageCode("ru"))
    )
    .build();

  private static final Block BLOCK = Block.newBuilder()
    .addParagraphs(PARAGRAPH)
    .setConfidence(0.69f)
    .setBoundingBox(POSITION)
    .build();

  private static final Page PAGE = Page.newBuilder()
    .addBlocks(BLOCK)
    .setConfidence(0.49f)
    .build();

  private static final TextAnnotation TEXT_ANNOTATION = TextAnnotation.newBuilder()
    .addPages(PAGE)
    .setText("Some Text")
    .build();

  private static final AnnotateImageResponse RESPONSE = AnnotateImageResponse.newBuilder()
    .setFullTextAnnotation(TEXT_ANNOTATION)
    .build();

  @Test
  @SuppressWarnings("ConstantConditions")
  public void testTransform() {
    String output = "extracted";
    Schema schema = Schema.recordOf("transformed-record-schema",
                                    Schema.Field.of("path", Schema.of(Schema.Type.STRING)),
                                    Schema.Field.of(output, ImageFeature.HANDWRITING.getSchema()));

    FullTextAnnotationsToRecordTransformer transformer = new FullTextAnnotationsToRecordTransformer(schema, output);
    StructuredRecord transformed = transformer.transform(INPUT_RECORD, RESPONSE);
    Assert.assertNotNull(transformed);
    StructuredRecord actual = transformed.get(output);
    assertAnnotationEquals(TEXT_ANNOTATION, actual);
  }

  @Test
  @SuppressWarnings("ConstantConditions")
  public void testTransformEmptyAnnotation() {
    String output = "extracted";
    Schema schema = Schema.recordOf("transformed-record-schema",
                                    Schema.Field.of("path", Schema.of(Schema.Type.STRING)),
                                    Schema.Field.of(output, ImageFeature.HANDWRITING.getSchema()));

    FullTextAnnotationsToRecordTransformer transformer = new FullTextAnnotationsToRecordTransformer(schema, output);

    TextAnnotation emptyAnnotation = TextAnnotation.newBuilder().build();
    AnnotateImageResponse emptyTextAnnotation = AnnotateImageResponse.newBuilder()
      .setFullTextAnnotation(emptyAnnotation)
      .build();
    StructuredRecord transformed = transformer.transform(INPUT_RECORD, emptyTextAnnotation);
    Assert.assertNotNull(transformed);
    StructuredRecord actual = transformed.get(output);
    assertAnnotationEquals(emptyAnnotation, actual);
  }

  @Test
  @SuppressWarnings("ConstantConditions")
  public void testTransformSingleField() {
    String output = "extracted";
    Schema textAnnotationSingleFieldSchema = Schema.recordOf(
      "single-text-field",
      Schema.Field.of(ImageExtractorConstants.FullTextAnnotation.TEXT_FIELD_NAME, Schema.of(Schema.Type.STRING)));
    Schema schema = Schema.recordOf("transformed-record-schema",
                                    Schema.Field.of("path", Schema.of(Schema.Type.STRING)),
                                    Schema.Field.of(output, textAnnotationSingleFieldSchema));

    FullTextAnnotationsToRecordTransformer transformer = new FullTextAnnotationsToRecordTransformer(schema, output);
    StructuredRecord transformed = transformer.transform(INPUT_RECORD, RESPONSE);
    Assert.assertNotNull(transformed);
    StructuredRecord actual = transformed.get(output);
    // actual record has single-field schema
    Assert.assertEquals(textAnnotationSingleFieldSchema, actual.getSchema());
    Assert.assertEquals(TEXT_ANNOTATION.getText(),
                        actual.get(ImageExtractorConstants.FullTextAnnotation.TEXT_FIELD_NAME));
  }

  private void assertAnnotationEquals(TextAnnotation expected, StructuredRecord actual) {
    Assert.assertEquals(expected.getText(), actual.get(ImageExtractorConstants.FullTextAnnotation.TEXT_FIELD_NAME));

    List<StructuredRecord> pages = actual.get(ImageExtractorConstants.FullTextAnnotation.PAGES_FIELD_NAME);
    Assert.assertNotNull(pages);
    Assert.assertEquals(expected.getPagesList().size(), pages.size());
    for (int i = 0; i < expected.getPagesList().size(); i++) {
      Page expectedPage = expected.getPages(i);
      StructuredRecord actualPage = pages.get(i);
      assertPageEquals(expectedPage, actualPage);
    }
  }

  private void assertPageEquals(Page expected, StructuredRecord actual) {
    Assert.assertEquals(expected.getConfidence(),
                        actual.<Float>get(ImageExtractorConstants.TextPage.CONFIDENCE_FIELD_NAME),
                        DELTA);

    List<StructuredRecord> blocks = actual.get(ImageExtractorConstants.TextPage.BLOCKS_FIELD_NAME);
    Assert.assertNotNull(blocks);
    Assert.assertEquals(expected.getBlocksList().size(), blocks.size());
    for (int i = 0; i < expected.getBlocksList().size(); i++) {
      Block expectedBlock = expected.getBlocks(i);
      StructuredRecord actualPage = blocks.get(i);
      assertBlockEquals(expectedBlock, actualPage);
    }

    List<StructuredRecord> languages = actual.get(ImageExtractorConstants.TextPage.DETECTED_LANGUAGES_FIELD_NAME);
    Assert.assertNotNull(languages);
    Assert.assertEquals(expected.getProperty().getDetectedLanguagesList().size(), languages.size());
    for (int i = 0; i < expected.getProperty().getDetectedLanguagesList().size(); i++) {
      TextAnnotation.DetectedLanguage expectedLanguage = expected.getProperty().getDetectedLanguages(i);
      StructuredRecord actualLanguage = languages.get(i);
      assertLanguageEquals(expectedLanguage, actualLanguage);
    }
    String expectedBreak = expected.getProperty().getDetectedBreak().getType().name();
    Assert.assertEquals(expectedBreak, actual.get(ImageExtractorConstants.TextPage.DETECTED_BREAK_FIELD_NAME));
  }

  private void assertLanguageEquals(TextAnnotation.DetectedLanguage expected, StructuredRecord actual) {
    Assert.assertEquals(expected.getConfidence(),
                        actual.<Float>get(ImageExtractorConstants.DetectedLanguage.CONFIDENCE_FIELD_NAME),
                        DELTA);
    Assert.assertEquals(expected.getLanguageCode(),
                        actual.get(ImageExtractorConstants.DetectedLanguage.CODE_FIELD_NAME));
  }

  private void assertBlockEquals(Block expected, StructuredRecord actual) {
    Assert.assertEquals(expected.getConfidence(),
                        actual.<Float>get(ImageExtractorConstants.TextBlock.CONFIDENCE_FIELD_NAME),
                        DELTA);

    List<StructuredRecord> paragraphs = actual.get(ImageExtractorConstants.TextBlock.PARAGRAPHS_FIELD_NAME);
    Assert.assertNotNull(paragraphs);
    Assert.assertEquals(expected.getParagraphsList().size(), paragraphs.size());
    for (int i = 0; i < expected.getParagraphsList().size(); i++) {
      Paragraph expectedParagraph = expected.getParagraphs(i);
      StructuredRecord actualParagraph = paragraphs.get(i);
      assertParagraphEquals(expectedParagraph, actualParagraph);
    }

    List<StructuredRecord> languages = actual.get(ImageExtractorConstants.TextBlock.DETECTED_LANGUAGES_FIELD_NAME);
    Assert.assertNotNull(languages);
    Assert.assertEquals(expected.getProperty().getDetectedLanguagesList().size(), languages.size());
    for (int i = 0; i < expected.getProperty().getDetectedLanguagesList().size(); i++) {
      TextAnnotation.DetectedLanguage expectedLanguage = expected.getProperty().getDetectedLanguages(i);
      StructuredRecord actualLanguage = languages.get(i);
      assertLanguageEquals(expectedLanguage, actualLanguage);
    }
    String expectedBreak = expected.getProperty().getDetectedBreak().getType().name();
    Assert.assertEquals(expectedBreak, actual.get(ImageExtractorConstants.TextBlock.DETECTED_BREAK_FIELD_NAME));
  }

  private void assertParagraphEquals(Paragraph expected, StructuredRecord actual) {
    Assert.assertEquals(expected.getConfidence(),
                        actual.<Float>get(ImageExtractorConstants.TextParagraph.CONFIDENCE_FIELD_NAME),
                        DELTA);

    List<StructuredRecord> paragraphs = actual.get(ImageExtractorConstants.TextParagraph.WORDS_FIELD_NAME);
    Assert.assertNotNull(paragraphs);
    Assert.assertEquals(expected.getWordsList().size(), paragraphs.size());
    for (int i = 0; i < expected.getWordsList().size(); i++) {
      Word expectedWord = expected.getWords(i);
      StructuredRecord actualWord = paragraphs.get(i);
      assertWordEquals(expectedWord, actualWord);
    }

    List<StructuredRecord> languages = actual.get(ImageExtractorConstants.TextParagraph.DETECTED_LANGUAGES_FIELD_NAME);
    Assert.assertNotNull(languages);
    Assert.assertEquals(expected.getProperty().getDetectedLanguagesList().size(), languages.size());
    for (int i = 0; i < expected.getProperty().getDetectedLanguagesList().size(); i++) {
      TextAnnotation.DetectedLanguage expectedLanguage = expected.getProperty().getDetectedLanguages(i);
      StructuredRecord actualLanguage = languages.get(i);
      assertLanguageEquals(expectedLanguage, actualLanguage);
    }
    String expectedBreak = expected.getProperty().getDetectedBreak().getType().name();
    Assert.assertEquals(expectedBreak, actual.get(ImageExtractorConstants.TextParagraph.DETECTED_BREAK_FIELD_NAME));
  }

  private void assertWordEquals(Word expected, StructuredRecord actual) {
    Assert.assertEquals(expected.getConfidence(),
                        actual.<Float>get(ImageExtractorConstants.TextWord.CONFIDENCE_FIELD_NAME),
                        DELTA);

    List<StructuredRecord> paragraphs = actual.get(ImageExtractorConstants.TextWord.SYMBOLS_FIELD_NAME);
    Assert.assertNotNull(paragraphs);
    Assert.assertEquals(expected.getSymbolsList().size(), paragraphs.size());
    for (int i = 0; i < expected.getSymbolsList().size(); i++) {
      Symbol expectedSymbol = expected.getSymbols(i);
      StructuredRecord actualSymbol = paragraphs.get(i);
      assertSymbolEquals(expectedSymbol, actualSymbol);
    }

    List<StructuredRecord> languages = actual.get(ImageExtractorConstants.TextWord.DETECTED_LANGUAGES_FIELD_NAME);
    Assert.assertNotNull(languages);
    Assert.assertEquals(expected.getProperty().getDetectedLanguagesList().size(), languages.size());
    for (int i = 0; i < expected.getProperty().getDetectedLanguagesList().size(); i++) {
      TextAnnotation.DetectedLanguage expectedLanguage = expected.getProperty().getDetectedLanguages(i);
      StructuredRecord actualLanguage = languages.get(i);
      assertLanguageEquals(expectedLanguage, actualLanguage);
    }
    String expectedBreak = expected.getProperty().getDetectedBreak().getType().name();
    Assert.assertEquals(expectedBreak, actual.get(ImageExtractorConstants.TextWord.DETECTED_BREAK_FIELD_NAME));
  }

  private void assertSymbolEquals(Symbol expected, StructuredRecord actual) {
    Assert.assertEquals(expected.getText(), actual.get(ImageExtractorConstants.TextSymbol.TEXT_FIELD_NAME));
    Assert.assertEquals(expected.getConfidence(),
                        actual.<Float>get(ImageExtractorConstants.TextSymbol.CONFIDENCE_FIELD_NAME),
                        DELTA);

    List<StructuredRecord> languages = actual.get(ImageExtractorConstants.TextSymbol.DETECTED_LANGUAGES_FIELD_NAME);
    Assert.assertNotNull(languages);
    Assert.assertEquals(expected.getProperty().getDetectedLanguagesList().size(), languages.size());
    for (int i = 0; i < expected.getProperty().getDetectedLanguagesList().size(); i++) {
      TextAnnotation.DetectedLanguage expectedLanguage = expected.getProperty().getDetectedLanguages(i);
      StructuredRecord actualLanguage = languages.get(i);
      assertLanguageEquals(expectedLanguage, actualLanguage);
    }
    String expectedBreak = expected.getProperty().getDetectedBreak().getType().name();
    Assert.assertEquals(expectedBreak, actual.get(ImageExtractorConstants.TextSymbol.DETECTED_BREAK_FIELD_NAME));
  }
}
