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
package org.onebusaway.admin.bundle.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BundleStatus implements Serializable {


  private static final long serialVersionUID = 1L;
  public static final String STATUS_STARTED = "in_progress";
  public static final String STATUS_COMPLETE = "complete";
  public static final String STATUS_ERROR = "error";
  public static final String STATUS_STAGING_COMPLETE = "staging_complete";
  
  private String id = null;
  private String status = "initialized";
  private List<String> bundleNames = Collections.synchronizedList(new ArrayList<String>());
  
  public List<String> getBundleNames() {
    return new ArrayList<String>(bundleNames);
  }
  
  public void setBundleNames(List<String> bundleNames) {
    this.bundleNames = bundleNames;
  }
  
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }

  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }

  public void addBundleName(String bundleFilename) {
	bundleNames.add(bundleFilename);
  }

}
