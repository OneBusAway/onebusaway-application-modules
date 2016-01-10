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
package org.onebusaway.admin.model.ui;

import org.json.simple.JSONObject;

/**
 * DTO for sending created/selected directory status to UI
 * @author abelsare
 *
 */
public class DirectoryStatus {
	private String directoryName;
	private String message;
	private boolean selected;
	private String gtfsPath;
	private String auxPath;
	private String bucketName;
	private String timestamp;
	private JSONObject bundleInfo;

	public DirectoryStatus(String directoryName, String message, boolean selected) {
	  this(directoryName, message, selected, "");
	}

	public DirectoryStatus(String directoryName, String message, boolean selected, String timestamp) {
		this.directoryName = directoryName;
		this.message = message;
		this.selected = selected;
		this.timestamp = timestamp;
	}

	public String getDirectoryName() {
		return directoryName;
	}

	public String getMessage() {
		return message;
	}

	public boolean isSelected() {
		return selected;
	}

	/**
	 * @return the gtfsPath
	 */
	public String getGtfsPath() {
		return gtfsPath;
	}

	/**
	 * @param gtfsPath the gtfsPath to set
	 */
	public void setGtfsPath(String gtfsPath) {
		this.gtfsPath = gtfsPath;
	}

	/**
	 * @return the auxPath for stif/hastus support
	 */
	public String getAuxPath() {
		return auxPath;
	}

	/**
	 * @param stifPath path for stif/hastus support
	 */
	public void setAuxPath(String auxPath) {
		this.auxPath = auxPath;
	}

	/**
	 * @return the bucketName
	 */
	public String getBucketName() {
		return bucketName;
	}

	/**
	 * @param bucketName the bucketName to set
	 */
	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}
	
	/**
	 * @return the timestamp
	 */
	 public String getTimestamp() {
	   return timestamp;
	 }
	 
	 /**
	  * @param timestampe the timestamp to set
	  */
	 public void setTimestamp(String timestamp) {
	   this.timestamp = timestamp;
	 }

	public JSONObject getBundleInfo() {
		return bundleInfo;
	}

	public void setBundleInfo(JSONObject bundleInfo) {
		this.bundleInfo = bundleInfo;
	}
}
