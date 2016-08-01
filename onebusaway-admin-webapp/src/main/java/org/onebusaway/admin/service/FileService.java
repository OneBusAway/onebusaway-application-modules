/**
 * Copyright (C) 2015 Cambridge Systematics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.admin.service;

import java.io.InputStream;
import java.util.List;

public interface FileService {
  void setup();
  void setUser(String user);
  void setPassword(String password);
  void setBucketName(String bucketName);
  void setGtfsPath(String gtfsPath);
  String getGtfsPath();
  void setAuxPath(String auxPath);
  String getAuxPath();
  void setBuildPath(String buildPath);
  String getConfigPath();
  void setConfigPath(String configPath);
  String getBuildPath();
  String getBucketName();
  String getBundleDirTimestamp(String dir);
  
  boolean bundleDirectoryExists(String filename);

  boolean createBundleDirectory(String filename);

  boolean deleteBundleDirectory(String filename);

  List<String[]> listBundleDirectories(int maxResults);

  String get(String s3path, String tmpDir);
  InputStream get(String s3Path);
  String put(String key, String directory);
  
  List<String> list(String directory, int maxResults);
  
  /**
   * Creates a zip of all the output files generated in the given bundle directory during bundle building process
   * @param directoryName bundle outpur directory name
   * @return name of the zip file created
   */
  String createOutputFilesZip(String directoryName);
  
  /**
   * Validates that given file name does not contain characters which could lead to directory 
   * traversal attack.
   * @param fileName the given file name
   */
  void validateFileName(String fileName);
  

}
