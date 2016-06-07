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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletContext;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;
import org.onebusaway.admin.model.BundleBuildResponse;
import org.onebusaway.admin.model.BundleResponse;
import org.onebusaway.admin.model.ui.DirectoryStatus;
import org.onebusaway.admin.model.ui.ExistingDirectory;
import org.onebusaway.admin.service.BundleRequestService;
import org.onebusaway.admin.service.DiffService;
import org.onebusaway.admin.service.FileService;
import org.onebusaway.admin.util.NYCFileUtils;
import org.onebusaway.agency_metadata.service.AgencyMetadataService;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.onebusaway.webapp.actions.OneBusAwayNYCAdminActionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.ServletContextAware;

import com.google.gson.JsonElement;
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
			@Result(name="diffResult", type="json", 
			params={"root", "diffResult"}),
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
public class ManageBundlesAction extends OneBusAwayNYCAdminActionSupport implements ServletContextAware {
	private static Logger _log = LoggerFactory.getLogger(ManageBundlesAction.class);
	private static final long serialVersionUID = 1L;

	private String bundleDirectory; // holds the final directory name 
	private String directoryName; // holds the value entered in the text box
	private String destDirectoryName; // holds copy directory name
	private String bundleBuildName; // holds the build name selected in the Compare tab
	private String bundleName; // what to call the bundle, entered in the text box
	private String agencyId; // agencyId from the Upload tab
	private String agencyDataSourceType; // 'gtfs' or 'aux', from the Upload tab
	private String agencyProtocol;  // 'http', 'ftp', or 'file', from the Upload tab
	private String agencyDataSource; // URL for the source data file, from the Upload tab
	private boolean cleanDir;    // on file upload, should target directory be cleaned first
	private File agencySourceFile;
	private String agencySourceFileContentType;
	private String agencySourceFileFileName;
	private boolean productionTarget;
	private String comments;
	private FileService fileService;
	private BundleRequestService bundleRequestService;
	private ConfigurationService configService;
	private static final int MAX_RESULTS = -1;
	private BundleResponse bundleResponse;
	private BundleBuildResponse bundleBuildResponse;
	private String id;
  private String downloadDataSet;
	private String downloadFilename;
	private InputStream downloadInputStream;
	private List<String> fileList = new ArrayList<String>();
	private String diffBundleName;
	private String diffBuildName;
	private List<String> existingBuildList = new ArrayList<String>();
	private List<String> diffResult = new ArrayList<String>();
	private DirectoryStatus directoryStatus;
	// where the bundle is deployed to
	private String s3Path = "s3://bundle-data/activebundle/<env>/";
	private String environment = "dev";
	private DiffService diffService;
	private static final String TRACKING_INFO = "info.json";

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
		
		JSONObject sourceObject = getBundleTrackingObject(directoryName);
		JSONObject destObject = new JSONObject();		
		destObject = sourceObject;
		destObject.put("directoryName", destDirectoryName);
		
		String createDirectoryMessage = null;
		boolean directoryCreated = false;
		String timestamp = "";

		_log.debug("in copy directory with dir=" + destDirectoryName);

		if (destDirectoryName.contains(" ")){
			_log.info("destination bundle dir contains a space");
			createDirectoryMessage = "Directory name cannot contain spaces. Please try again!";
		} else {
			if(fileService.bundleDirectoryExists(destDirectoryName)) {
				_log.info("bundle dir exists");
				createDirectoryMessage = destDirectoryName + " already exists. Please try again!";
			} else {
				_log.info("creating bundledir=" + destDirectoryName);
				//Create the directory if it does not exist.
				directoryCreated = fileService.createBundleDirectory(destDirectoryName);
				
				// copy gtfs_latest and aux_latest 
				try {
          Path gtfsTempDir = Files.createTempDirectory("gtfs_latest");
          Path auxTempDir = Files.createTempDirectory("aux_latest");
          
          String s3GtfsKey = directoryName +  File.separator + fileService.getGtfsPath();
          String s3AuxKey = directoryName +  File.separator + fileService.getAuxPath();
          
          String gtfsCopy = fileService.get(s3GtfsKey, gtfsTempDir.toString());
          String auxCopy = fileService.get(s3AuxKey, auxTempDir.toString());

          String newS3GtfsKey = destDirectoryName + File.separator + fileService.getGtfsPath();
          String newS3AuxKey = destDirectoryName + File.separator + fileService.getAuxPath();
          fileService.put(newS3GtfsKey, gtfsCopy);
          fileService.put(newS3AuxKey, auxCopy);
          _log.info("copy complete");
          
        } catch (IOException e) {
          _log.error("Error copying directory:", e);
        }
				
				bundleDirectory = destDirectoryName;
				if(directoryCreated) {
					_log.info("Copied from: "+directoryName+ " to: "+destDirectoryName);
					directoryName = destDirectoryName;
					createDirectoryMessage = "Successfully copied into new directory: " + destDirectoryName;
					createDirectoryMessage += ". Validate and Build now!";
					timestamp = fileService.getBundleDirTimestamp(destDirectoryName);
				} else {
					createDirectoryMessage = "Unable to create direcory: " + destDirectoryName;
				}
			}
		}		
		directoryStatus = createDirectoryStatus(createDirectoryMessage, directoryCreated, timestamp, destDirectoryName);
		writeBundleTrackingInfo(destObject, destDirectoryName);
		directoryStatus.setBundleInfo(destObject);
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

		_log.debug("in create directory with dir=" + directoryName);

		if (directoryName.contains(" ")){
			_log.info("bundle dir contains a space");
			createDirectoryMessage = "Directory name cannot contain spaces. Please try again!";
		} else {
			if(fileService.bundleDirectoryExists(directoryName)) {
				_log.info("bundle dir exists");
				createDirectoryMessage = directoryName + " already exists. Please try again!";
			} else {
				_log.info("creating bundledir=" + directoryName);
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
		JSONObject obj = getBundleTrackingObject(directoryName);
		if(obj == null){
			obj = new JSONObject();
		}
		obj.put("directoryName", directoryName);			
		writeBundleTrackingInfo(obj, directoryName);

		return "selectDirectory";
	}

	private void writeBundleTrackingInfo(JSONObject bundleObject, String directoryName) {
		if(fileService.getBucketName() != null){
			String pathname = fileService.getBucketName() + File.separatorChar + directoryName + File.separatorChar + TRACKING_INFO;
			
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
					handle.close();
				}catch(IOException ie){
					_log.error("Bundle Tracker Writing :: File Handle Failed to Close");
				}
			}
		}
	}

	public String selectDirectory() {
		List<String[]> existingDirectories = fileService.listBundleDirectories(MAX_RESULTS);
		_log.info("in selectDirectory with dirname=" + directoryName);
		bundleDirectory = directoryName;
		directoryStatus = createDirectoryStatus("Failed to find directory " + directoryName, false, null, directoryName);
		for (String[] directory: existingDirectories){
			if (directory[0].equals(directoryName)){
				directoryStatus = createDirectoryStatus("Loaded existing directory " + directoryName, true, null, directoryName);
				JSONObject bundleInfo = directoryStatus.getBundleInfo();
				// Update the agency list to reflect what's actually on the file system
				JSONArray updatedAgencyList = updateAgencyList(directoryStatus, directoryName);
				bundleInfo.put("agencyList", updatedAgencyList);
				directoryStatus.setBundleInfo(bundleInfo);
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
			JSONObject bundleInfo = getBundleTrackingObject(directoryName);
			if(bundleInfo != null) { //Added for JUnit
				if(!bundleInfo.isEmpty()) {
					directoryStatus.setBundleInfo(bundleInfo);
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
		List<String[]> existingDirectories = fileService.listBundleDirectories(MAX_RESULTS);
		Set<ExistingDirectory> directories = new TreeSet<ExistingDirectory> ();
		for(String[] existingDirectory : existingDirectories) {
			ExistingDirectory directory = new ExistingDirectory(existingDirectory[0], existingDirectory[1], 
					existingDirectory[2]);
			directories.add(directory);
		}
		return directories;
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
		JSONObject bundleObj = getBundleTrackingObject(bundleResponse.getDirectoryName());		
		if(bundleObj.get("validationResponse") == null){
			validationObj = new JSONObject();
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

		writeBundleTrackingInfo(bundleObj, bundleResponse.getDirectoryName());
		return "fileList";
	}

  /**
   * Updates the bundle comments for the specified bundle
   */
  @SuppressWarnings("unchecked")
  public String updateBundleComments() {
    JSONObject buildObj = new JSONObject();
    JSONObject bundleObj = getBundleTrackingObject(directoryName);
    if (bundleObj == null) {
      bundleObj = new JSONObject();
    }
    if(bundleObj.get("buildResponse") != null) {
      buildObj = (JSONObject)bundleObj.get("buildResponse");
    }
    buildObj.put("comment", comments);
    bundleObj.put("buildResponse", buildObj);
    writeBundleTrackingInfo(bundleObj, directoryName);
    return "uploadStatus";
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
		target += src.substring(src.lastIndexOf('/'));
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
		JSONObject bundleObj = getBundleTrackingObject(directoryName);
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
		writeBundleTrackingInfo(bundleObj, directoryName);
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
		JSONObject bundleObj = getBundleTrackingObject(directoryName);
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
		writeBundleTrackingInfo(bundleObj, directoryName);
		return "uploadStatus";
	}	  

	public String existingBuildList() {
		_log.info("existingBuildList called for path=" + fileService.getBucketName()+"/"+ diffBundleName +"/"+fileService.getBuildPath()); 
		File builds = new File(fileService.getBucketName()+"/"+ diffBundleName +"/"+fileService.getBuildPath());
		File[] existingDirectories = builds.listFiles();
		existingBuildList.clear();
		if(existingDirectories == null){
			return null;
		}
		for(File file: existingDirectories) {
			existingBuildList.add(file.getName());
		}
		return "existingBuildList";
	}

	public String diffResult() {
		String currentBundlePath = fileService.getBucketName() + File.separator 
				+ bundleDirectory + "/builds/" + bundleName + "/outputs/gtfs_stats.csv"; 
		String selectedBundlePath = fileService.getBucketName()
				+ File.separator
				+ diffBundleName + "/builds/"
				+ diffBuildName + "/outputs/gtfs_stats.csv";
		diffResult.clear();
		diffResult = diffService.diff(selectedBundlePath, currentBundlePath);
		return "diffResult";
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
		JSONObject bundleObj = getBundleTrackingObject(bundleBuildResponse.getBundleDirectoryName());
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

		writeBundleTrackingInfo(bundleObj, bundleBuildResponse.getBundleDirectoryName());
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
		this.bundleBuildResponse = this.bundleRequestService.lookupBuildRequest(getId());
		if (this.bundleBuildResponse != null) {
			String s3Key = bundleBuildResponse.getRemoteOutputDirectory() + File.separator + this.downloadFilename;
			_log.info("get request for s3Key=" + s3Key);
			this.downloadInputStream = this.fileService.get(s3Key);
			return "download";
		}
		// TODO
		return "error";
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

	/**
	 * @return the directoryName
	 */
	public String getDirectoryName() {
		return directoryName;
	}

	/**
	 * @param directoryName the directoryName to set
	 */
	public void setDirectoryName(String directoryName) {
		this.directoryName = directoryName;
	}

	/**
	 * @return the bundleBuildName
	 */
	public String getBundleBuildName() {
		return bundleBuildName;
	}

	/**
	 * @param bundleBuildName
	 */
	public void setBundleBuildName(String bundleBuildName) {
		this.bundleBuildName = bundleBuildName;
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
	 * @param diffService the diffService to set
	 */
	@Autowired
	public void setDiffService(DiffService diffService) {
		this.diffService = diffService;
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

	public void setBundleName(String bundleName) {
		this.bundleName = bundleName;
	}

	public String getBundleName() {
		return bundleName;
	}

	public void setAgencyId(String agencyId) {
		this.agencyId = agencyId;
	}

	public String getAgencyId() {
		return agencyId;
	}

	public void setAgencyDataSourceType(String agencyDataSourceType) {
		this.agencyDataSourceType = agencyDataSourceType;
	}

	public String getAgencyDataSourceType() {
		return agencyDataSourceType;
	}

	public void setAgencyProtocol(String agencyProtocol) {
		this.agencyProtocol = agencyProtocol;
	}

	public String getAgencyProtocol() {
		return agencyProtocol;
	}

	public void setAgencyDataSource(String agencyDataSource) {
		this.agencyDataSource = agencyDataSource;
	}

	public String getAgencyDataSource() {
		return agencyDataSource;
	}

	public void setCleanDir(boolean cleanDir) {
		this.cleanDir = cleanDir;
	}

	public boolean getCleanDir() {
		return cleanDir;
	}

	public void setAgencySourceFile(File agencySourceFile) {
		this.agencySourceFile = agencySourceFile;
	}

	public File getAgencySourceFile(File agencySourceFile) {
		return agencySourceFile;
	}

	public void setAgencySourceFileContentType(String agencySourceFileContentType) {
		this.agencySourceFileContentType = agencySourceFileContentType;
	}

	public void setAgencySourceFileFileName(String agencySourceFileFileName) {
		this.agencySourceFileFileName = agencySourceFileFileName;
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

	public void setDiffBundleName(String diffBundleName) {
		this.diffBundleName = diffBundleName;
	}

	public String getDiffBundleName() {
		return diffBundleName;
	}

	public void setDiffBuildName(String diffBuildName) {
		this.diffBuildName = diffBuildName;
	}

	public String getDiffBuildName() {
		return diffBuildName;
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

	public List<String> getExistingBuildList() {
		return this.existingBuildList;
	}

	public List<String> getDiffResult() {
		return this.diffResult;
	}

	public void setEmailTo(String to) {
	}

	public String getS3Path() {
		return s3Path;
	}

	public String getEnvironment() {
		return environment;
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

	private JSONObject getBundleTrackingObject(String bundleDirectory) {
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
		}

		return bundleObj;
	}

	public FileService getFileService() {
		return fileService;

	}

	/**
	 * @return the destDirectoryName
	 */
	public String getDestDirectoryName() {
		return destDirectoryName;
	}

	/**
	 * @param destDirectoryName the destDirectoryName to set
	 */
	public void setDestDirectoryName(String destDirectoryName) {
		this.destDirectoryName = destDirectoryName;
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
  private JSONArray updateAgencyList(DirectoryStatus directoryStatus,
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
    if (uploadedFiles.length > 0) {
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
