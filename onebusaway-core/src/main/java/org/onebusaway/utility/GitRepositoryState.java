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

package org.onebusaway.utility;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitRepositoryState {

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
	private MavenVersion parsedVersion;

	public GitRepositoryState() {
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
		this.setParsedVersion(new MavenVersion(version));
	}

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

	public String getCommitId() {
		return commitId;
	}

	public void setCommitId(String commitId) {
		this.commitId = commitId;
	}

	public String getCommitIdAbbrev() {
		return commitIdAbbrev;
	}

	public void setCommitIdAbbrev(String commitIdAbbrev) {
		this.commitIdAbbrev = commitIdAbbrev;
	}

	public String getBuildUserName() {
		return buildUserName;
	}

	public void setBuildUserName(String buildUserName) {
		this.buildUserName = buildUserName;
	}

	public String getBuildUserEmail() {
		return buildUserEmail;
	}

	public void setBuildUserEmail(String buildUserEmail) {
		this.buildUserEmail = buildUserEmail;
	}

	public String getBuildTime() {
		return buildTime;
	}

	public void setBuildTime(String buildTime) {
		this.buildTime = buildTime;
	}

	public String getCommitUserName() {
		return commitUserName;
	}

	public void setCommitUserName(String commitUserName) {
		this.commitUserName = commitUserName;
	}

	public String getCommitUserEmail() {
		return commitUserEmail;
	}

	public void setCommitUserEmail(String commitUserEmail) {
		this.commitUserEmail = commitUserEmail;
	}

	public String getCommitMessageFull() {
		return commitMessageFull;
	}

	public void setCommitMessageFull(String commitMessageFull) {
		this.commitMessageFull = commitMessageFull;
	}

	public String getCommitMessageShort() {
		return commitMessageShort;
	}

	public void setCommitMessageShort(String commitMessageShort) {
		this.commitMessageShort = commitMessageShort;
	}

	public String getCommitTime() {
		return commitTime;
	}

	public void setCommitTime(String commitTime) {
		this.commitTime = commitTime;
	}

	public MavenVersion getParsedVersion() {
		return parsedVersion;
	}

	public void setParsedVersion(MavenVersion parsedVersion) {
		this.parsedVersion = parsedVersion;
	}

	public static class MavenVersion {
		private String major;
		private String minor;
		private String incremental;
		private String qualifier;

		public String getMajor() {
			return major;
		}

		public String getMinor() {
			return minor;
		}

		public String getIncremental() {
			return incremental;
		}

		public String getQualifier() {
			return qualifier;
		}

		public MavenVersion(String version) {
			// TODO: Would be cleaner with named capturing groups but they
			// aren't supported until Java 7
			final Pattern VERSION_PATTERN = Pattern
					.compile("([0-9]+)\\.([0-9]+)\\.([0-9]+)(?:-(.*))?");

			Matcher m = VERSION_PATTERN.matcher(version);

			if (m.matches()) {
				this.major = m.group(1);
				this.minor = m.group(2);
				this.incremental = m.group(3);
				this.qualifier = m.group(4);
			} else {
				this.major = "";
				this.minor = "";
				this.incremental = "";
				this.qualifier = "";
			}
		}
	}
}