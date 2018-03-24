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
package org.onebusaway.admin.bundle;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.onebusaway.admin.model.ui.DirectoryStatus;
import org.onebusaway.admin.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 
 * This class contains utility methods for dealing with lists of agencies for
 * a particular data bundle.  These are used by the Transit Data Bundle Utility
 * in building a bundle.
 *
 */
public class AgencyList {
  private static Logger _log = LoggerFactory.getLogger(AgencyList.class);
  private static final long serialVersionUID = 1L;

  private FileService fileService;

  public AgencyList(FileService fileService) {
    this.fileService = fileService;
  }
  
  /**
   * This method will create an agencyList JSON array based on the directories
   * and files currently in this bundle directory.  That list will then be
   * updated based on any additional information in the bundleInfo data from
   * the file info.json.
   *
   * @param directoryStatus contains info about the bundle build process
   * @param directoryName name of the directory for this bundle
   * @return the updated agency list
   */
  public JSONArray updateAgencyList(DirectoryStatus directoryStatus,
      String directoryName) {
    JSONArray newAgencyList = agencyListFromFiles(directoryName);
    newAgencyList = updateFromLastAgencyList(newAgencyList, directoryStatus);
    return newAgencyList;
  }

  /**
   * This creates an agencyList JSON array based on the directories
   * and files currently in this bundle directory.
   *
   * @param directoryName name of the directory for this bundle
   * @return a newly constructed agencyList constructed based on the directories
   *         and files currently on the file system.
   */
  private JSONArray agencyListFromFiles(String directoryName) {
    JSONArray newAgencyList = new JSONArray();
    String basePath = fileService.getBucketName();

    // add gtfs files
    String fullPath = basePath + "/" + directoryName + "/" + fileService.getGtfsPath();
    File[] agencyDirs = (new File(fullPath)).listFiles();
    if (agencyDirs != null) {
      for (File agencyDir : agencyDirs) {
        String agencyId = agencyDir.getName();
        String agencyPath = fullPath + "/" + agencyId;
        File mostRecentFile = getMostRecentGtfsFile(agencyDir);
        JSONObject agencyRecord = createAgencyRecord(mostRecentFile, "gtfs", agencyId);
        newAgencyList.add(agencyRecord);
      }
    }

    // add aux files
    fullPath = basePath + "/" + directoryName + "/" + fileService.getAuxPath();
    agencyDirs = (new File(fullPath)).listFiles();
    if (agencyDirs != null) {
      for (File agencyDir : agencyDirs) {
        String agencyId = agencyDir.getName();
        String agencyPath = fullPath + "/" + agencyId;
        File[] auxFiles = agencyDir.listFiles();
        for (File auxFile : auxFiles) {
          JSONObject agencyRecord = createAgencyRecord(auxFile, "aux", agencyId);
          newAgencyList.add(agencyRecord);
        }
      }
    }

    return newAgencyList;
  }

  /**
   * Gets the most recent file in a GTFS directory for an agency.  There should
   * only be one file anyway, but this is needed in case additional versions of
   * the file have been manually added.
   *
   * @param agencyDir the name of the directory for this agency, generally the
   *                  same as the agency id.
   * @return the most recent file in this GTFS directory.
   */
  private File getMostRecentGtfsFile(File agencyDir) {
    File[] uploadedFiles =  agencyDir.listFiles();
    File mostRecentFile = null;
    if (uploadedFiles != null && uploadedFiles.length > 0) {
      mostRecentFile = uploadedFiles[0];
      for (File existingFile : uploadedFiles) {
        if (existingFile.lastModified() > mostRecentFile.lastModified()) {
          mostRecentFile = existingFile;
        }
      }
    }
    return mostRecentFile;
  }

  /**
   * Creates a new agency record to be added to the agencyList.
   *
   * @param bundleFile the file to be added
   * @param dataSourceType 'aux' or 'http'
   * @param agencyId the agency id
   * @return the new agency record
   */
  private JSONObject createAgencyRecord(File bundleFile, String dataSourceType, String agencyId) {
    JSONObject agencyRecord = new JSONObject();
    if (bundleFile == null) return agencyRecord;
    String fileDate = new SimpleDateFormat("MMM dd yyyy").format(new Date(bundleFile.lastModified()));
    agencyRecord.put("agencyBundleUploadDate", fileDate);
    agencyRecord.put("agencyDataSource", bundleFile.getName());
    agencyRecord.put("agencyDataSourceType", dataSourceType);
    agencyRecord.put("agencyId", agencyId);
    agencyRecord.put("agencyProtocol", "file");
    return agencyRecord;
  }

  /**
   * Updates the new agencyList based on any additional information in the
   * bundleInfo data from the file info.json.  More specifically, if an entry
   * from the bundleInfo matches for agency id, filename, and date, then the
   * data source (URL for this file) and protocol are updated from the
   * bundleInfo data.
   *
   * @param newAgencyList the newly created agencyList
   * @param directoryStatus contains info about the bundle build process
   * @return the updated agencyList
   */
  private JSONArray updateFromLastAgencyList(JSONArray newAgencyList, DirectoryStatus directoryStatus) {
    JSONObject bundleInfo = directoryStatus.getBundleInfo();
    JSONArray agencyList = bundleInfo == null ? null
        : (JSONArray)bundleInfo.get("agencyList");
    if (agencyList != null) {
      for (int i=0; i < agencyList.size(); ++i) {
        JSONObject agency = (JSONObject)agencyList.get(i);
        String currentFilename = (String)agency.get("agencyDataSource");
        if (currentFilename != null) {
          currentFilename = currentFilename.substring(currentFilename.lastIndexOf("/") + 1);
        } else {
          currentFilename = "";
        }
        for (int j=0; j<newAgencyList.size(); ++j) {
          JSONObject updatedAgency = (JSONObject)newAgencyList.get(j);
          if (agency.get("agencyId").equals(updatedAgency.get("agencyId"))
              && currentFilename.equals(updatedAgency.get("agencyDataSource"))
              && agency.get("agencyBundleUploadDate")
                .equals(updatedAgency.get("agencyBundleUploadDate"))) {
            updatedAgency.put("agencyDataSource", agency.get("agencyDataSource"));
            updatedAgency.put("agencyProtocol", agency.get("agencyProtocol"));
            newAgencyList.set(j, updatedAgency);
          }
        }
      }
    }
    return newAgencyList;
  }
}
