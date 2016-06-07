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

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.ArrayList;
import java.util.List;

public class BundleBuildRequest {
	private String _id;
	private String _bundleDirectory;
	private String _bundleName;
	private String _tmpDirectory;
	private String _emailAddress;
	private String _bundleStartDate;
	private String _bundleEndDate;
	private String _bundleComment;
	private boolean _archiveFlag;
	private boolean _consolidateFlag;
	private boolean _predateFlag;
	private String _sessionId;
	
	public String getBundleDirectory() {
		return _bundleDirectory;
	}

	public void setBundleDirectory(String bundleDirectory) {
		_bundleDirectory = bundleDirectory;
	}

	public String getTmpDirectory() {
		return _tmpDirectory;
	}

	public void setTmpDirectory(String tmpDirectory) {
		_tmpDirectory = tmpDirectory;
	}

	// TODO this should come from config service
	public List<String> getNotInServiceDSCList() {
		ArrayList<String> dscs = new ArrayList<String>();
		dscs.add("10");
		dscs.add("11");
		dscs.add("12");
		dscs.add("13");
		dscs.add("22");
		dscs.add("6");
		return dscs;
	}

	public String getBundleName() {
		return _bundleName;
	}

	public void setBundleName(String bundleName) {
		_bundleName = bundleName;
	}

	public LocalDate getBundleStartDate() {
		DateTimeFormatter dtf = ISODateTimeFormat.date();
		return (_bundleStartDate==null?null: new LocalDate(dtf.parseLocalDate(_bundleStartDate)));
	}

	public String getBundleStartDateString() {
		return _bundleStartDate;
	}

	public void setBundleStartDate(String bundleStartDate) {
		_bundleStartDate = bundleStartDate;
	}

	public LocalDate getBundleEndDate() {
		DateTimeFormatter dtf = ISODateTimeFormat.date();
		return (_bundleEndDate==null?null: new LocalDate(dtf.parseLocalDate(_bundleEndDate)));
	}

	public void setBundleEndDate(String bundleEndDate) {
		_bundleEndDate = bundleEndDate;
	}

	public String getBundleEndDateString() {
		return _bundleEndDate;
	}

	public String getBundleComment() {
		return _bundleComment;
	}

	public void setBundleComment(String bundleComment) {
		_bundleComment = bundleComment;
	}
	
	public String getEmailAddress() {
		return _emailAddress;
	}

	public void setEmailAddress(String emailTo) {
		_emailAddress = emailTo;
	}

	public String getId() {
		return _id;
	}

	public void setId(String id) {
		_id = id;
	}

	public boolean getArchiveFlag() {
	  return _archiveFlag;
	}
	
  public void setArchiveFlag(boolean archive) {
    _archiveFlag = archive;
  }
  
  public boolean getConsolidateFlag() {
    return _consolidateFlag;
  }
  
  public void setConsolidateFlag(boolean consolidate) {
    _consolidateFlag = consolidate;
  }

  public boolean getPredate() {
    return _predateFlag;
  }
  public void setPredate(boolean predate) {
    _predateFlag = predate;
  }
  
  public String getSessionId() {
	  return _sessionId;
  }
  
  public void setSessionId(String sessionId) {
	  _sessionId = sessionId;
  }

}
