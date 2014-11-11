/**
 * Copyright (C) 2014 Kurt Raschke <kurt@kurtraschke.com>
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

package org.onebusaway.api.model;

import java.io.Serializable;

public class GitRepositoryStateV2Bean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String version;
	private String branch;
	private String describe;
	private String commitId;
	private String commitIdAbbrev;
	private String buildUserName;
	private String buildUserEmail;
	private String buildTime;
	private String commitUserName;
	private String commitUserEmail;
	private String commitMessageFull;
	private String commitMessageShort;
	private String commitTime;
	private String major;
	private String minor;
	private String incremental;
	private String qualifier;

	public GitRepositoryStateV2Bean() {

	}

	public String getBranch() {
		return branch;
	}

	public String getBuildTime() {
		return buildTime;
	}

	public String getBuildUserEmail() {
		return buildUserEmail;
	}

	public String getBuildUserName() {
		return buildUserName;
	}

	public String getCommitId() {
		return commitId;
	}

	public String getCommitIdAbbrev() {
		return commitIdAbbrev;
	}

	public String getCommitMessageFull() {
		return commitMessageFull;
	}

	public String getCommitMessageShort() {
		return commitMessageShort;
	}

	public String getCommitTime() {
		return commitTime;
	}

	public String getCommitUserEmail() {
		return commitUserEmail;
	}

	public String getCommitUserName() {
		return commitUserName;
	}

	public String getDescribe() {
		return describe;
	}

	public String getIncremental() {
		return incremental;
	}

	public String getMajor() {
		return major;
	}

	public String getMinor() {
		return minor;
	}

	public String getQualifier() {
		return qualifier;
	}

	public String getVersion() {
		return version;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	public void setBuildTime(String buildTime) {
		this.buildTime = buildTime;
	}

	public void setBuildUserEmail(String buildUserEmail) {
		this.buildUserEmail = buildUserEmail;
	}

	public void setBuildUserName(String buildUserName) {
		this.buildUserName = buildUserName;
	}

	public void setCommitId(String commitId) {
		this.commitId = commitId;
	}

	public void setCommitIdAbbrev(String commitIdAbbrev) {
		this.commitIdAbbrev = commitIdAbbrev;
	}

	public void setCommitMessageFull(String commitMessageFull) {
		this.commitMessageFull = commitMessageFull;
	}

	public void setCommitMessageShort(String commitMessageShort) {
		this.commitMessageShort = commitMessageShort;
	}

	public void setCommitTime(String commitTime) {
		this.commitTime = commitTime;
	}

	public void setCommitUserEmail(String commitUserEmail) {
		this.commitUserEmail = commitUserEmail;
	}

	public void setCommitUserName(String commitUserName) {
		this.commitUserName = commitUserName;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

	public void setIncremental(String incremental) {
		this.incremental = incremental;
	}

	public void setMajor(String major) {
		this.major = major;
	}

	public void setMinor(String minor) {
		this.minor = minor;
	}

	public void setQualifier(String qualifier) {
		this.qualifier = qualifier;
	}

	public void setVersion(String version) {
		this.version = version;
	}

}