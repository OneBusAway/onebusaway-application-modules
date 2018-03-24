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

import org.apache.commons.io.FileUtils;
import org.onebusaway.admin.bundle.model.BundleStatus;
import org.onebusaway.admin.service.RemoteConnectionService;
import org.onebusaway.admin.service.bundle.BundleStager;
import org.onebusaway.util.FileUtility;
import org.onebusaway.util.services.configuration.ConfigurationServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class DirectoryBundleStagerImpl implements BundleStager {
  
  private static final int MAX_RESULTS = -1;
  
  private static Logger _log = LoggerFactory
      .getLogger(DirectoryBundleStagerImpl.class);
  
  private String _builtBundleDirectory;
  private String _stagingDirectory;
  
  private FileUtility _fileUtil;
  
  
  @Autowired
  private ConfigurationServiceClient _configClient;
  
  @Autowired
  private RemoteConnectionService _remoteConnectionService;

  public void setBuiltBundleDirectory(String _builtBundleDirectory) {
    this._builtBundleDirectory = _builtBundleDirectory;
  }

  public void setStagedBundleDirectory(String _stagingDirectory) {
    this._stagingDirectory = _stagingDirectory;
  }
  
  public String getBuiltBundleDirectory() {
    return _builtBundleDirectory;
  }

  public String getStagedBundleDirectory() {
    return _stagingDirectory;
  }
  
  @PostConstruct
  public void setup() {
      _fileUtil = new FileUtility();
  }
  
  private void stageBundle(BundleStatus status, String environment, String bundleDir, String bundleName)
      throws Exception {
    File srcDir = new File(this.getBuiltBundleDirectory()
        + File.separator + bundleDir + File.separator + "builds"
        + File.separator + bundleName);
    File srcFile = new File(srcDir, bundleName + ".tar.gz");
    File destDir = new File(this.getStagedBundleDirectory());
    _log.info("deleting " + destDir);
    // cleanup from past run
    try {
      FileUtils.deleteDirectory(destDir);
    } catch (Exception any) {
      _log.error("deleteDir failed with :", any);
    }
    
    try{
      _log.info("making directory" + destDir);
      destDir.mkdir();
    } catch (Exception any) {
      _log.warn("unable to create  directory :", any);
      throw any;
    }
    
    try{
      _log.info("expanding " + srcFile + " to " + destDir);
      _fileUtil.unTargz(srcFile, destDir);
      status.addBundleName(bundleName);
    } catch(Exception any){
      _log.error("unable to successfully unTar bundle :" + any);
      throw any;
    }  
  }
  
  public List<String> listBundlesForServing(String path) {
    List<String> bundleFiles = new ArrayList<String>();
    List<String> bundlePaths = listFiles(path, MAX_RESULTS);
    for (String bundle : bundlePaths) {
      bundleFiles.add(parseFileName(bundle, File.separator));
    }
    return bundleFiles;
  }
  
  
  
  public void stage(BundleStatus status, String environment, String bundleDir, String bundleName){
    try {
      status.setStatus(BundleStatus.STATUS_STARTED);
      stageBundle(status, environment, bundleDir, bundleName);
      status.setStatus(BundleStatus.STATUS_COMPLETE);
    } catch (Exception e) {
      status.setStatus(BundleStatus.STATUS_ERROR);
    }
  }
  
  public void notifyOTP(String bundleName) throws Exception {
    String otpNotificationUrl = _configClient.getItem("admin", "otpNotificationUrl");
    if (otpNotificationUrl == null) return;
    otpNotificationUrl = otpNotificationUrl.replaceAll(":uuid", (bundleName==null?"":bundleName));
    _remoteConnectionService.getContent(otpNotificationUrl);
  }
  
  private String parseFileName(String fullFileName, String separator) {
    
    int i = fullFileName.lastIndexOf(separator);
    
    if (i+1 < fullFileName.length()) {
      return fullFileName.substring(i+1, fullFileName.length());
    }
    if (i >= 0) {
      return fullFileName.substring(i, fullFileName.length());
    }
    return fullFileName;
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
}
