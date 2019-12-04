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
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import io.cdap.plugin.cloud.vision.CredentialsHelper;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.annotation.Nullable;

/**
 * An iterator which iterates over blobs' paths stored in GCS bucket.
 */
public class GCSPathIterator implements Iterator<String> {

  @Nullable
  private final Long lastModifiedEpoch;
  private final boolean skipDirectories;
  private final boolean skipFiles;
  private final Iterator<Blob> blobIterator;

  private boolean currentPathReturned = false;
  private Blob value;


  private GCSPathIterator(Iterator<Blob> blobIterator, boolean skipDirectories, boolean skipFiles,
                          @Nullable Long lastModifiedEpochMilli) {
    this.blobIterator = blobIterator;
    this.skipDirectories = skipDirectories;
    this.skipFiles = skipFiles;
    this.lastModifiedEpoch = lastModifiedEpochMilli;
    this.currentPathReturned = true;
  }

  /**
   * Provides handy methods to build {@link GCSPathIterator} instance.
   */
  public static class Builder {
    @Nullable
    private String serviceAccountFilePath;

    @Nullable
    private Long lastModifiedEpochMilli;
    private String project;
    private String path;
    private boolean skipDirectories;
    private boolean skipFiles;
    private boolean isRecursive;

    public Builder(String path) {
      this.path = path;
      this.skipDirectories = true;
      this.skipFiles = false;
    }

    public Builder setPath(String path) {
      this.path = path;
      return this;
    }

    public Builder setServiceAccountFilePath(@Nullable String serviceAccountFilePath) {
      this.serviceAccountFilePath = serviceAccountFilePath;
      return this;
    }

    public Builder setLastModifiedEpochMilli(@Nullable Long lastModifiedEpochMilli) {
      this.lastModifiedEpochMilli = lastModifiedEpochMilli;
      return this;
    }

    public Builder setProject(String project) {
      this.project = project;
      return this;
    }

    public Builder setSkipDirectories(boolean skipDirectories) {
      this.skipDirectories = skipDirectories;
      return this;
    }

    /**
     * Blobs that represent bucket's top-level and empty directories will be included.
     * Note, that non-empty nested directories will be skipped if {@link GCSPathIterator} instance created using
     * {@link Builder#recursive()} along with this method. Can be used along with {@link Builder#skipFiles()}.
     * <p>
     * For example, an instance created using {@link Builder#recursive()} and {@link Builder#includeDirectories()}
     * methods with the path pointing to the following bucket:
     * <pre>
     * * test-bucket
     * ├── top-level-dir-1/
     * │   └── ...
     * ├── top-level-dir-2/
     * │   ├── nested-empty-dir-2/
     * │   ├── nested-non-empty-dir/
     * │   │   └── ...
     * │   └── ...
     * ├── top-level-empty-dir/
     * └── ...
     * </pre>
     * <p>
     * will iterate over "top-level-dir-1/", "top-level-dir-2/", "nested-empty-dir-2/", "top-level-empty-dir/" and
     * "nested-non-empty-dir/" directory blob will be skipped.
     *
     * @return {@link GCSPathIterator} instance to iterate over directories blobs.
     */
    public Builder includeDirectories() {
      return setSkipDirectories(false);
    }

    public Builder setSkipFiles(boolean skipFiles) {
      this.skipFiles = skipFiles;
      return this;
    }

    /**
     * Indicates that file blobs must be skipped.
     *
     * @return {@link GCSPathIterator} instance to iterate over only on directories blobs and skip file blobs.
     */
    public Builder skipFiles() {
      return setSkipFiles(true);
    }

    public Builder setRecursive(boolean isRecursive) {
      this.isRecursive = isRecursive;
      return this;
    }

    public Builder recursive() {
      return setRecursive(true);
    }

    public GCSPathIterator build() throws IOException {
      Credentials credentials = serviceAccountFilePath == null ? null
        : CredentialsHelper.getCredentials(serviceAccountFilePath);
      Storage storage = getStorage(project, credentials);
      GCSPath gcsPath = GCSPath.from(path);
      Bucket bucket = storage.get(gcsPath.getBucket());
      Page<Blob> blobList = isRecursive ? bucket.list(Storage.BlobListOption.prefix(gcsPath.getName()))
        : bucket.list(Storage.BlobListOption.currentDirectory(), Storage.BlobListOption.prefix(gcsPath.getName()));

      return new GCSPathIterator(blobList.iterateAll().iterator(), skipDirectories, skipFiles, lastModifiedEpochMilli);
    }

    private static Storage getStorage(String project, @Nullable Credentials credentials) {
      StorageOptions.Builder builder = StorageOptions.newBuilder().setProjectId(project);
      if (credentials != null) {
        builder.setCredentials(credentials);
      }
      return builder.build().getService();
    }
  }

  public static Builder builder(String path) {
    return new Builder(path);
  }

  /**
   * Returns {@code true} if the specified blob represents a directory and this iterator configured to skip directories
   * or if the specified blob represents a file and this iterator configured to skip files.
   * {@link Blob#isDirectory()} can not be used since it works only if the blob is returned by
   * {@link Storage#list(String, Storage.BlobListOption...)} when the {@link Storage.BlobListOption#currentDirectory()}
   * option is used.
   *
   * @param blob blob to check.
   * @return {@code true} if the specified blob must be skipped according to it's type.
   */
  private boolean mustBeSkippedByType(Blob blob) {
    boolean isDirectory = blob.getName().endsWith("/");
    return skipDirectories && isDirectory || skipFiles && !isDirectory;
  }

  /**
   * Checks whether specified blob must be skipped according to the last modified timestamp. Returns {@code true} if
   * blob's last modification timestamp is less than or equal to the configured one.
   *
   * @param blob blob to check.
   * @return {@code true} if blob's last modification timestamp is less than or equal to the configured one.
   */
  private boolean mustBeSkippedByTimestamp(Blob blob) {
    return !(lastModifiedEpoch == null || blob.getUpdateTime() == null) && blob.getUpdateTime() <= lastModifiedEpoch;
  }

  private String toPath(Blob blob) {
    return String.format("gs://%s/%s", blob.getBucket(), blob.getName());
  }

  @Override
  public boolean hasNext() {
    if (currentPathReturned) {
      if (!blobIterator.hasNext()) {
        return false;
      }
      value = blobIterator.next();
      while ((mustBeSkippedByType(value) || mustBeSkippedByTimestamp(value)) && blobIterator.hasNext()) {
        value = blobIterator.next();
      }
      currentPathReturned = false;
    }
    return !mustBeSkippedByType(value) && !mustBeSkippedByTimestamp(value);
  }

  @Override
  public String next() {
    if (!hasNext()) {
      throw new NoSuchElementException("No more paths.");
    }
    currentPathReturned = true;
    return toPath(value);
  }
}
