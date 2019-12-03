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

import io.cdap.plugin.cloud.vision.CloudVisionConstants;

/**
 * Cloud Vision File Path Source constants.
 */
public class FilePathSourceConstants extends CloudVisionConstants {

  private FilePathSourceConstants() {
    throw new AssertionError("Should not instantiate static utility class.");
  }

  /**
   * File Path Batch Source plugin name.
   */
  public static final String PLUGIN_NAME = "FilePath";

  /**
   * Configuration property name used to specify path to the directory where the files whose paths are to be emitted
   * are located.
   */
  public static final String PATH = "path";

  /**
   * Configuration property name used to specify whether the plugin should recursively traverse the directory for
   * subdirectories.
   */
  public static final String RECURSIVE = "recursive";

  /**
   * Configuration property name used to specify a way to filter files to be returned based on their last modified
   * timestamp.
   */
  public static final String LAST_MODIFIED = "lastModified";

  /**
   * Configuration property name used to specify a splitting mechanism.
   */
  public static final String SPLIT_BY = "splitBy";

  /**
   * File paths mapped to a record with a single {@value PATH_FIELD_NAME} string field.
   */
  public static final String PATH_FIELD_NAME = "path";
}
