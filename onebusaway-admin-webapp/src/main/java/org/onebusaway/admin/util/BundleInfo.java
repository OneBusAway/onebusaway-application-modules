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
package org.onebusaway.admin.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.onebusaway.admin.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class BundleInfo {
  private static Logger _log = LoggerFactory.getLogger(BundleInfo.class);
  private FileService fileService;

  private static final String TRACKING_INFO = "info.json";

  @Autowired
  public void setFileService(FileService fileService) {
    this.fileService = fileService;
  }

  public JSONObject getBundleTrackingObject(String bundleDirectory) {
    String pathname = fileService.getBucketName() + File.separatorChar + bundleDirectory + File.separatorChar + TRACKING_INFO;    
    File file = new File(pathname);
    JSONObject bundleObj =  null;
    JSONParser parser = new JSONParser();
    try{              
      if(!file.exists()){
        file.createNewFile(); 
        bundleObj = new JSONObject();
      }else{
        Object obj = parser.parse(new FileReader(file));
        bundleObj = (JSONObject) obj;
      }
    }
    catch(Exception e){
      _log.error(e.getMessage(), e);
      _log.error("configured pathname was " + pathname);
      bundleObj = new JSONObject();
    }
    return bundleObj;
  }

  public void writeBundleTrackingInfo(JSONObject bundleObject, String directoryName) {

    if(fileService.getBucketName() != null){
      String pathname = fileService.getBucketName() + File.separatorChar
          + directoryName + File.separatorChar + TRACKING_INFO;

      File file = new File(pathname);   
      FileWriter handle = null;

      try {     
        if(!file.exists()){
          file.createNewFile();
        }

        handle = new FileWriter(file);
        if(bundleObject != null){
          handle.write(bundleObject.toJSONString());
        }   
        handle.flush();
      }
      catch(Exception e){
        _log.error("Bundle Tracker Writing:: " +e.getMessage());
      }
      finally{
        try{
          if (handle != null) {
            handle.close();
          }
        }catch(IOException ie){
          _log.error("Bundle Tracker Writing :: File Handle Failed to Close");
        }
      }
    }
  }


  public synchronized boolean addFileToBundleForDirectoryName(String bundleDir,
                                              String agencyId,
                                              String agencyProtocol,
                                              Date uploadDate) {
    try {
      _log.info("addFileToBundleForDirectoryName for {}/{}", agencyId, bundleDir);
      JSONObject bundleTrackingObject = getBundleTrackingObject(bundleDir);
      JSONObject agencyObj = new JSONObject();
      if (bundleTrackingObject != null) {
        JSONArray agencyList = new JSONArray();
        if (bundleTrackingObject.get("agencyList") != null) {
          agencyList = (JSONArray) bundleTrackingObject.get("agencyList");
        }
        agencyObj.put("agencyId", agencyId);
        agencyObj.put("agencyDataSource", "gtfs");
        agencyObj.put("agencyDataSourceType", "gtfs");
        agencyObj.put("agencyProtocol", agencyProtocol);
        agencyObj.put("agencyBundleUploadDate",
                new SimpleDateFormat("MMM dd yyyy").format(uploadDate));
        agencyList.add(agencyObj);
        bundleTrackingObject.put("agencyList", agencyList);
        _log.info("writeBundleTrackingInfo {}/{} of {}", agencyId, bundleDir, bundleTrackingObject.toJSONString());
        writeBundleTrackingInfo(bundleTrackingObject, bundleDir);
      }
    } catch (Exception e) {
      _log.info("addFileToBundleForDirectoryName blew for {}/{}", agencyId, bundleDir, e, e);
      return false;
    }
    return true;
  }
}
