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

import io.cdap.plugin.cloud.vision.CloudVisionConfigBuilder;

import javax.annotation.Nullable;

/**
 * Builder class that provides handy methods to construct {@link FilePathSourceConfig} for testing.
 */
public class FilePathSourceConfigBuilder extends CloudVisionConfigBuilder<FilePathSourceConfigBuilder> {

  private String referenceName;
  private String path;
  private boolean recursive;
  private String splitBy;

  @Nullable
  private String lastModified;

  public static FilePathSourceConfigBuilder builder() {
    return new FilePathSourceConfigBuilder();
  }

  public static FilePathSourceConfigBuilder builder(FilePathSourceConfig original) {
    return new FilePathSourceConfigBuilder()
      .setReferenceName(original.getReferenceName())
      .setProject(original.getProject())
      .setServiceFilePath(original.getServiceAccountFilePath())
      .setPath(original.getPath())
      .setLastModified(original.getLastModified())
      .setRecursive(original.isRecursive())
      .setSplitBy(original.getSplitBy());

  }

  public FilePathSourceConfigBuilder setReferenceName(String referenceName) {
    this.referenceName = referenceName;
    return this;
  }

  public FilePathSourceConfigBuilder setPath(String path) {
    this.path = path;
    return this;
  }

  public FilePathSourceConfigBuilder setRecursive(boolean recursive) {
    this.recursive = recursive;
    return this;
  }

  public FilePathSourceConfigBuilder setSplitBy(String splitBy) {
    this.splitBy = splitBy;
    return this;
  }

  public FilePathSourceConfigBuilder setLastModified(@Nullable String lastModified) {
    this.lastModified = lastModified;
    return this;
  }

  public FilePathSourceConfig build() {
    return new FilePathSourceConfig(referenceName, path, serviceFilePath, path, recursive, lastModified, splitBy);
  }
}
