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
package org.onebusaway.webapp.actions.admin.bundles;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.ServletContext;

import org.apache.struts2.convention.annotation.AllowedMethods;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.onebusaway.admin.bundle.AgencyList;
import org.onebusaway.admin.model.BundleBuildResponse;
import org.onebusaway.admin.model.BundleResponse;
import org.onebusaway.admin.model.ui.DirectoryStatus;
import org.onebusaway.admin.model.ui.ExistingDirectory;
import org.onebusaway.admin.service.BundleRequestService;
import org.onebusaway.admin.service.FileService;
import org.onebusaway.admin.service.bundle.GtfsArchiveService;
import org.onebusaway.admin.util.BundleInfo;
import org.onebusaway.admin.util.NYCFileUtils;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.onebusaway.webapp.actions.OneBusAwayNYCAdminActionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.ServletContextAware;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


/**
 * Action class that holds properties and methods required across all bundle building UI pages
 * @author abelsare
 * @author sheldonabrown
 *
 */
@Namespace(value="/admin/bundles")
@Results({
	@Result(type = "redirectAction", name = "redirect", 
			params={"actionName", "manage-bundles"}),
			@Result(name="selectDirectory", type="json", 
			params={"root", "directoryStatus"}),
			@Result(name="uploadStatus", type="json",
			params={"root", "directoryStatus"}),
			@Result(name="validationResponse", type="json", 
			params={"root", "bundleResponse"}),
			@Result(name="buildResponse", type="json", 
			params={"root", "bundleBuildResponse"}),
			@Result(name="fileList", type="json", 
			params={"root", "fileList"}),
			@Result(name="existingBuildList", type="json", 
			params={"root", "existingBuildList"}),
			@Result(name="downloadZip", type="stream", 
			params={"contentType", "application/zip", 
					"inputName", "downloadInputStream",
					"contentDisposition", "attachment;filename=\"output.zip\"",
					"bufferSize", "1024"}),
			@Result(name="download", type="stream",
			params={"contentType", "text/html",
					"inputName", "downloadInputStream",
					"contentDisposition", "attachment;filename=\"${downloadFilename}\"",
					"bufferSize", "1024"}),
      @Result(name="downloadGzip", type="stream",
      params={"contentType", "application/x-gzip",
          "inputName", "downloadInputStream",
          "contentDisposition", "attachment;filename=\"${downloadFilename}\"",
          "bufferSize", "1024"})
})
@AllowedMethods({"selectDirectory", "copyDirectory", "deleteDirectory", "createDirectory",
				"fileList", "updateBundleComments", "existingBuildList", "download", "buildList",
				"buildOutputZip", "downloadOutputFile", "downloadBundle", "downloadValidateFile"})
public class ManageBundlesAction extends OneBusAwayNYCAdminActionSupport implements ServletContextAware {
	private static Logger _log = LoggerFactory.getLogger(ManageBundlesAction.class);
	private static final long serialVersionUID = 1L;
	private static final String API_LATEST_BUNDLE = "/api/bundle/latest";

	private String bundleDirectory; // holds the final directory name 
	private String directoryName; // holds the value entered in the text box
	private String destDirectoryName; // holds copy directory name
	private boolean productionTarget;
	private String comments;
	private FileService fileService;
	private BundleRequestService bundleRequestService;
	private ConfigurationService configService;
	private GtfsArchiveService gtfsArchiveService;
	private static final int MAX_RESULTS = -1;
	private BundleResponse bundleResponse;
	private BundleBuildResponse bundleBuildResponse;
	private String id;
  private String downloadDataSet;
	private String downloadFilename;
	private InputStream downloadInputStream;
	private List<String> fileList = new ArrayList<String>();
	private String selectedBundleName;
	private SortedMap<String, String> existingBuildList = new TreeMap<String, String>();
	private DirectoryStatus directoryStatus;
	private BundleInfo bundleInfo;
	private boolean useArchivedGtfs;
	private String stagingDeployedDataset;
	private String stagingDeployedBundleName;
	// where the bundle is deployed to
	private String s3Path = "s3://bundle-data/activebundle/<env>/";
	private String environment = "dev";

	private static final String TRACKING_INFO = "info.json";

  @Autowired
  public void setBundleInfo(BundleInfo bundleInfo) {
    this.bundleInfo = bundleInfo;
  }

	@Override
	public String input() {
		_log.debug("in input");
		return SUCCESS;
	}

	/**
	 * Creates directory from existing directory
	 */
	@SuppressWarnings("unchecked")
	public String copyDirectory() {
		_log.info("Finally in copyDirectory action");		
		
    JSONObject sourceObject = bundleInfo.getBundleTrackingObject(directoryName);
		JSONObject destObject = new JSONObject();		
		destObject = sourceObject;
		destObject.put("directoryName", destDirectoryName);
		// Note that the bundle comments are part of the buildResponse object and
		// will not be retained on the copy.
		destObject.remove("buildResponse");
		destObject.remove("validationResponse");
		
		String createDirectoryMessage = null;
		boolean directoryCreated = false;
		String timestamp = "";

		if (destDirectoryName.contains(" ")){
			createDirectoryMessage = "Directory name cannot contain spaces. Please try again!";
		} else {
			if(fileService.bundleDirectoryExists(destDirectoryName)) {
				createDirectoryMessage = destDirectoryName + " already exists. Please try again!";
			} else {
				//Create the directory if it does not exist.
				directoryCreated = fileService.createBundleDirectory(destDirectoryName);
				
				// copy gtfs_latest and aux_latest 
				try {
          Path gtfsTempDir = Files.createTempDirectory("gtfs_latest");
          Path auxTempDir = Files.createTempDirectory("aux_latest");
          
          String srcGtfsDir = directoryName +  File.separator + fileService.getGtfsPath();
          String srcAuxDir = directoryName +  File.separator + fileService.getAuxPath();
          
          String gtfsCopy = fileService.get(srcGtfsDir, gtfsTempDir.toString());
          String auxCopy = fileService.get(srcAuxDir, auxTempDir.toString());

          String destGtfsDir = destDirectoryName + File.separator + fileService.getGtfsPath();
          String destAuxDir = destDirectoryName + File.separator + fileService.getAuxPath();
          fileService.put(destGtfsDir, gtfsTempDir.toString());
          fileService.put(destAuxDir, auxTempDir.toString());
          _log.info("copy complete");
          
        } catch (IOException e) {
          _log.error("Error copying directory:", e);
        }
				
				bundleDirectory = destDirectoryName;
				if(directoryCreated) {
					_log.info("Copied from: "+directoryName+ " to: "+destDirectoryName);
					directoryName = destDirectoryName;
					createDirectoryMessage = "Successfully copied into new directory: " + destDirectoryName;
					timestamp = fileService.getBundleDirTimestamp(destDirectoryName);
				} else {
					createDirectoryMessage = "Unable to create direcory: " + destDirectoryName;
				}
			}
		}		
		directoryStatus = createDirectoryStatus(createDirectoryMessage, directoryCreated, timestamp, destDirectoryName);
    bundleInfo.writeBundleTrackingInfo(destObject, destDirectoryName);
		directoryStatus.setBundleInfo(destObject);
		return "selectDirectory";
	}
	
  /**
   * Deletes an existing directory
   */
  @SuppressWarnings("unchecked")
  public String deleteDirectory() {
    boolean directoryDeleted = false;

    directoryDeleted = fileService.deleteBundleDirectory(directoryName);
    directoryStatus = createDirectoryStatus("Directory deleted", directoryDeleted, "", "");
    return "selectDirectory";
  }

	/**
	 * Creates directory for uploading bundles on AWS
	 */
	@SuppressWarnings("unchecked")
	public String createDirectory() {
		String createDirectoryMessage = null;
		boolean directoryCreated = false;
		String timestamp = "";
		if (directoryName.contains(" ")){
			createDirectoryMessage = "Directory name cannot contain spaces. Please try again!";
		} else {
			if(fileService.bundleDirectoryExists(directoryName)) {
				createDirectoryMessage = directoryName + " already exists. Please try again!";
			} else {
				//Create the directory if it does not exist.
				directoryCreated = fileService.createBundleDirectory(directoryName);
				bundleDirectory = directoryName;
				if(directoryCreated) {
					createDirectoryMessage = "Successfully created new directory: " +directoryName;
					timestamp = fileService.getBundleDirTimestamp(directoryName);
				} else {
					createDirectoryMessage = "Unable to create direcory: " +directoryName;
				}
			}
		}

		directoryStatus = createDirectoryStatus(createDirectoryMessage, directoryCreated, timestamp, directoryName);
		JSONObject obj = bundleInfo.getBundleTrackingObject(directoryName);
		if(obj == null){
			obj = new JSONObject();
		}
		obj.put("directoryName", directoryName);			
		bundleInfo.writeBundleTrackingInfo(obj, directoryName);

		return "selectDirectory";
	}

	public String selectDirectory() {
		List<String[]> existingDirectories = fileService.listBundleDirectories(MAX_RESULTS);
		bundleDirectory = directoryName;
		directoryStatus = createDirectoryStatus("Failed to find directory " + directoryName, false, null, directoryName);
		for (String[] directory: existingDirectories){
			if (directory[0].equals(directoryName)){
				directoryStatus = createDirectoryStatus("Loaded existing directory " + directoryName, true, null, directoryName);
				JSONObject bundleInfo = directoryStatus.getBundleInfo();
				// Update the agency list to reflect what's actually on the file system
				JSONArray updatedAgencyList = (new AgencyList(fileService)).updateAgencyList(directoryStatus, directoryName);
				if (bundleInfo != null && !bundleInfo.isEmpty()) {
				  bundleInfo.put("agencyList", updatedAgencyList);
				  directoryStatus.setBundleInfo(bundleInfo);
				}
				break;
			}
		}
		return "selectDirectory";
	}

	private DirectoryStatus createDirectoryStatus(String statusMessage, boolean selected, String timestamp, String directoryName) {
		DirectoryStatus directoryStatus = null;
		if(timestamp != null){
			directoryStatus = new DirectoryStatus(directoryName, statusMessage, selected, timestamp);
		}else {
			directoryStatus = new DirectoryStatus(directoryName, statusMessage, selected);
		}		 
		directoryStatus.setGtfsPath(fileService.getGtfsPath());
		directoryStatus.setAuxPath(fileService.getAuxPath());
		directoryStatus.setBucketName(fileService.getBucketName());	
		if(selected){
			JSONObject bundleObj = bundleInfo.getBundleTrackingObject(directoryName);
			if(bundleInfo != null && bundleObj != null) { //Added for JUnit
				if(!bundleObj.isEmpty()) {
					directoryStatus.setBundleInfo(bundleObj);
				}
				else {
					directoryStatus.setBundleInfo(null);
				}				
			}
		}
		return directoryStatus;
	}

	/**
	 * Returns the existing directories in the current bucket on AWS
	 * @return list of existing directories
	 */
	public Set<ExistingDirectory> getExistingDirectories() {
		_log.info("enter getExistingDirectories");
		Set<ExistingDirectory> directories = new TreeSet<ExistingDirectory>();
		try {
			List<String[]> existingDirectories = fileService.listBundleDirectories(MAX_RESULTS);

			for (String[] existingDirectory : existingDirectories) {
				ExistingDirectory directory = new ExistingDirectory(existingDirectory[0], existingDirectory[1],
								existingDirectory[2]);
				_log.info("directory=" + directory);
				directories.add(directory);
			}
		} catch (Throwable t) {
			_log.error("getExistingDirectories exception=" + t, t);
		} finally {
			_log.info("exit getExistingDirectories");
		}
		return directories;
	}

  /**
   * Returns the existing directories in the current bucket on AWS, but sorted
   * by date.
   * @return list of existing directories
   */
  public Set<ExistingDirectory> getSortedByDateDirectories() {
		Set<ExistingDirectory> directories = getExistingDirectories();
		_log.error("getSortedByDateDirectories existing directories input=" + directories);
  	try {
			// Resort by date
			Set<ExistingDirectory> sortedDirectories
							= new TreeSet<ExistingDirectory>(new DirectoryByDateComp());
			sortedDirectories.addAll(directories);
			_log.info("sorted directories output=" + sortedDirectories);
			return sortedDirectories;
		} catch (Throwable t) {
  		_log.error("getSortedByDateDirectories exception sorting directory ", t, t);
  		return directories;
		} finally {
  		_log.info("getSortedByDateDirectories exit");
		}
  }

  public SortedSet<String> getExistingArchivedDirectories() {
    SortedSet<String> existingArchivedDirectories = gtfsArchiveService.getAllDatasets();
    return existingArchivedDirectories;
  }

	@SuppressWarnings("unchecked")
	public String fileList() {
		_log.info("fileList called for id=" + id); 
		this.bundleResponse = bundleRequestService.lookupValidationRequest(getId());
		fileList.clear();
		if (this.bundleResponse != null) {
			fileList.addAll(this.bundleResponse.getValidationFiles());
		}

		//writing bundle information data in JSON
		JSONArray validationFiles = new JSONArray();
		JSONArray statusMessages = new JSONArray();
		JSONObject validationObj = new JSONObject();		
		JSONObject bundleObj = bundleInfo.getBundleTrackingObject(bundleResponse.getDirectoryName());		
		if(bundleObj == null || bundleObj.get("validationResponse") == null){
			validationObj = new JSONObject();
			bundleObj = new JSONObject();
		}else {
			validationObj = (JSONObject)bundleObj.get("validationResponse");

		}

		validationObj.put("bundleBuildName", bundleResponse.getBuildName());
		validationObj.put("requestId", this.bundleResponse.getId());
		for(String file : this.bundleResponse.getValidationFiles()){
			validationFiles.add(file);
		}
		validationObj.put("validationFiles", validationFiles);
		for(String msg : this.bundleResponse.getStatusMessages()){
			statusMessages.add(msg);
		}
		validationObj.put("statusMessages", statusMessages);
		bundleObj.put("validationResponse", validationObj);

		bundleInfo.writeBundleTrackingInfo(bundleObj, bundleResponse.getDirectoryName());
		return "fileList";
	}

  /**
   * Updates the bundle comments for the specified bundle
   */
  @SuppressWarnings("unchecked")
  public String updateBundleComments() {
    JSONObject buildObj = new JSONObject();
    JSONObject bundleObj = bundleInfo.getBundleTrackingObject(directoryName);
    if (bundleObj == null) {
      bundleObj = new JSONObject();
    }
    if(bundleObj.get("buildResponse") != null) {
      buildObj = (JSONObject)bundleObj.get("buildResponse");
    }
    buildObj.put("comment", comments);
    bundleObj.put("buildResponse", buildObj);
    bundleInfo.writeBundleTrackingInfo(bundleObj, directoryName);
    return "uploadStatus";
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

	public String download() {
		this.bundleResponse = bundleRequestService.lookupValidationRequest(getId());
		_log.info("download=" + this.downloadFilename + " and id=" + id);
		if (this.bundleResponse != null) {
			this.downloadInputStream = new NYCFileUtils().read(this.bundleResponse.getTmpDirectory() + File.separator + this.downloadFilename);
			return "download";
		}
		// TODO
		_log.error("bundleResponse not found for id=" + id);
		return "error";
	}

	@SuppressWarnings("unchecked")
	public String buildList() {
		_log.info("buildList called with id=" + id);
		this.bundleBuildResponse = this.bundleRequestService.lookupBuildRequest(getId());
		if (this.bundleBuildResponse != null) {
			fileList.addAll(this.bundleBuildResponse.getOutputFileList());
		}

		//writing bundle information data in JSON
		JSONArray buildFiles = new JSONArray();
		JSONArray statusMessages = new JSONArray();
		JSONObject buildObj = new JSONObject();		
    JSONObject bundleObj = bundleInfo.getBundleTrackingObject(bundleBuildResponse.getBundleDirectoryName());
		if (bundleObj == null) {
		  bundleObj = new JSONObject();
		}
		if(bundleObj.get("buildResponse") == null){
			buildObj = new JSONObject();
		}else {
			buildObj = (JSONObject)bundleObj.get("buildResponse");

		}

		buildObj.put("requestId", bundleBuildResponse.getId());
		buildObj.put("bundleBuildName", bundleBuildResponse.getBundleBuildName());
		buildObj.put("startDate", bundleBuildResponse.getBundleStartDate());
		buildObj.put("endDate", bundleBuildResponse.getBundleEndDate());
		buildObj.put("email", bundleBuildResponse.getBundleEmailTo());
		buildObj.put("comment", bundleBuildResponse.getBundleComment());
		for(String file : this.bundleBuildResponse.getOutputFileList()){
			buildFiles.add(file);
		}
		buildObj.put("buildOutputFiles", buildFiles);
		for(String msg : this.bundleBuildResponse.getStatusList()){
			statusMessages.add(msg);
		}
		buildObj.put("statusMessages", statusMessages);
		bundleObj.put("buildResponse", buildObj);

    bundleInfo.writeBundleTrackingInfo(bundleObj, bundleBuildResponse.getBundleDirectoryName());
		return "fileList";
	}

	public String buildOutputZip() {
		_log.info("buildOuputZip called with id=" +id);
		bundleBuildResponse = bundleRequestService.lookupBuildRequest(getId());
		String zipFileName = fileService.createOutputFilesZip(bundleBuildResponse.getRemoteOutputDirectory());
		try {
			downloadInputStream = new FileInputStream(zipFileName);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "downloadZip";
	}

	public String downloadOutputFile() {
		_log.info("downloadOutputFile with id=" + id + " and file=" + this.downloadFilename);
		fileService.validateFileName(downloadFilename);
		try {
			this.bundleBuildResponse = this.bundleRequestService.lookupBuildRequest(getId());
		} catch (Throwable t) {
			_log.error("transaction issue " + t, t);
		}
		if (this.bundleBuildResponse != null) {
		  String dir = bundleBuildResponse.getRemoteOutputDirectory();
		  if (dir == null) {
		    // something went very wrong but we don't have enough information to know what
		    // return link to bundlebuilder log and hope that contains the relevant info to debug
		    _log.error("bundle build response did not have a valid remote output dir!");
		    this.downloadInputStream = createDefaultSummaryStream();
		    return "download";
		  }
			String s3Key = dir + File.separator + this.downloadFilename;
			_log.info("get request for s3Key=" + s3Key);
			try {
			  this.downloadInputStream = this.fileService.get(s3Key);
			} catch (Exception e) {
			  // bundle build failed and summary was not written to disk
			  // return a standard version so the log can be examined
			  this.downloadInputStream = createDefaultSummaryStream();
			}
			return "download";
		}
		// TODO
		return "error";
	}

	private InputStream createDefaultSummaryStream() {
	  String summary = "filename,description,lines\n";
	  return new ByteArrayInputStream(summary.getBytes());
	}
	
	public String downloadBundle() {
    fileService.validateFileName(downloadFilename);
    String s3Key = this.downloadDataSet + "/builds/" + this.downloadFilename
        + "/" + this.downloadFilename + ".tar.gz";
    this.downloadFilename += ".tar.gz";
    this.downloadInputStream = this.fileService.get(s3Key);
    return "downloadGzip";
	}

	public String downloadValidateFile() {
		this.bundleResponse = this.bundleRequestService.lookupValidationRequest(getId());
		fileService.validateFileName(downloadFilename);
		if (this.bundleResponse != null) {
			String s3Key = bundleResponse.getRemoteOutputDirectory() + File.separator + this.downloadFilename;
			_log.info("get request for s3Key=" + s3Key);
			this.downloadInputStream = this.fileService.get(s3Key);
			return "download";
		}
		// TODO
		_log.error("validate file not found for id=" + getId());
		return "error";
	}

	public String getDirectoryName() {
		return directoryName;
	}

	public void setDirectoryName(String directoryName) {
		this.directoryName = directoryName;
	}

	/**
	 * @return the productionTarget
	 */
	public boolean isProductionTarget() {
		return productionTarget;
	}

	/**
	 * @param productionTarget the productionTarget to set
	 */
	public void setProductionTarget(boolean productionTarget) {
		this.productionTarget = productionTarget;
	}

	/**
	 * @return the comments
	 */
	public String getComments() {
		return comments;
	}

	/**
	 * @param comments the comments to set
	 */
	public void setComments(String comments) {
		this.comments = comments;
	}

	/**
	 * @param fileService the fileService to set
	 */
	@Autowired
	public void setFileService(FileService fileService) {
		this.fileService = fileService;
	}

	/**
	 * @return the bundleDirectory
	 */
	public String getBundleDirectory() {
		return bundleDirectory;
	}

	/**
	 * @param bundleDirectory the bundleDirectory to set
	 */
	public void setBundleDirectory(String bundleDirectory) {
		this.bundleDirectory = bundleDirectory;
	}

	/**
	 * Injects {@link BundleRequestService}
	 * @param bundleRequestService the bundleRequestService to set
	 */
	@Autowired
	public void setBundleRequestService(BundleRequestService bundleRequestService) {
		this.bundleRequestService = bundleRequestService;
	}

	/**
	 * Injects {@link ConfigurationService}
	 * @param configService the configService to set
	 */
	@Autowired
	public void setConfigurationService(ConfigurationService configService) {
		this.configService = configService;
	}

  @Autowired
  public void setGtfsArchiveService(GtfsArchiveService gtfsArchiveService) {
    this.gtfsArchiveService = gtfsArchiveService;
  }

	public BundleResponse getBundleResponse() {
		return bundleResponse;
	}

	public BundleBuildResponse getBundleBuildResponse() {
		return bundleBuildResponse;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public String getDeployedBundle() {
		String apiHostname = configService.getConfigurationValueAsString(
				"apiHostname", null);
		if (apiHostname != null) {
			String apiHost = apiHostname + "/api/where/config.json?key=TEST";  
			try {
				return getJsonData(apiHost).getAsJsonObject()
						.getAsJsonObject("data").getAsJsonObject("entry")
						.get("name").getAsString();
			} catch (Exception e2) {
				_log.error("Failed to retrieve name of the latest deployed bundle (apiHost=["+ apiHost+"])");
			}

		}
		String tdmHost = System.getProperty("tdm.host") + "/api/bundle/list";
		try {
			return getJsonData(tdmHost).getAsJsonObject()
					.getAsJsonArray("bundles").get(0).getAsJsonObject()
					.get("name").getAsString();
		} catch (Exception e) {
			_log.error("Failed to retrieve name of the latest deployed bundle (tdmHost=["+tdmHost+"])");
		}
		return "";
	}

	private JsonElement getJsonData (String spec) throws IOException{
		URL url = new URL((!spec.toLowerCase().matches("^\\w+://.*")?"http://":"") + spec);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		return new JsonParser().parse(in.readLine());
	}

	public void setSelectedBundleName(String selectedBundleName) {
		this.selectedBundleName = selectedBundleName;
	}

	public String getSelectedBundleName() {
		return selectedBundleName;
	}

	public DirectoryStatus getDirectoryStatus() {
		return directoryStatus;
	}

	public InputStream getDownloadInputStream() {
		return this.downloadInputStream;
	}

  public void setDownloadDataSet(String name) {
    this.downloadDataSet = name;
  }

  public String getDownloadDataSet() {
    return this.downloadDataSet;
  }

	public void setDownloadFilename(String name) {
		this.downloadFilename = name;
	}

	public String getDownloadFilename() {
		return this.downloadFilename;
	}

	public List<String> getFileList() {
		return this.fileList;
	}

	public SortedMap<String, String> getExistingBuildList() {
		return this.existingBuildList;
	}

	public void setEmailTo(String to) {
	}

	public String getStagingDeployedDataset() {
	  if (stagingDeployedDataset == null || stagingDeployedDataset.isEmpty()) {
	    getStagingDeployedBundleData();
	  }
	  return stagingDeployedDataset;
  }

  public void setStagingDeployedDataset(String stagingDeployedDataset) {
    this.stagingDeployedDataset = stagingDeployedDataset;
  }

  public String getStagingDeployedBundleName() {
    if (stagingDeployedBundleName == null || stagingDeployedBundleName.isEmpty()) {
      getStagingDeployedBundleData();
    }
    return stagingDeployedBundleName;
  }

  public void setStagingDeployedBundleName(String stagingDeployedBundleName) {
    this.stagingDeployedBundleName = stagingDeployedBundleName;
  }

  private void getStagingDeployedBundleData() {
    String adminStagingHost = configService.getConfigurationValueAsString(
        "adminStaging", null);
    String adminStagingPort = configService.getConfigurationValueAsString(
        "adminStagingPort", null);
    String adminStagingUrl = adminStagingHost + ":" + adminStagingPort
        + API_LATEST_BUNDLE;
    JsonObject latestBundle = null;
    try {
      latestBundle = getJsonData(adminStagingUrl).getAsJsonObject();
    }  catch (Exception e) {
      _log.error("Failed to retrieve name of the latest deployed bundle at: "
			  + adminStagingUrl);
    }
    stagingDeployedDataset = latestBundle.get("dataset").getAsString();
    stagingDeployedBundleName = latestBundle.get("name").getAsString();
  }

  public String getS3Path() {
		return s3Path;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setUseArchivedGtfs(boolean useArchivedGtfs) {
	  this.useArchivedGtfs = useArchivedGtfs;
	}

	@Override
	public void setServletContext(ServletContext context) {
		if (context != null) {
			String obanycEnv = context.getInitParameter("obanyc.environment");
			if (obanycEnv != null && obanycEnv.length() > 0) {
				String rootDir = context.getInitParameter("s3.bundle.bucketName");
				if (rootDir == null  || rootDir.length() == 0) {
					rootDir = context.getInitParameter("file.bundle.bucketName");
				} else {
					rootDir = "s3://" + rootDir;
				}
				environment = obanycEnv;
				s3Path = rootDir
						+ "/activebundles/" + environment
						+ "/";
				_log.info("injecting env=" + environment + ", s3Path=" + s3Path);
			}
		}
	}

	public String getDestDirectoryName() {
		return destDirectoryName;
	}

	public void setDestDirectoryName(String destDirectoryName) {
		this.destDirectoryName = destDirectoryName;
	}

	private class DirectoryByDateComp implements Comparator<ExistingDirectory> {
	  @Override
	  public int compare(ExistingDirectory ed1, ExistingDirectory ed2) {
	  	try {
	  		return unsafeCompare(ed1, ed2);
		} catch (Throwable t) {
	  		_log.error("compare failed: " + t);
		}
		return 0;
	  }

	  private int unsafeCompare(ExistingDirectory ed1, ExistingDirectory ed2) throws Exception {
		  // ExistingDirectory.creationTimestamp is a String 'dow mon dd hh:mm:ss zzz yyyy'
			try {
				String[] ed1Split = ed1.getCreationTimestamp().split(" ");
				String[] ed2Split = ed2.getCreationTimestamp().split(" ");
				SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss dd-MMM-yyyy");
				Date ed1Date = new Date();
				Date ed2Date = new Date();
				ed1Date = formatter.parse(ed1Split[3]
								+ " " + ed1Split[2] + "-" + ed1Split[1]
								+ "-" + ed1Split[5]);
				ed2Date = formatter.parse(ed2Split[3]
								+ " " + ed2Split[2] + "-" + ed2Split[1]
								+ "-" + ed2Split[5]);
				_log.info("" + ed1Date + " ?= " + ed2Date);
				int difference = ed1Date.compareTo(ed2Date) * -1;
				if (difference == 0) {
					return ed1.getName().compareTo(ed2.getName());
				}
				return difference;
			} catch (Throwable t) {
				_log.error("date issue " + t + " for ed1=" + ed1 + " and ed2=" + ed2, t);
				return -1;
			}

	  }
	}
}
