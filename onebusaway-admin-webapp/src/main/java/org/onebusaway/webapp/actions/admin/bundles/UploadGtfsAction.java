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
package org.onebusaway.webapp.actions.admin.bundles;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.struts2.convention.annotation.AllowedMethods;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.onebusaway.admin.service.FileService;
import org.onebusaway.admin.util.BundleInfo;
import org.onebusaway.webapp.actions.OneBusAwayNYCAdminActionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Namespace(value="/admin/bundles")
@Results({
  @Result(name="uploadStatus", type="json",
  params={"root", "directoryStatus"})
})
@AllowedMethods({"uploadSourceData"})
public class UploadGtfsAction extends OneBusAwayNYCAdminActionSupport {
  private static Logger _log = LoggerFactory.getLogger(UploadGtfsAction.class);
  private static final long serialVersionUID = 1L;
  private FileService fileService;

  private String directoryName;   // holds the value entered in the text box
  private String agencyId; // agencyId from the Upload tab
  private String agencyDataSourceType; // 'gtfs' or 'aux', from the Upload tab
  private String agencyProtocol;  // 'http', 'ftp', or 'file', from the Upload tab
  private String agencyDataSource; // URL for the source data file, from the Upload tab
  private boolean cleanDir;    // on file upload, should target directory be cleaned first
  private File agencySourceFile;
  private BundleInfo bundleInfo;
  private String agencySourceFileFileName;
  private String agencySourceFileContentType;

  @Autowired
  public void setFileService(FileService fileService) {
    this.fileService = fileService;
  }

  public void setDirectoryName(String directoryName) {
    this.directoryName = directoryName;
  }

  public void setAgencyId(String agencyId) {
    this.agencyId = agencyId;
  }

  public void setAgencyDataSourceType(String agencyDataSourceType) {
    this.agencyDataSourceType = agencyDataSourceType;
  }

  public void setAgencyProtocol(String agencyProtocol) {
    this.agencyProtocol = agencyProtocol;
  }

  public void setAgencyDataSource(String agencyDataSource) {
    this.agencyDataSource = agencyDataSource;
  }

  public void setCleanDir(boolean cleanDir) {
    this.cleanDir = cleanDir;
  }

  public void setAgencySourceFile(File agencySourceFile) {
    this.agencySourceFile = agencySourceFile;
  }

  @Autowired
  public void setBundleInfo(BundleInfo bundleInfo) {
    this.bundleInfo = bundleInfo;
  }

  public void setAgencySourceFileFileName(String agencySourceFileFileName) {
    this.agencySourceFileFileName = agencySourceFileFileName;
  }

  public void setAgencySourceFileContentType(String agencySourceFileContentType) {
    this.agencySourceFileContentType = agencySourceFileContentType;
  }

  /**
   * Uploads the bundle source data for the specified agency
   */
  @SuppressWarnings("unchecked")
  public String uploadSourceData() {
    _log.info("uploadSourceData called"); 
    _log.info("agencyId: " + agencyId + ", agencyDataSourceType: " + agencyDataSourceType + ", agencyProtocol: " + agencyProtocol 
        + ", agencyDataSource: " + agencyDataSource);
    _log.info("gtfs path: " + fileService.getGtfsPath());
    _log.info("aux path: " + fileService.getAuxPath());
    _log.info("build path: " + fileService.getBuildPath());
    _log.info("directory name: " + directoryName);
    _log.info("base path: " + fileService.getBucketName());
    _log.info("cleanDir: " + cleanDir);
    // Build URL/File path
    String src = agencyDataSource;
    if (agencyProtocol.equals("http")) {
      if (src.startsWith("//")) {
        src = "http:" + src;
      } else if (src.startsWith("/")) {
        src = "http:/" + src;
      } else if (!src.toLowerCase().startsWith("http")) {
        src = "http://" + src;
      }
    } else if (agencyProtocol.equals("ftp")) {
      if (src.startsWith("//")) {
        src = "ftp:" + src;
      } else if (src.startsWith("/")) {
        src = "ftp:/" + src;
      } else if (!src.toLowerCase().startsWith("ftp")) {
        src = "ftp://" + src;
      }      
    }
    _log.info("Source: " + src);

    // Build target path
    String target = fileService.getBucketName() + "/" + directoryName + "/";
    if (agencyDataSourceType.equals("gtfs")) {
      target += fileService.getGtfsPath();
    } else {
      target += fileService.getAuxPath();
    }
    target += "/" + agencyId;
    // Clean target directory before appending the file name to the target string
    if (cleanDir) {
      File targetDir = new File(target);
      if (targetDir.exists()) {
        for (File file: targetDir.listFiles()) {
          file.delete();
        }
      }
    }
    // name it something safe -- as url may have stuff in it
    target += File.separator + agencyId + ".zip";
    _log.info("Target: " + target);

    // Copy file
    if (agencyProtocol.equals("http") || agencyProtocol.equals("ftp")) {
      try {
        URL website = new URL(src);
        File targetPath = new File(target);
        targetPath.mkdirs();
        Files.copy(website.openStream(), targetPath.toPath(), StandardCopyOption.REPLACE_EXISTING);
      } catch (Exception e) {
        _log.info(e.getMessage());
      }
    }
    //Writing JSON for agency just uploaded
    JSONArray agencyList = null;    
    JSONObject agencyObj = new JSONObject();
    JSONObject bundleObj = bundleInfo.getBundleTrackingObject(directoryName);
    if(bundleObj.get("agencyList") == null){
      agencyList = new JSONArray();
    }else {
      agencyList = (JSONArray)bundleObj.get("agencyList");
    }

    // Don't retain any existing agencyList entries for this agency.
    JSONArray updatedAgencyList = new JSONArray();
    for (Object existingObj : agencyList) {
      JSONObject existingAgencyObj = (JSONObject) existingObj;
      String existingAgencyId = (String)existingAgencyObj.get("agencyId");
      if (existingAgencyId != null &&  !existingAgencyId.equals(agencyId)) {
        updatedAgencyList.add(existingObj);
      }
    }
    agencyList = updatedAgencyList;

    agencyObj.put("agencyId", agencyId);
    agencyObj.put("agencyDataSource", agencyDataSource);
    agencyObj.put("agencyDataSourceType", agencyDataSourceType);
    agencyObj.put("agencyProtocol", agencyProtocol);  
    agencyObj.put("agencyBundleUploadDate", 
        new SimpleDateFormat("MMM dd yyyy").format(new Date()));
    agencyList.add(agencyObj);
    bundleObj.put("agencyList", agencyList);
    bundleInfo.writeBundleTrackingInfo(bundleObj, directoryName);
    return "uploadStatus";
  }

  @SuppressWarnings("unchecked")
  public String uploadSourceFile() {
    _log.info("in uploadSourceFile");
    _log.info("agencyId: " + agencyId + ", agencyDataSourceType: " + agencyDataSourceType);
    _log.info("gtfs path: " + fileService.getGtfsPath());
    _log.info("aux path: " + fileService.getAuxPath());
    _log.info("build path: " + fileService.getBuildPath());
    _log.info("directory name: " + directoryName);
    _log.info("base path: " + fileService.getBucketName());
    _log.info("upload file name: " + agencySourceFileFileName);
    _log.info("file content type: " + agencySourceFileContentType);
    _log.info("file name: " +  agencySourceFile.getName());
    _log.info("cleanDir: " + cleanDir);

    // Build target path
    String target = fileService.getBucketName() + "/" + directoryName + "/";
    if (agencyDataSourceType.equals("gtfs")) {
      target += fileService.getGtfsPath();
    } else {
      target += fileService.getAuxPath();
    }
    target += "/" + agencyId;
    // Clean target directory before appending the file name to the target string
    if (cleanDir) {
      File targetDir = new File(target);
      if (targetDir.exists()) {
        for (File file: targetDir.listFiles()) {
          file.delete();
        }
      }
    }   
    target += "/" + agencySourceFileFileName;
    _log.info("Target: " + target);

    // Copy file
    try {
      File targetPath = new File(target);
      targetPath.mkdirs();
      Files.copy(agencySourceFile.toPath(), targetPath.toPath(), StandardCopyOption.REPLACE_EXISTING);
    } catch (Exception e) {
      _log.info(e.getMessage());
    }

    //Writing JSON for agency just uploaded
    JSONArray agencyList = null;    
    JSONObject agencyObj = new JSONObject();
    JSONObject bundleObj = bundleInfo.getBundleTrackingObject(directoryName);
    if(bundleObj.get("agencyList") == null){
      agencyList = new JSONArray();
    }else {
      agencyList = (JSONArray)bundleObj.get("agencyList");
    }

    // Don't retain any existing agencyList entries for this agency.
    JSONArray updatedAgencyList = new JSONArray();
    for (Object existingObj : agencyList) {
      JSONObject existingAgencyObj = (JSONObject) existingObj;
      String existingAgencyId = (String)existingAgencyObj.get("agencyId");
      if (existingAgencyId != null &&  !existingAgencyId.equals(agencyId)) {
        updatedAgencyList.add(existingObj);
      }
    }
    agencyList = updatedAgencyList;

    agencyObj.put("agencyId", agencyId);
    agencyObj.put("agencyDataSource", agencyDataSource);
    agencyObj.put("agencyDataSourceType", agencyDataSourceType);
    agencyObj.put("agencyProtocol", agencyProtocol);  
    agencyObj.put("agencyBundleUploadDate", 
        new SimpleDateFormat("MMM dd yyyy").format(new Date()));
    agencyList.add(agencyObj);
    bundleObj.put("agencyList", agencyList);
    bundleInfo.writeBundleTrackingInfo(bundleObj, directoryName);
    return "uploadStatus";
  }   

}
