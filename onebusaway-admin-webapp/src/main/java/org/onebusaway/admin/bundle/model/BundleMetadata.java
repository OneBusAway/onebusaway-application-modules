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

import java.util.Date;
import java.util.List;


public class BundleMetadata {

  private String id;
  private String name;
  private Date serviceDateFrom;
  private Date serviceDateTo;
  private List<BundleFile> outputFiles;
  private List<SourceFile> sourceData;
  private String changeLogUri;
  private String statisticsUri;
  private String validationUri;
  
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public Date getServiceDateFrom() {
    return serviceDateFrom;
  }
  public void setServiceDateFrom(Date serviceDateFrom) {
    this.serviceDateFrom = serviceDateFrom;
  }
  public Date getServiceDateTo() {
    return serviceDateTo;
  }
  public void setServiceDateTo(Date serviceDateTo) {
    this.serviceDateTo = serviceDateTo;
  }
  public List<BundleFile> getOutputFiles() {
	  return outputFiles;
  }
  public void setOutputFiles(List<BundleFile> bundleFiles) {
    outputFiles = bundleFiles;	
  }
  
  public List<SourceFile> getSourceData() {
    return sourceData;
  }
  public void setSourceData(List<SourceFile> sourceFilesWithSumsForDirectory) {
    this.sourceData = sourceFilesWithSumsForDirectory;
  }
  public String getChangeLogUri() {
    return changeLogUri;
  }
  public void setChangeLogUri(String uri) {
    this.changeLogUri = uri;
  }
  public String getStatisticsUri() {
    return statisticsUri;
  }
  public void setStatisticsUri(String uri) {
    this.statisticsUri = uri;
  }
  public String getValidationUri() {
    return validationUri;
  }
  public void setValidationUri(String uri) {
    this.validationUri = uri;
  }
  
}
