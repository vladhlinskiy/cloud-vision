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

import com.google.auth.Credentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageClass;
import com.google.cloud.storage.StorageOptions;
import io.cdap.plugin.cloud.vision.CredentialsHelper;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

/**
 * Tests of {@link GCSPathIterator} methods.
 * Creates the following hierarchy in the test bucket accroding to the {@link GCSPathIteratorTest#TEST_DIRS_NUM}:
 * <pre>
 * test-bucket
 * ├── 1/
 * │   ├── 8b0685ed-0e9b-41af-ab04-7c3b033d881a
 * │   ├── ... (number of files equals to {@value TEST_DIRS_FILES}/{@value TEST_DIRS_NUM})
 * │   └── 123685ed-0e9b-41af-ab04-7c3b033d881a
 * ├── 2/
 * │   ├── nested/
 * │   │   ├── 2115d6b9-479b-4fb7-b3ae-5261ff61fdca
 * │   │   ├── ... (contains half of the last test directory files)
 * │   │   └── 1945e57e-b512-4d89-8974-f914d0188283
 * │   ├── 0c7200da-aee6-49f0-8a04-c51449fe4517
 * │   ├── ...
 * │   ├── ...
 * │   ├── ... (total number of files in the last test directory equals to
 * │   ├── ... {@value TEST_DIRS_FILES}/{@value TEST_DIRS_NUM} + {@value TEST_DIRS_FILES}%{@value TEST_DIRS_NUM})
 * │   ├── ...
 * │   ├── ...
 * │   └── ffe581a5-47a3-4a10-99da-adce1b9fc263
 * ├── empty-dir/
 * ├── a1228bdf-c36d-45f3-ab21-a86a8407c14d
 * ├── ... (total number of top-level files equals to {@value GCSPathIteratorTest#TOP_LEVEL_DIR_FILES})
 * └── 4be75485-7ca0-4167-b362-f050c3af4a9c
 * </pre>
 */
public class GCSPathIteratorTest {

  public static final String EMPTY_DIRECTORY_NAME = "empty-dir/";
  public static final int TOP_LEVEL_DIR_FILES = 19;
  public static final int TEST_DIRS_FILES = 5;
  public static final int TEST_DIRS_NUM = 2;

  protected static final String PROJECT = System.getProperty("project", "auto-detect");
  protected static final String SERVICE_ACCOUNT_FILE_PATH = System.getProperty("serviceFilePath", "auto-detect");
  protected static final String PATH = System.getProperty("path", "gs://file-path-source-test-bucket");

  private static Storage storage;
  private static Bucket bucket;
  private static Set<String> topLevelFilePaths;
  private static Set<String> allFilePaths;
  private static Map<Integer, Set<String>> testDirFiles;

  @BeforeClass
  public static void testSetup() throws Exception {
    Credentials credentials = CredentialsHelper.getCredentials(SERVICE_ACCOUNT_FILE_PATH);
    storage = getStorage(PROJECT, credentials);

    GCSPath path = GCSPath.from(PATH);
    String bucketName = path.getBucket();
    bucket = storage.get(bucketName);
    if (bucket != null) {
      deleteBucket(storage, bucket);
    }

    BucketInfo bucketInfo = BucketInfo.newBuilder(bucketName)
      .setStorageClass(StorageClass.STANDARD)
      .setLocation("us-central1")
      .build();
    bucket = storage.create(bucketInfo);
    initTestsData();
  }

  private static void initTestsData() {
    topLevelFilePaths = new HashSet<>();
    allFilePaths = new HashSet<>();
    testDirFiles = new HashMap<>();
    for (int i = 0; i <= TOP_LEVEL_DIR_FILES; i++) {
      String createdFilePath = createEmptyFile();
      topLevelFilePaths.add(createdFilePath);
      allFilePaths.add(createdFilePath);
    }
    for (int i = 1; i <= TEST_DIRS_NUM; i++) {
      String testDirName = testDirectoryName(i);
      createDirectory(testDirName);
      if (i != TEST_DIRS_NUM) {
        int filesToCreate = TEST_DIRS_FILES / TEST_DIRS_NUM;
        Set<String> createdFilePaths = createEmptyFile(testDirName, filesToCreate);
        testDirFiles.put(i, createdFilePaths);
        allFilePaths.addAll(createdFilePaths);
      } else {
        // last directory contains nested directory
        String nestedDirName = testDirName + "nested/";
        int filesToCreate = TEST_DIRS_FILES / TEST_DIRS_NUM + TEST_DIRS_FILES % TEST_DIRS_NUM;
        int filesToCreateInNestedDir = filesToCreate / 2;
        Set<String> nestedFilePaths = createEmptyFile(nestedDirName, filesToCreateInNestedDir);
        Set<String> createdFilePaths = createEmptyFile(testDirName, filesToCreate - filesToCreateInNestedDir);
        testDirFiles.put(i, createdFilePaths);
        allFilePaths.addAll(createdFilePaths);
        allFilePaths.addAll(nestedFilePaths);
      }
    }

    createDirectory(EMPTY_DIRECTORY_NAME);
  }

  @Test
  public void testIterateTopLevelDirectoryFiles() throws IOException {
    GCSPathIterator iterator = GCSPathIterator.builder(bucket.getName())
      .setProject(PROJECT)
      .setServiceAccountFilePath(SERVICE_ACCOUNT_FILE_PATH)
      .setRecursive(false)
      .setSkipFiles(false)
      .setSkipDirectories(true)
      .build();

    for (int i = 0; i < topLevelFilePaths.size(); i++) {
      Assert.assertTrue(iterator.hasNext());
      String actualPath = iterator.next();
      Assert.assertTrue(topLevelFilePaths.contains(actualPath));
    }
    Assert.assertFalse(iterator.hasNext());
  }

  @Test
  public void testIterateDirectoryNonRecursive() throws IOException {
    // last directory contains nested directory, that must not be read
    String path = bucket.getName() + "/" + testDirectoryName(TEST_DIRS_NUM);
    GCSPathIterator iterator = GCSPathIterator.builder(path)
      .setProject(PROJECT)
      .setServiceAccountFilePath(SERVICE_ACCOUNT_FILE_PATH)
      .setRecursive(false)
      .setSkipFiles(false)
      .setSkipDirectories(true)
      .build();

    for (int i = 0; i < testDirFiles.get(TEST_DIRS_NUM).size(); i++) {
      Assert.assertTrue(iterator.hasNext());
      String actualPath = iterator.next();
      Assert.assertTrue(testDirFiles.get(TEST_DIRS_NUM).contains(actualPath));
    }
    Assert.assertFalse(iterator.hasNext());
  }

  @Test
  public void testIterateAllFiles() throws IOException {
    GCSPathIterator iterator = GCSPathIterator.builder(bucket.getName())
      .setProject(PROJECT)
      .setServiceAccountFilePath(SERVICE_ACCOUNT_FILE_PATH)
      .setRecursive(true)
      .setSkipFiles(false)
      .setSkipDirectories(true)
      .build();

    for (int i = 0; i < allFilePaths.size(); i++) {
      Assert.assertTrue(iterator.hasNext());
      String actualPath = iterator.next();
      Assert.assertTrue(allFilePaths.contains(actualPath));
    }
    Assert.assertFalse(iterator.hasNext());
  }

  @Test
  public void testIterateFilesAndDirectories() throws IOException {
    GCSPathIterator iterator = GCSPathIterator.builder(bucket.getName())
      .setProject(PROJECT)
      .setServiceAccountFilePath(SERVICE_ACCOUNT_FILE_PATH)
      .setRecursive(true)
      .setSkipFiles(false)
      .setSkipDirectories(false)
      .build();

    // all files + TEST_DIRS_NUM + empty dir
    for (int i = 0; i < allFilePaths.size() + TEST_DIRS_NUM + 1; i++) {
      Assert.assertTrue(iterator.hasNext());
      iterator.next();
    }
    Assert.assertFalse(iterator.hasNext());
  }

  @Test
  public void testIterateDirectoriesOnly() throws IOException {
    GCSPathIterator iterator = GCSPathIterator.builder(bucket.getName())
      .setProject(PROJECT)
      .setServiceAccountFilePath(SERVICE_ACCOUNT_FILE_PATH)
      .setRecursive(true)
      .setSkipFiles(true)
      .setSkipDirectories(false)
      .build();

    // TEST_DIRS_NUM + empty dir
    for (int i = 0; i < TEST_DIRS_NUM + 1; i++) {
      Assert.assertTrue(iterator.hasNext());
      iterator.next();
    }
    Assert.assertFalse(iterator.hasNext());
  }

  @Test
  public void testIterateEmptyDirectory() throws IOException {
    GCSPathIterator iterator = GCSPathIterator.builder(bucket.getName() + "/" + EMPTY_DIRECTORY_NAME)
      .setProject(PROJECT)
      .setServiceAccountFilePath(SERVICE_ACCOUNT_FILE_PATH)
      .setRecursive(true)
      .setSkipFiles(false)
      .setSkipDirectories(true)
      .build();

    Assert.assertFalse(iterator.hasNext());
  }

  private static void deleteBucket(Storage storage, Bucket bucket) {
    for (Blob blob : bucket.list().iterateAll()) {
      storage.delete(blob.getBlobId());
    }
    bucket.delete(Bucket.BucketSourceOption.metagenerationMatch());
  }

  private static Storage getStorage(String project, @Nullable Credentials credentials) {
    StorageOptions.Builder builder = StorageOptions.newBuilder().setProjectId(project);
    if (credentials != null) {
      builder.setCredentials(credentials);
    }
    return builder.build().getService();
  }

  private static String testDirectoryName(int index) {
    return index + "/";
  }

  private static void createDirectory(String name) {
    bucket.create(name, new byte[]{});
  }

  private static Set<String> createEmptyFile(String directory, int filesNum) {
    Set<String> fileNames = new HashSet<>();
    for (int i = 0; i < filesNum; i++) {
      fileNames.add(createEmptyFile(directory));
    }
    return fileNames;
  }

  private static String createEmptyFile(String directory) {
    String fileName = directory + UUID.randomUUID().toString();
    bucket.create(fileName, new byte[]{});
    return String.format("gs://%s/%s", bucket.getName(), fileName);
  }

  private static String createEmptyFile() {
    String fileName = UUID.randomUUID().toString();
    bucket.create(fileName, new byte[]{});
    return String.format("gs://%s/%s", bucket.getName(), fileName);
  }

}
