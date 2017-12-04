/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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
package org.onebusaway.webapp.actions.admin.bundlereports;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.admin.model.ui.ExistingDirectory;
import org.onebusaway.admin.service.FileService;
import org.onebusaway.admin.service.bundle.GtfsArchiveService;
import org.onebusaway.webapp.actions.OneBusAwayNYCAdminActionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Results({
    @Result (
      name="success", location ="fixed-route-comparison.jspx"
    ),
    @Result(name="existingBuildList", type="json", 
    params={"root", "existingBuildList"})
  })

public class BundleReportsAction extends OneBusAwayNYCAdminActionSupport {
  private static Logger _log = LoggerFactory.getLogger(BundleReportsAction.class);
  private static final long serialVersionUID = 1L;

  private FileService fileService;
  private GtfsArchiveService gtfsArchiveService;
  private String selectedBundleName;
  private SortedMap<String, String> existingBuildList = new TreeMap<String, String>();
  private boolean useArchivedGtfs;
  private static final int MAX_RESULTS = -1;

  @Autowired
  public void setFileService(FileService fileService) {
    this.fileService = fileService;
  }

  public void setSelectedBundleName(String selectedBundleName) {
    this.selectedBundleName = selectedBundleName;
  }

  public String getSelectedBundleName() {
    return selectedBundleName;
  }

  @Autowired
  public void setGtfsArchiveService(GtfsArchiveService gtfsArchiveService) {
    this.gtfsArchiveService = gtfsArchiveService;
  }

  public SortedMap<String, String> getExistingBuildList() {
    return this.existingBuildList;
  }

  public void setUseArchivedGtfs(boolean useArchivedGtfs) {
    this.useArchivedGtfs = useArchivedGtfs;
  }

  @Override
  public String execute() {
    
    return SUCCESS;
  }

  /**
   * Returns the existing directories in the current bucket on AWS
   * @return list of existing directories
   */
  public Set<ExistingDirectory> getExistingDirectories() {
    List<String[]> existingDirectories = fileService.listBundleDirectories(MAX_RESULTS);
    Set<ExistingDirectory> directories = new TreeSet<ExistingDirectory> ();
    for(String[] existingDirectory : existingDirectories) {
      ExistingDirectory directory = new ExistingDirectory(existingDirectory[0], existingDirectory[1], 
          existingDirectory[2]);
      directories.add(directory);
    }
    return directories;
  }

  public SortedSet<String> getExistingArchivedDirectories() {
    SortedSet<String> existingArchivedDirectories = gtfsArchiveService.getAllDatasets();
    return existingArchivedDirectories;
  }

  public String existingBuildList() {
    existingBuildList.clear();
    if (!useArchivedGtfs) {
      _log.info("existingBuildList called for path=" + fileService.getBucketName()+"/"+ selectedBundleName +"/"+fileService.getBuildPath());
      File builds = new File(fileService.getBucketName()+"/"+ selectedBundleName +"/"+fileService.getBuildPath());
      File[] existingDirectories = builds.listFiles();
      if(existingDirectories == null){
        return null;
      }
      int i = 1;
      for(File file: existingDirectories) {
        existingBuildList.put(file.getName(), ""+i++);
      }
    } else {
      existingBuildList = gtfsArchiveService.getBuildNameMapForDataset(selectedBundleName);
    }

    return "existingBuildList";
  }


}
