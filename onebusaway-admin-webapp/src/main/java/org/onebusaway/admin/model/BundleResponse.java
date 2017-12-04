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
package org.onebusaway.admin.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BundleResponse {
  private boolean _isComplete = false;
  private Exception _exception;
  private List<String> _statusMessages = Collections.synchronizedList(new ArrayList<String>());
  private List<String> _validationFiles = Collections.synchronizedList(new ArrayList<String>());
  private String _tmpDirectory = null;
  private String _remoteOutputDirectory;
  private String directoryName;
  private String buildName;
  private String _id = null;
  
  
  public BundleResponse() {
    // for JSON serialization
  }
  
  public String toString() {
    return "BundleResponse{[" + _id + "], complete=" + _isComplete + "}"; 
  }
  
  public BundleResponse(String id) {
    _id = id;
  }
  
  public synchronized  boolean isComplete() {
    return _isComplete;
  }

  public synchronized  void setException(Exception any) {
    _exception = any;
  }

  public Exception getException() {
    return _exception;
  }

  public List<String> getStatusMessages() {
    return new ArrayList<String>(_statusMessages);
  }
  
  public void addStatusMessage(String msg) {
    _statusMessages.add(msg);
  }

  // for JSON serialization only
  public void setStatusMessages(List<String> statusMessages) {
    _statusMessages = statusMessages;
  }
  
  public synchronized void setComplete(boolean b) {
    _isComplete = b;
  }

  public List<String> getValidationFiles() {
    return new ArrayList<String>(_validationFiles);
  }

  public void addValidationFile(String filename) {
    _validationFiles.add(filename);
  }

  // for JSON serialization only
  public void setValidationFiles(List<String> validationFiles) {
    _validationFiles = validationFiles;
  }
  
  public String getTmpDirectory() {
    return _tmpDirectory;
  }
  
  public void setTmpDirectory(String dir) {
    _tmpDirectory = dir;
  }
  /**
   * key for retrieval from lookup service
   */
  public String getId() {
    return _id;
  }
  
  public void setId(String id) {
    _id = id;
  }

  public void setRemoteOutputDirectory(String directoryPath) {
    _remoteOutputDirectory = directoryPath;
  }

   public String getRemoteOutputDirectory() {
    return _remoteOutputDirectory;
  }

   public String getDirectoryName() {
	   return directoryName;
   }

   public void setDirectoryName(String directoryName) {
	   this.directoryName = directoryName;
   }

   public String getBuildName() {
	   return buildName;
   }

   public void setBuildName(String buildName) {
	   this.buildName = buildName;
   }

}
