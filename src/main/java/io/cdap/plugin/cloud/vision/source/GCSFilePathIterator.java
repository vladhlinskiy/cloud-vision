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

import com.google.api.gax.paging.Page;
import com.google.auth.Credentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import javax.annotation.Nullable;

/**
 * An iterator which iterates over names of files stored in GCS bucket.
 */
public class GCSFilePathIterator implements Iterator<String> {

  @Nullable
  private final Long lastModifiedEpochMilli;
  private final Iterator<Blob> blobIterator;
  private Blob value;

  private GCSFilePathIterator(Iterator<Blob> blobIterator, @Nullable Long lastModifiedEpochMilli) {
    this.blobIterator = blobIterator;
    this.lastModifiedEpochMilli = lastModifiedEpochMilli;
  }

  public static GCSFilePathIterator create(String project,
                                           @Nullable String serviceAccountFilePath,
                                           String path,
                                           @Nullable Long lastModifiedEpochMilli,
                                           boolean isRecursive) throws IOException {
    Credentials credentials = serviceAccountFilePath == null ? null : loadCredentials(serviceAccountFilePath);
    Storage storage = getStorage(project, credentials);
    GCSPath gcsPath = GCSPath.from(path);
    Bucket bucket = storage.get(gcsPath.getBucket());
    Page<Blob> blobList = isRecursive ? bucket.list(Storage.BlobListOption.prefix(gcsPath.getName()))
      : bucket.list(Storage.BlobListOption.currentDirectory(), Storage.BlobListOption.prefix(gcsPath.getName()));

    return new GCSFilePathIterator(blobList.iterateAll().iterator(), lastModifiedEpochMilli);
  }

  private static ServiceAccountCredentials loadCredentials(String path) throws IOException {
    File credentialsPath = new File(path);
    try (FileInputStream serviceAccountStream = new FileInputStream(credentialsPath)) {
      return ServiceAccountCredentials.fromStream(serviceAccountStream);
    }
  }

  private static Storage getStorage(String project, @Nullable Credentials credentials) {
    StorageOptions.Builder builder = StorageOptions.newBuilder().setProjectId(project);
    if (credentials != null) {
      builder.setCredentials(credentials);
    }
    return builder.build().getService();
  }

  /**
   * Returns {@code true} if the specified blob represents a directory. {@link Blob#isDirectory()} can not be used since
   * it works only if the blob is returned by {@link Storage#list(String, Storage.BlobListOption...)} when the
   * {@link Storage.BlobListOption#currentDirectory()} option is used.
   *
   * @param blob blob to check.
   * @return {@code true} if the specified blob represents a directory.
   */
  private boolean isDirectory(Blob blob) {
    return blob.getName().endsWith("/");
  }

  private String toPath(Blob blob) {
    return String.format("gs://%s/%s", blob.getBucket(), blob.getName());
  }

  /**
   * Checks whether specified blob must be skipped according to the last modified timestamp. Returns {@code true} if
   * blob's last modification timestamp is less than or equal to the configured one.
   *
   * @param blob blob to check.
   * @return {@code true} if blob's last modification timestamp is less than or equal to the configured one.
   */
  private boolean mustBeSkippedByTimestamp(Blob blob) {
    if (lastModifiedEpochMilli == null || blob.getUpdateTime() == null) {
      return false;
    }
    return blob.getUpdateTime() <= lastModifiedEpochMilli;
  }

  @Override
  public boolean hasNext() {
    if (!blobIterator.hasNext()) {
      return false;
    }
    value = blobIterator.next();
    while ((isDirectory(value) || mustBeSkippedByTimestamp(value)) && blobIterator.hasNext()) {
      value = blobIterator.next();
    }
    return !isDirectory(value) && !mustBeSkippedByTimestamp(value);
  }

  @Override
  public String next() {
    return toPath(value);
  }
}
