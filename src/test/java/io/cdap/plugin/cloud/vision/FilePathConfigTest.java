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

package io.cdap.plugin.cloud.vision;

import io.cdap.cdap.etl.api.validation.CauseAttributes;
import io.cdap.cdap.etl.api.validation.ValidationFailure;
import io.cdap.cdap.etl.mock.validation.MockFailureCollector;
import io.cdap.plugin.common.Constants;
import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 * Tests of {@link FilePathSourceConfig} methods.
 */
public class FilePathConfigTest {

  private static final String MOCK_STAGE = "mockstage";
  private static final FilePathSourceConfig VALID = FilePathSourceConfigBuilder.builder()
    .setReferenceName("FilePathSource")
    .setPath("gs://test-bucket/some-dir")
    .setRecursive(false)
    .setLastModified("2019-10-02T13:12:55.123Z")
    .setSplitBy(SplittingMechanism.DIRECTORY.getDisplayName())
    .build();

  @Test
  public void testValidateValid() {
    MockFailureCollector failureCollector = new MockFailureCollector(MOCK_STAGE);
    FilePathSourceConfigBuilder.builder(VALID).build().validate(failureCollector);
    Assert.assertTrue(failureCollector.getValidationFailures().isEmpty());
  }

  @Test
  public void testValidateReferenceNameNull() {
    FilePathSourceConfig config = FilePathSourceConfigBuilder.builder(VALID)
      .setReferenceName(null)
      .build();

    MockFailureCollector failureCollector = new MockFailureCollector(MOCK_STAGE);
    config.validate(failureCollector);
    assertValidationFailed(failureCollector, Constants.Reference.REFERENCE_NAME);
  }

  @Test
  public void testValidateReferenceNameEmpty() {
    FilePathSourceConfig config = FilePathSourceConfigBuilder.builder(VALID)
      .setReferenceName("")
      .build();

    MockFailureCollector failureCollector = new MockFailureCollector(MOCK_STAGE);
    config.validate(failureCollector);
    assertValidationFailed(failureCollector, Constants.Reference.REFERENCE_NAME);
  }

  @Test
  public void testValidateReferenceNameInvalid() {
    FilePathSourceConfig config = FilePathSourceConfigBuilder.builder(VALID)
      .setReferenceName("**********")
      .build();

    MockFailureCollector failureCollector = new MockFailureCollector(MOCK_STAGE);
    config.validate(failureCollector);
    assertValidationFailed(failureCollector, Constants.Reference.REFERENCE_NAME);
  }

  @Test
  public void testValidateSplitByNull() {
    FilePathSourceConfig config = FilePathSourceConfigBuilder.builder(VALID)
      .setSplitBy(null)
      .build();

    MockFailureCollector failureCollector = new MockFailureCollector(MOCK_STAGE);
    config.validate(failureCollector);
    assertValidationFailed(failureCollector, CloudVisionConstants.SPLIT_BY);
  }

  @Test
  public void testValidateSplitByEmpty() {
    FilePathSourceConfig config = FilePathSourceConfigBuilder.builder(VALID)
      .setSplitBy("")
      .build();

    MockFailureCollector failureCollector = new MockFailureCollector(MOCK_STAGE);
    config.validate(failureCollector);
    assertValidationFailed(failureCollector, CloudVisionConstants.SPLIT_BY);
  }

  @Test
  public void testValidateSplitByInvalid() {
    FilePathSourceConfig config = FilePathSourceConfigBuilder.builder(VALID)
      .setSplitBy("invalid-split-by")
      .build();

    MockFailureCollector failureCollector = new MockFailureCollector(MOCK_STAGE);
    config.validate(failureCollector);
    assertValidationFailed(failureCollector, CloudVisionConstants.SPLIT_BY);
  }

  @Test
  public void testValidatePathNull() {
    FilePathSourceConfig config = FilePathSourceConfigBuilder.builder(VALID)
      .setPath(null)
      .build();

    MockFailureCollector failureCollector = new MockFailureCollector(MOCK_STAGE);
    config.validate(failureCollector);
    assertValidationFailed(failureCollector, CloudVisionConstants.PATH);
  }

  @Test
  public void testValidatePathEmpty() {
    FilePathSourceConfig config = FilePathSourceConfigBuilder.builder(VALID)
      .setPath("")
      .build();

    MockFailureCollector failureCollector = new MockFailureCollector(MOCK_STAGE);
    config.validate(failureCollector);
    assertValidationFailed(failureCollector, CloudVisionConstants.PATH);
  }

  @Test
  public void testValidateModificationTime() {
    String time = "2019-10-02T13:12:55.123Z";
    FilePathSourceConfig config = FilePathSourceConfigBuilder.builder(VALID)
      .setLastModified(time)
      .build();

    long epochExpected = Instant.parse(time).toEpochMilli();
    Long epochActual = config.getLastModifiedEpochMilli();
    Assert.assertNotNull(epochActual);
    Assert.assertEquals(epochExpected, (long) epochActual);
  }

  @Test
  public void testValidateModificationTimeInvalid() {
    FilePathSourceConfig config = FilePathSourceConfigBuilder.builder(VALID)
      .setLastModified("invalid-time")
      .build();

    MockFailureCollector failureCollector = new MockFailureCollector(MOCK_STAGE);
    config.validate(failureCollector);
    assertValidationFailed(failureCollector, CloudVisionConstants.LAST_MODIFIED);
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
