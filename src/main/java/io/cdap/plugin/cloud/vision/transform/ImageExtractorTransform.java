/*
 * Copyright © 2015 Cask Data, Inc.
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

import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Macro;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.cdap.api.data.format.StructuredRecord;
import io.cdap.cdap.api.data.schema.Schema;
import io.cdap.cdap.api.plugin.PluginConfig;
import io.cdap.cdap.etl.api.Emitter;
import io.cdap.cdap.etl.api.PipelineConfigurer;
import io.cdap.cdap.etl.api.StageSubmitterContext;
import io.cdap.cdap.etl.api.Transform;
import io.cdap.cdap.etl.api.TransformContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Google Cloud Vision Image Extractor Transform which can be used in conjunction with the file path batch source to
 * extract enrichments from each image based on selected features.
 */
@Plugin(type = Transform.PLUGIN_TYPE)
@Name(ImageExtractorTransform.NAME)
@Description(ImageExtractorTransform.DESCRIPTION)
public class ImageExtractorTransform extends Transform<StructuredRecord, StructuredRecord> {

  public static final String NAME = "ImageExtractor";
  public static final String DESCRIPTION = "Extracts enrichments from each image based on selected features.";

  private SpeechTransformConfig config;
  private Schema outputSchema = null;

  @Override
  public void configurePipeline(PipelineConfigurer configurer) throws IllegalArgumentException {
    super.configurePipeline(configurer);
    Schema inputSchema = configurer.getStageConfigurer().getInputSchema();
//    config.validate(inputSchema);
    configurer.getStageConfigurer().setOutputSchema(getSchema(inputSchema));
  }

  @Override
  public void prepareRun(StageSubmitterContext context) throws Exception {
    super.prepareRun(context);
//    config.validate(context.getInputSchema());
  }

  @Override
  public void initialize(TransformContext context) throws Exception {
    super.initialize(context);
    outputSchema = context.getOutputSchema();
  }

  @Override
  public void transform(StructuredRecord input, Emitter<StructuredRecord> emitter) {
    // if an output schema is available then use it or else use the schema for the given input record
    Schema currentSchema;
    if (outputSchema != null) {
      currentSchema = outputSchema;
    } else {
      currentSchema = getSchema(input.getSchema());
    }

    StructuredRecord.Builder outputBuilder = StructuredRecord.builder(currentSchema);
    String orig = input.get(config.originalField);
    outputBuilder.set(config.transformedField, orig == null ? "" : new StringBuilder(orig).reverse().toString());
    copyFields(input, outputBuilder);

    emitter.emit(outputBuilder.build());
  }

  @Override
  public void destroy() {
    super.destroy();
    // TODO
  }

  private Schema getSchema(Schema inputSchema) {
    // TODO move to the config, perform validation
    List<Schema.Field> fields = new ArrayList<>();
    if (inputSchema.getFields() != null) {
      fields.addAll(inputSchema.getFields());
    }
    fields.add(Schema.Field.of(config.transformedField, Schema.nullableOf(Schema.of(Schema.Type.STRING))));

    return Schema.recordOf("record", fields);
  }

  private void copyFields(StructuredRecord input, StructuredRecord.Builder outputBuilder) {
    // copy other schema field values
    List<Schema.Field> fields = input.getSchema().getFields();
    if (fields != null) {
      for (Schema.Field field : fields) {
        outputBuilder.set(field.getName(), input.get(field.getName()));
      }
    }
  }


  private static class SpeechTransformConfig extends PluginConfig {

    @Macro
    @Description("The name of the field to be transformed.")
    @Name("original")
    private String originalField;

    @Macro
    @Description("The name of the field to store transformation result.")
    @Name("transformed")
    private String transformedField;

    // TODO
  }
}
