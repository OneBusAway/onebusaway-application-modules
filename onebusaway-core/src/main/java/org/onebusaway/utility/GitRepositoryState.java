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

import java.io.Serializable;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitRepositoryState implements Serializable {

  private static final long serialVersionUID = 593030390723190029L;

  private String branch; // =${git.branch}
  private String buildTime; // =${git.build.time}
  private String buildUserEmail; // =${git.build.user.email}
  private String buildUserName; // =${git.build.user.name}
  private String commitId; // =${git.commit.id}
  private String commitIdAbbrev; // =${git.commit.id.abbrev}
  private String commitMessageFull; // =${git.commit.message.full}
  private String commitMessageShort; // =${git.commit.message.short}
  private String commitTime; // =${git.commit.time}
  private String commitUserName; // =${git.commit.user.name}
  private String commitUserEmail; // =${git.commit.user.email}
  private String describe; // =${git.commit.id.describe}
  private String shortDescribe; // =${git.commit.id.describe-short}
  private String tags; // =${git.tags} // comma separated tag names
  private String version;

  MavenVersion parsedVersion;

  public GitRepositoryState() {
  }

  public GitRepositoryState(Properties properties) {
    this.branch = properties.get("git.branch").toString();
    this.buildTime = properties.get("git.build.time").toString();
    this.buildUserEmail = properties.get("git.build.user.email").toString();
    this.buildUserName = properties.get("git.build.user.name").toString();
    this.commitId = properties.get("git.commit.id").toString();
    this.commitIdAbbrev = properties.get("git.commit.id.abbrev").toString();
    this.commitMessageFull = properties.get("git.commit.message.full").toString();
    this.commitMessageShort = properties.get("git.commit.message.short").toString();
    this.commitTime = properties.get("git.commit.time").toString();
    this.commitUserName = properties.get("git.commit.user.name").toString();
    this.commitUserEmail = properties.get("git.commit.user.email").toString();
    this.describe = properties.get("git.commit.id.describe").toString();
    setVersion(properties.get("version").toString());
  }

  public String getDetails() {
    return "{ " + "branch: " + branch + ", " + "describe: " + describe + ", "
        + "commitId: " + commitId + ", " + "buildUserName: " + buildUserName
        + ", " + "buildUserEmail: " + buildUserEmail + ", " + "buildTime: "
        + buildTime + ", " + "commitUserName: " + commitUserName + ", "
        + "commitUserEmail: " + commitUserEmail + ", " + "commitMessageShort: "
        + commitMessageShort + ", " + "commitMessageFull: " + commitMessageFull
        + "," + "commitTime: " + commitTime + " }";

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
}