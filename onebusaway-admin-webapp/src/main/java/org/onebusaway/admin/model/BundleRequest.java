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

public class BundleRequest {
  private String _id;
  private String _bundleDirectory;
  private String _tmpDirectory;
  private String _bundleBuildName;
  private String _sessionId;
  
  public String getBundleDirectory() {
    return _bundleDirectory;
  }
  
  public void setBundleDirectory(String dir) {
    _bundleDirectory = dir;
  }

  public String getTmpDirectory() {
    return _tmpDirectory;
  }
  
  public void setTmpDirectory(String dir) {
    _tmpDirectory = dir;
  }

  public String getBundleBuildName() {
    return _bundleBuildName;
  }
  
  public void setBundleBuildName(String bundleName) {
    this._bundleBuildName = bundleName;
  }

  public String getId() {
    return _id;
  }
  
  public void setId(String id) {
    _id = id;
  }
  
  public String getSessionId() {
	  return _sessionId;
  }
  
  public void setSessionId(String sessionId) {
	  _sessionId = sessionId;
  }
}
