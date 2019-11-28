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

package io.cdap.plugin.cloud.vision.transform;

import io.cdap.cdap.api.data.schema.Schema;
import io.cdap.cdap.etl.api.validation.CauseAttributes;
import io.cdap.cdap.etl.api.validation.ValidationFailure;
import io.cdap.cdap.etl.mock.validation.MockFailureCollector;
import io.cdap.plugin.cloud.vision.source.FilePathSourceConfig;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 * Tests of {@link FilePathSourceConfig} methods.
 */
public class ImageExtractorConfigTest {

  private static final String MOCK_STAGE = "mockstage";
  private static final Schema VALID_SCHEMA = Schema.recordOf(
    "schema",
    Schema.Field.of("path", Schema.of(Schema.Type.STRING)),
    Schema.Field.of("extracted", ImageFeature.FACE.getSchema())
  );

  private static final ImageExtractorTransformConfig VALID = ImageExtractorConfigBuilder.builder()
    .setPathField("path")
    .setOutputField("extracted")
    .setFeatures(ImageFeature.FACE.getDisplayName())
    .setSchema(VALID_SCHEMA.toString())
    .build();

  @Test
  public void testValidatePathFieldNull() {
    ImageExtractorTransformConfig config = ImageExtractorConfigBuilder.builder(VALID)
      .setPathField(null)
      .build();

    MockFailureCollector failureCollector = new MockFailureCollector(MOCK_STAGE);
    config.validate(failureCollector);
    assertValidationFailed(failureCollector, ImageExtractorConstants.PATH_FIELD);
  }

  @Test
  public void testValidatePathFieldEmpty() {
    ImageExtractorTransformConfig config = ImageExtractorConfigBuilder.builder(VALID)
      .setPathField("")
      .build();

    MockFailureCollector failureCollector = new MockFailureCollector(MOCK_STAGE);
    config.validate(failureCollector);
    assertValidationFailed(failureCollector, ImageExtractorConstants.PATH_FIELD);
  }

  @Test
  public void testValidateOutputFieldNull() {
    ImageExtractorTransformConfig config = ImageExtractorConfigBuilder.builder(VALID)
      .setOutputField(null)
      .build();

    MockFailureCollector failureCollector = new MockFailureCollector(MOCK_STAGE);
    config.validate(failureCollector);
    assertValidationFailed(failureCollector, ImageExtractorConstants.OUTPUT_FIELD);
  }

  @Test
  public void testValidateOutputFieldEmpty() {
    ImageExtractorTransformConfig config = ImageExtractorConfigBuilder.builder(VALID)
      .setOutputField("")
      .build();

    MockFailureCollector failureCollector = new MockFailureCollector(MOCK_STAGE);
    config.validate(failureCollector);
    assertValidationFailed(failureCollector, ImageExtractorConstants.OUTPUT_FIELD);
  }

  @Test
  public void testValidateFeatureNull() {
    ImageExtractorTransformConfig config = ImageExtractorConfigBuilder.builder(VALID)
      .setFeatures(null)
      .build();

    MockFailureCollector failureCollector = new MockFailureCollector(MOCK_STAGE);
    config.validate(failureCollector);
    assertValidationFailed(failureCollector, ImageExtractorConstants.FEATURES);
  }

  @Test
  public void testValidateFeatureEmpty() {
    ImageExtractorTransformConfig config = ImageExtractorConfigBuilder.builder(VALID)
      .setFeatures("")
      .build();

    MockFailureCollector failureCollector = new MockFailureCollector(MOCK_STAGE);
    config.validate(failureCollector);
    assertValidationFailed(failureCollector, ImageExtractorConstants.FEATURES);
  }

  @Test
  public void testValidateFeatureInvalid() {
    ImageExtractorTransformConfig config = ImageExtractorConfigBuilder.builder(VALID)
      .setFeatures("invalid-split-by")
      .build();

    MockFailureCollector failureCollector = new MockFailureCollector(MOCK_STAGE);
    config.validate(failureCollector);
    assertValidationFailed(failureCollector, ImageExtractorConstants.FEATURES);
  }

  private static void assertValidationFailed(MockFailureCollector failureCollector, String paramName) {
    List<ValidationFailure> failureList = failureCollector.getValidationFailures();
    Assert.assertEquals(1, failureList.size());
    ValidationFailure failure = failureList.get(0);
    List<ValidationFailure.Cause> causeList = getCauses(failure, CauseAttributes.STAGE_CONFIG);
    Assert.assertEquals(1, causeList.size());
    ValidationFailure.Cause cause = causeList.get(0);
    Assert.assertEquals(paramName, cause.getAttribute(CauseAttributes.STAGE_CONFIG));
  }

  @Nonnull
  private static List<ValidationFailure.Cause> getCauses(ValidationFailure failure, String attribute) {
    return failure.getCauses()
      .stream()
      .filter(cause -> cause.getAttribute(attribute) != null)
      .collect(Collectors.toList());
  }
}
