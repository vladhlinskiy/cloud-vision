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
package io.cdap.plugin.cloud.vision.source;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.cdap.plugin.cloud.vision.FilePathSourceConfig;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import java.io.IOException;
import java.util.Iterator;

/**
 * RecordReader implementation, which reads names of files stored in GCS bucket.
 */
public class GCSFilePathRecordReader extends RecordReader<NullWritable, String> {
  private static final Gson gson = new GsonBuilder().create();

  private Iterator<String> iterator;
  private String value;

  /**
   * Initialize an iterator and config.
   *
   * @param inputSplit         specifies batch details
   * @param taskAttemptContext task context
   */
  @Override
  public void initialize(InputSplit inputSplit, TaskAttemptContext taskAttemptContext) throws IOException {
    Configuration configuration = taskAttemptContext.getConfiguration();
    String confJson = configuration.get(FilePathInputFormatProvider.PROPERTY_CONFIG_JSON);
    FilePathSourceConfig conf = gson.fromJson(confJson, FilePathSourceConfig.class);
    // TODO splits
    this.iterator = GCSFilePathIterator.create(conf.getProject(), conf.getServiceAccountFilePath(), conf.getPath(),
                                               conf.getLastModifiedEpochMilli(), conf.isRecursive());
  }

  @Override
  public boolean nextKeyValue() {
    if (!iterator.hasNext()) {
      return false;
    }
    value = iterator.next();
    return true;
  }

  @Override
  public NullWritable getCurrentKey() {
    return null;
  }

  @Override
  public String getCurrentValue() {
    return value;
  }

  @Override
  public float getProgress() {
    // progress is unknown
    return 0.0f;
  }

  @Override
  public void close() throws IOException {
  }
}
