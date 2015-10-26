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
package org.onebusaway.admin.service.bundle.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.onebusaway.admin.bundle.model.BundleStatus;
import org.onebusaway.admin.service.bundle.BundleDeployer;
import org.onebusaway.admin.util.NYCFileUtils;
import org.onebusaway.util.FileUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectoryBundleDeployerImpl implements BundleDeployer {
  
  private static final int MAX_RESULTS = -1;
  private static Logger _log = LoggerFactory
      .getLogger(DirectoryBundleDeployerImpl.class);
  
  private FileUtility _fileUtil;
  private NYCFileUtils _nycFileUtils;
  private String _deployBundleDirectory;
  
  public void setDeployBundleDirectory(String localBundlePath) {
    _deployBundleDirectory = localBundlePath;
  }
  
  @PostConstruct
  public void setup() {
      _fileUtil = new FileUtility();
      _nycFileUtils = new NYCFileUtils();
  }
  
  @Override
  public List<String> listStagedBundles(String path) {
    List<String> bundleFiles = new ArrayList<String>();
    List<String> bundlePaths = listFiles(path, MAX_RESULTS);
    for (String bundle : bundlePaths) {
      bundleFiles.add(_nycFileUtils.parseFileName(bundle, File.separator));
    }
    return bundleFiles;
  }
  
  private List<String> listFiles(String directory, int maxResults){
    File bundleDir = new File(directory);
    int fileCount = 1;
    List<String> fileList;
      if(maxResults > 0)
        fileList = new ArrayList<String>(maxResults);
      else
        fileList = new ArrayList<String>();
    if (bundleDir.isDirectory()) {
      for (File bundleFile : bundleDir.listFiles()) {
        if (maxResults >= 0 && fileCount > maxResults)
          break;
        else {
          try {
            fileList.add(bundleFile.getCanonicalPath());
          } catch (IOException e) {
            _log.error("Unable to access " + bundleFile.getName());
            _log.error(e.toString(), e);
            continue;
          }
          fileCount++;
        }
      }
    }
    return fileList;
  }

  /**
   * Copy the bundle from Staging to the Admin Server's bundle serving location, and arrange
   * as necessary.
   */
  private int deployBundleForServing(BundleStatus status, String path) throws Exception{
    _log.info("deployBundleForServing(" + path + ")");

    int bundlesDownloaded = 0;
    // list bundles at given path
    List<String> bundles = listFiles(path, MAX_RESULTS);

    if (bundles != null && !bundles.isEmpty()) {
      clearBundleDeployDirectory();
    } else {
      _log.error("no bundles found at path=" + path);
      return bundlesDownloaded;
    }

    for (String bundle : bundles) {
      String bundleFilename = _nycFileUtils.parseFileName(bundle, File.separator);
      // retreive bundle and add it to the list of bundles
      try{
        _log.info("getting bundle = " + bundle);
        get(bundle, _deployBundleDirectory);
        status.addBundleName(bundleFilename);
      } catch(Exception e){
        _log.error("exception deploying bundle=" + bundle);
        throw e;
      }
    }

    // don't cleanup, staging should mirror deploy
    return bundlesDownloaded;
  }

  /**
   * delete bundle deploy directory.
   */
  private void clearBundleDeployDirectory(){
    _log.info("wiping bundle deploy directory");
    try {
      _fileUtil.delete(new File(_deployBundleDirectory));
    } catch (IOException ioe) {
      _log.error("error wiping bundle dir:", ioe);
    }
    new File(_deployBundleDirectory).mkdir();
  }

  /**
   * Retrieve the specified bundle file and store in the given directory.
   */
  public String get(String bundlePath, String destinationDirectory) throws Exception {
    _log.info("get(" + bundlePath + ", " + destinationDirectory + ")");
    String filename = _nycFileUtils.parseFileName(bundlePath, File.separator);
    _log.info("filename=" + filename);
    try {
      _fileUtil.moveDir(bundlePath, destinationDirectory);
    } catch (IOException e) {
      _log.error("exception copying bundle from " + bundlePath + " to " + destinationDirectory, e);
      throw e;
    } catch (Exception e) {
      _log.error("exception copying bundle from " + bundlePath + " to " + destinationDirectory, e);
      throw e;
    }
    return destinationDirectory;
  }

  
  @Override
  /**
   * Transfer bundles from staging directory to active bundles directory for serving 
   */
  public void deploy(BundleStatus status, String path) {
    try {
      status.setStatus(BundleStatus.STATUS_STARTED);
      deployBundleForServing(status, path);
      status.setStatus(BundleStatus.STATUS_COMPLETE);
    } catch (Exception e) {
      status.setStatus(BundleStatus.STATUS_ERROR);
    }
  }

}
