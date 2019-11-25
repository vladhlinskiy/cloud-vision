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

import io.cdap.cdap.api.data.schema.Schema;
import io.cdap.plugin.cloud.vision.transform.ImageExtractorTransformConfig;

/**
 * A factory which creates instance of {@ImageAnnotationToRecordTransformer} in accordance to feature and output schema
 * configured in input config.
 */
public class TransformerFactory {
  public static ImageAnnotationToRecordTransformer createInstance(ImageExtractorTransformConfig config, Schema schema) {
    switch (config.getImageFeature()) {
      case FACE:
        return new FaceAnnotationsToRecordTransformer(schema, config.getOutputField());
      case TEXT:
        return new TextAnnotationsToRecordTransformer(schema, config.getOutputField());
      case HANDWRITING:
        return new DocumentTextAnnotationsToRecordTransformer(schema, config.getOutputField());
      default:
        throw new IllegalArgumentException(String.format("Unsupported image feature: '%s'", config.getImageFeature()));
    }
  }
}
