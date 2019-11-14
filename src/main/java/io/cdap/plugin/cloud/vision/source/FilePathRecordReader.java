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
import org.apache.commons.collections.iterators.EmptyIterator;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;

/**
 * RecordReader implementation, which reads String entries.
 */
public class FilePathRecordReader extends RecordReader<NullWritable, String> {
  private static final Logger LOG = LoggerFactory.getLogger(FilePathRecordReader.class);
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
  public void initialize(InputSplit inputSplit, TaskAttemptContext taskAttemptContext) {
    Configuration conf = taskAttemptContext.getConfiguration();
    String confJson = conf.get(FilePathInputFormatProvider.PROPERTY_CONFIG_JSON);
    FilePathSourceConfig config = gson.fromJson(confJson, FilePathSourceConfig.class);

    // TODO implement
    this.iterator = EmptyIterator.INSTANCE;
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
    LOG.trace("Closing Record reader");
    // TODO
  }
}
