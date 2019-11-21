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

import com.google.cloud.vision.v1.AnnotateImageResponse;
import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.cdap.api.data.format.StructuredRecord;
import io.cdap.cdap.api.data.schema.Schema;
import io.cdap.cdap.etl.api.Emitter;
import io.cdap.cdap.etl.api.FailureCollector;
import io.cdap.cdap.etl.api.PipelineConfigurer;
import io.cdap.cdap.etl.api.StageConfigurer;
import io.cdap.cdap.etl.api.StageSubmitterContext;
import io.cdap.cdap.etl.api.Transform;
import io.cdap.cdap.etl.api.TransformContext;
import io.cdap.plugin.cloud.vision.transform.transformer.FaceAnnotationsToRecordTransformer;
import io.cdap.plugin.cloud.vision.transform.transformer.ImageAnnotationToRecordTransformer;

import java.util.ArrayList;
import java.util.List;

/**
 * Google Cloud Vision Image Extractor Transform which can be used in conjunction with the file path batch source to
 * extract enrichments from each image based on selected features.
 */
@Plugin(type = Transform.PLUGIN_TYPE)
@Name(ImageExtractorConstants.PLUGIN_NAME)
@Description("Extracts enrichments from each image based on selected features.")
public class ImageExtractorTransform extends Transform<StructuredRecord, StructuredRecord> {

  private CloudVisionClient cloudVisionClient;
  private ImageAnnotationToRecordTransformer transformer;
  private ImageExtractorTransformConfig config;

  public ImageExtractorTransform(ImageExtractorTransformConfig config) {
    this.config = config;
  }

  @Override
  public void configurePipeline(PipelineConfigurer configurer) throws IllegalArgumentException {
    super.configurePipeline(configurer);
    Schema inputSchema = configurer.getStageConfigurer().getInputSchema();
    StageConfigurer stageConfigurer = configurer.getStageConfigurer();
    FailureCollector collector = stageConfigurer.getFailureCollector();
    config.validate(collector);
    collector.getOrThrowException();
    Schema schema = getSchema(inputSchema);
    Schema configuredSchema = config.getParsedSchema();
    if (configuredSchema == null) {
      configurer.getStageConfigurer().setOutputSchema(schema);
      return;
    }
    ImageExtractorTransformConfig.validateFieldsMatch(schema, configuredSchema, collector);
    collector.getOrThrowException();
    configurer.getStageConfigurer().setOutputSchema(configuredSchema);
//    configurer.getStageConfigurer().setErrorSchema(ERROR_SCHEMA);
  }

  @Override
  public void prepareRun(StageSubmitterContext context) throws Exception {
    super.prepareRun(context);
    FailureCollector collector = context.getFailureCollector();
    config.validate(collector);
    collector.getOrThrowException();
  }

  @Override
  public void initialize(TransformContext context) throws Exception {
    super.initialize(context);
    Schema schema = context.getOutputSchema();
    // todo create transformed according to the feature type
    transformer = new FaceAnnotationsToRecordTransformer(schema, config.getOutputField());
    cloudVisionClient = new CloudVisionClient(config);
  }

  @Override
  public void transform(StructuredRecord input, Emitter<StructuredRecord> emitter) throws Exception {
    String imagePath = input.get(config.getPathField());
    AnnotateImageResponse response = cloudVisionClient.extractFaces(imagePath);
    StructuredRecord transformed = transformer.transform(input, response);
    emitter.emit(transformed);
  }

  @Override
  public void destroy() {
    super.destroy();
    if (cloudVisionClient != null) {
      try {
        cloudVisionClient.close();
      } catch (Exception e) {
        // no-op
      }
    }
  }

  private Schema getSchema(Schema inputSchema) {
    List<Schema.Field> fields = new ArrayList<>();
    if (inputSchema.getFields() != null) {
      fields.addAll(inputSchema.getFields());
    }
    // TODO infer schema according to the specified feature type
    fields.add(Schema.Field.of(config.getOutputField(), Schema.arrayOf(ImageExtractorConstants.FaceAnnotation.SCHEMA)));
    return Schema.recordOf("record", fields);
  }

}
