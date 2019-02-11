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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(name="bundle_build_response")
@org.hibernate.annotations.Table(appliesTo ="bundle_build_response", indexes = {
    @Index(name = "bundle_build_response_id_idx", columnNames = {"id"}),
    })
@org.hibernate.annotations.Entity(mutable = true)

public class BundleBuildResponse {

  /* Sound Transit constants */
  private static final int BUNDLE_ROOT_DIR_LEN = 255;
  private static final int BUNDLE_INPUT_DIR_LEN = 255;
  private static final int BUNDLE_OUTPUT_DIR_LEN = 255;
  private static final int BUNDLE_DATA_DIR_LEN = 255;
  private static final int BUNDLE_TAR_FILENAME_LEN = 255;
  private static final int REMOTE_INPUT_DIR_LEN = 255;
  private static final int REMOTE_OUTPUT_DIR_LEN = 255;
  private static final int REMOTE_OUTPUT_GTFS_DIR_LEN = 255;
  private static final int VERSION_STRING_LEN = 255;
  private static final int TMP_DIR_LEN = 255;
  private static final int BUNDLE_BUILD_NAME_LEN = 255;
  private static final int BUNDLE_START_DATE_LEN = 20;
  private static final int BUNDLE_END_DATE_LEN = 20;
  private static final int BUNDLE_COMMENT_LEN = 4096;
  private static final int BUNDLE_DIR_NAME_LEN = 255;
  private static final int BUNDLE_EMAIL_TO_LEN = 255;
  private static final int BUNDLE_RESULTS_LINK_LEN = 4096;
  private static final int BUNDLE_ID_LEN = 255;
  private static final int ID_LEN = 10;
  private static final int NAME_LEN = 255;
  private static final int STATUS_LEN = 4096;
  private static final int EX_MSG_LEN = 4096;
  private static final int EX_ROOT_CAUSE_LEN = 4096;

  @Id
  @Column(nullable = true, name="id", length = ID_LEN)
  private String id = null;

  @ElementCollection
  @LazyCollection (LazyCollectionOption.FALSE)
  @Column(nullable = true, name="gtfs_list", length = NAME_LEN)
	private List<String> _gtfsList = Collections.synchronizedList(new ArrayList<String>());

  @ElementCollection
  @LazyCollection (LazyCollectionOption.FALSE)
  @Column(nullable = true, name="aux_zip_list", length = NAME_LEN)
	private List<String> _auxZipList = Collections.synchronizedList(new ArrayList<String>());

  @ElementCollection
  @LazyCollection (LazyCollectionOption.FALSE)
  @Column(nullable = true, name="config_list", length = NAME_LEN)
	private List<String> _configList = Collections.synchronizedList(new ArrayList<String>());

  @ElementCollection
  @LazyCollection (LazyCollectionOption.FALSE)
  @Column(nullable = true, name="status_list", length = STATUS_LEN)
	private List<String> _statusList = Collections.synchronizedList(new ArrayList<String>());

  @ElementCollection
  @LazyCollection (LazyCollectionOption.FALSE)
  @Column(nullable = true, name="output_file_list", length = NAME_LEN)
	private List<String> _outputFileList = Collections.synchronizedList(new ArrayList<String>());

  @ElementCollection
  @LazyCollection (LazyCollectionOption.FALSE)
  @Column(nullable = true, name="output_gtfs_file_list", length = NAME_LEN)
	private List<String> _outputGtfsFileList = Collections.synchronizedList(new ArrayList<String>());

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "_msg", column = @Column(name = "exception_msg", length = EX_MSG_LEN)),
    @AttributeOverride(name = "_rootCause", column = @Column(name = "exception_root_cause", length = EX_ROOT_CAUSE_LEN))})
	private SerializableException _exception = null;

  @Column(nullable = true, name="is_complete", length = NAME_LEN)
	private boolean _isComplete = false;
  @Column(nullable = true, name="bundle_root_dir", length = BUNDLE_ROOT_DIR_LEN)
	private String _bundleRootDirectory;
  @Column(nullable = true, name="bundle_input_dir", length = BUNDLE_INPUT_DIR_LEN)
	private String _bundleInputDirectory;
  @Column(nullable = true, name="bundle_output_dir", length = BUNDLE_OUTPUT_DIR_LEN)
	private String _bundleOutputDirectory;
  @Column(nullable = true, name="bundle_data_dir", length = BUNDLE_DATA_DIR_LEN)
	private String _bundleDataDirectory;
  @Column(nullable = true, name="bundle_tar_filename", length = BUNDLE_TAR_FILENAME_LEN)
	private String _bundleTarFilename;
  @Column(nullable = true, name="remote_input_dir", length = REMOTE_INPUT_DIR_LEN)
	private String _remoteInputDirectory;
  @Column(nullable = true, name="remote_output_dir", length = REMOTE_OUTPUT_DIR_LEN)
	private String _remoteOutputDirectory;
  @Column(nullable = true, name="remote_output_gtfs_dir", length = REMOTE_OUTPUT_GTFS_DIR_LEN)
	private String _remoteOutputGtfsDirectory;	
  @Column(nullable = true, name="version_string", length = VERSION_STRING_LEN)
	private String _versionString;
  @Column(nullable = true, name="tmp_dir", length = TMP_DIR_LEN)
	private String _tmpDirectory;
  @Column(nullable = true, name="bundle_build_name", length = BUNDLE_BUILD_NAME_LEN)
	private String _bundleBuildName;
  @Column(nullable = true, name="bundle_start_date", length = BUNDLE_START_DATE_LEN)
	private String bundleStartDate;
  @Column(nullable = true, name="bundle_end_date", length = BUNDLE_END_DATE_LEN)
	private String bundleEndDate;
  @Column(nullable = true, name="bundle_comment", length = BUNDLE_COMMENT_LEN)
	private String bundleComment;
  @Column(nullable = true, name="bundle_dir_name", length = BUNDLE_DIR_NAME_LEN)
	private String bundleDirectoryName;
  @Column(nullable = true, name="bundle_email_to", length = BUNDLE_EMAIL_TO_LEN)
	private String bundleEmailTo;
  @Column(nullable = true, name="bundle_id", length = BUNDLE_ID_LEN)
	private String bundleId;

  @Column(nullable = true, name="bundle_result_link", length = BUNDLE_RESULTS_LINK_LEN)
	private String bundleResultLink;

	// no arg constructor for serialization
	public BundleBuildResponse() {
	}

	public BundleBuildResponse(String id) {
		this.id = id;
	}

	public String toString() {
		return "BundleBuildResponse{[" + id + "], bundleResultLink=" + bundleResultLink
				+ ", statusList=" + _statusList 
				+ ", complete=" + _isComplete
				+ ", exception=" + _exception + "}"; 
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void addGtfsFile(String file) {
		_gtfsList.add(file);
	}

	public List<String> getGtfsList() {
		return new ArrayList<String>(_gtfsList);
	}

	// for JSON serialization only
	public void setGtfsList(List<String> gtfsList) {
		_gtfsList = gtfsList;
	}

	public void addAuxZipFile(String file) {
		_auxZipList.add(file);
	}

	public List<String> getAuxZipList() {
		return new ArrayList<String>(_auxZipList);
	}

	// for JSON serializaton only
	public void setAuxZipList(List<String> auxZipList) {
		_auxZipList = auxZipList;
	}

	public List<String> getConfigList() {
	  return new ArrayList<String>(_configList);
	}
	
	public void setConfigList(List<String> configList) {
	  _configList = configList;
	}
	
  public void addConfigFile(String file) {
    _configList.add(file);
  }
  
	public void addStatusMessage(String msg) {
	  if (msg.length() > STATUS_LEN) {
	    msg = msg.substring(0, STATUS_LEN - 1);
	  }
		_statusList.add(msg);
	}

	public List<String> getStatusList() {
		return new ArrayList<String>(_statusList);
	}

	// for JSON serialization only
	public void setStatusList(List<String> statusList) {
		_statusList = statusList;
	}

	public void addOutputFile(String name) {
		_outputFileList.add(name);
	}

	public List<String> getOutputFileList() {
		return new ArrayList<String>(_outputFileList);
	}

	public void setOutputFileList(List<String> outputFileList) {
		_outputFileList = outputFileList;
	}
	
  public void addOutputGtfsFile(String gtfsFile) {
    _outputGtfsFileList.add(gtfsFile);
  }

  public List<String> getOutputGtfsFile() {
    return new ArrayList<String>(_outputGtfsFileList);
  }

  public void setOutputGtfsFileList(List<String> outputGtfsFileList) {
    _outputGtfsFileList = outputGtfsFileList;
  }


	/**
	 * This method is additive, it chains exceptions if called multiple times.
	 */
	public void setException(Exception e) {
	  if (e == null) {
	    _exception = null;
	    return;
	  }
	  if (_exception == null) {
	  	String msg = "";
	  	if (e.getMessage() != null) {
	  		msg = e.getMessage().substring(0, Math.min(e.getMessage().length(), BundleBuildResponse.EX_MSG_LEN));
		}
	  _exception = new SerializableException(msg, e);
	  } else {
	    _exception = new SerializableException(e, _exception);
	  }
	}

	public Exception getException() {
	  return _exception;
	}

	public void setComplete(boolean complete) {
		_isComplete = complete;
	}

	public boolean isComplete() {
		return _isComplete;
	}

	public void setBundleOutputDirectory(String bundleDir) {
		_bundleOutputDirectory = bundleDir;
	}

	public String getBundleOutputDirectory() {
		return _bundleOutputDirectory;
	}

	public void setVersionString(String versionString) {
		_versionString = versionString;
	}

	public String getVersionString() {
		return _versionString;
	}

	public String getBundleDataDirectory() {
		return _bundleDataDirectory;
	}
	public void setBundleDataDirectory(String directoryPath) {
		_bundleDataDirectory = directoryPath;
	}

	public String getBundleInputDirectory() {
		return _bundleInputDirectory;
	}
	public void setBundleInputDirectory(String directoryPath) {
		_bundleInputDirectory = directoryPath;

	}

	public String getBundleTarFilename() {
		return _bundleTarFilename;
	}

	public void setBundleTarFilename(String filename) {
		_bundleTarFilename = filename;
	}

	public String getBundleRootDirectory() {
		return _bundleRootDirectory;
	}

	public void setBundleRootDirectory(String directoryPath) {
		_bundleRootDirectory = directoryPath;
	}

	public String getTmpDirectory() {
		return _tmpDirectory;
	}

	public void setTmpDirectory(String tmpDirectory) {
		_tmpDirectory = tmpDirectory;
	}

	public String getRemoteInputDirectory() {
		return _remoteInputDirectory;
	}

	public void setRemoteInputDirectory(String directoryPath) {
		_remoteInputDirectory = directoryPath;
	}

	public String getRemoteOutputDirectory() {
		return _remoteOutputDirectory;
	}

	public void setRemoteOutputDirectory(String directoryPath) {
		_remoteOutputDirectory = directoryPath;
	}

	
	public String getRemoteOutputGtfsDirectory() {
	  return _remoteOutputGtfsDirectory;
	}
	
  public void setRemoteOutputGtfsDirectory(String directoryPath) {
    _remoteOutputGtfsDirectory = directoryPath;
  }

	
	/**
	 * @return the bundleResultLink
	 */
	 public String getBundleResultLink() {
		return bundleResultLink;
	}

	/**
	 * @param bundleResultLink the bundleResultLink to set
	 */
	 public void setBundleResultLink(String bundleResultLink) {
		 this.bundleResultLink = bundleResultLink;
	 }

	 public String getBundleBuildName() {
		 return _bundleBuildName;
	 }

	 public void setBundleBuildName(String bundleName) {
		 this._bundleBuildName = bundleName;
	 }
	 /**
	  * @return the bundleStartDate
	  */
	 public String getBundleStartDate() {
		 return bundleStartDate;
	 }
	 /**
	  * @param bundleStartDate the bundleStartDate to set
	  */
	 public void setBundleStartDate(String bundleStartDate) {
		 this.bundleStartDate = bundleStartDate;
	 }
	 /**
	  * @return the bundleEndDate
	  */
	 public String getBundleEndDate() {
		 return bundleEndDate;
	 }
	 /**
	  * @param bundleEndDate the bundleEndDate to set
	  */
	 public void setBundleEndDate(String bundleEndDate) {
		 this.bundleEndDate = bundleEndDate;
	 }
	 /**
	  * @return the bundleComment
	  */
	 public String getBundleComment() {
		 return bundleComment;
	 }
	 /**
	  * @param bundleComment the bundleComment to set
	  */
	 public void setBundleComment(String bundleComment) {
		 this.bundleComment = bundleComment;
	 }

	 private static class SerializableException extends Exception implements Serializable {
		 private String _msg = "";
		 private String _rootCause = "";

		 public SerializableException() {
		   _msg = "";
		   _rootCause = "";
		 }

		 public SerializableException(String msg, Exception rootCause) {
			 _msg = rootCause.getClass().getName() + ":" + msg;
			 if (msg != null && msg.length() > EX_MSG_LEN) {
			   msg = msg.substring(0, EX_MSG_LEN - 1);
			 }
			 int count = 0;
			 for (StackTraceElement ste:rootCause.getStackTrace()) {
				 _rootCause += ste.toString() + "\n";
				 count++;
				 if (count > 1) break;
			 }
       if (_rootCause.length() > EX_ROOT_CAUSE_LEN) {
         _rootCause = _rootCause.substring(0, EX_ROOT_CAUSE_LEN - 1);
       }
		 }

		 public SerializableException(Exception newException, Exception rootCause) {
			 _msg = newException.getClass().getName() + ":"  + newException.getMessage() 
					 + ";  " + rootCause.getClass().getName() + ":" + rootCause.getMessage();
       if (_msg.length() > EX_MSG_LEN) {
         _msg = _msg.substring(0, EX_MSG_LEN - 1);
       }

			 int count = 0;
			 for (StackTraceElement ste:newException.getStackTrace()) {
				 _rootCause += ste.toString() + "\n";
				 count++;
				 if (count > 1) break;
			 }
			 _rootCause += "  Caused by:\n\n";
			 count++;
			 for (StackTraceElement ste:rootCause.getStackTrace()) {
				 _rootCause += ste.toString() + "\n";
				 if (count > 1) break;
			 }
       if (_rootCause.length() > EX_MSG_LEN) {
         _rootCause = _rootCause.substring(0, EX_MSG_LEN - 1);
       }
		 }

		 public String getMessage() {
			 return toString();
		 }

		 public String toString() {
			 return  _msg + "\n  Caused by:\n\n" + _rootCause; 
		 }
		
	 }

	public String getBundleDirectoryName() {
		return bundleDirectoryName;
	}
	public void setBundleDirectoryName(String bundleDirectoryName) {
		this.bundleDirectoryName = bundleDirectoryName;
	}
	public String getBundleEmailTo() {
		return bundleEmailTo;
	}
	public void setBundleEmailTo(String bundleEmailTo) {
		this.bundleEmailTo = bundleEmailTo;
	}
	public String getBundleId() {
	  return bundleId;
	}
	public void setBundleId(String id) {
	  bundleId = id;
	}
}
