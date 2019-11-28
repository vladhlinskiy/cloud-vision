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
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * InputFormat for mapreduce job, which provides a single split of data.
 */
public class FilePathInputFormat extends InputFormat {

  private static final Gson gson = new GsonBuilder().create();

  @Override
  public List<InputSplit> getSplits(JobContext jobContext) throws IOException {
    Configuration configuration = jobContext.getConfiguration();
    String confJson = configuration.get(FilePathInputFormatProvider.PROPERTY_CONFIG_JSON);
    FilePathSourceConfig config = gson.fromJson(confJson, FilePathSourceConfig.class);
    SplittingMechanism splittingMechanism = config.getSplittingMechanism();
    if (splittingMechanism == SplittingMechanism.DEFAULT || !config.isRecursive()) {
      // Use single split as default splitting mechanism.
      // Also, single split is used in the case when the plugin configured to not read the path recursively
      return Collections.singletonList(new FilePathSplit(config.getPath(), config.isRecursive()));
    }

    GCSPathIterator directoryIterator = GCSPathIterator.builder(config.getPath())
      .setRecursive(false)
      .includeDirectories()
      .skipFiles()
      .setProject(config.getProject())
      .setServiceAccountFilePath(config.getServiceAccountFilePath())
      .build();

    List<InputSplit> splits = new ArrayList<>();
    while (directoryIterator.hasNext()) {
      // iterate over subdirectories
      String subDirectoryPath = directoryIterator.next();
      splits.add(new FilePathSplit(subDirectoryPath, config.isRecursive()));
    }
    // add one more split for top-level directory
    splits.add(new FilePathSplit(config.getPath(), false));

    return splits;
  }

  @Override
  public RecordReader<NullWritable, String> createRecordReader(
    InputSplit inputSplit, TaskAttemptContext taskAttemptContext) {
    return new GCSFilePathRecordReader();
  }
}
