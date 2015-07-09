/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.util.git;

import java.util.Properties;

public class GitRepositoryState {

    String branch;                  // =${git.branch}
    public String getBranch() { return branch; }
    String describe;                // =${git.commit.id.describe}
    public String getDescribe() { return describe; }
    String commitId;                // =${git.commit.id}
    public String getCommitId() { return commitId; }
    String commitIdAbbrev;          // =${git.commit.id.abbrev}
    public String getCommigIdAbbrev() { return commitIdAbbrev; }
    String buildUserName;           // =${git.build.user.name}
    public String getBuildUserName() { return buildUserName; }
    String buildUserEmail;          // =${git.build.user.email}
    public String getBuildUserEmail() { return buildUserEmail; }
    String buildTime;               // =${git.build.time}
    public String getBuildTime() { return buildTime; }
    String commitUserName;          // =${git.commit.user.name}
    public String getCommitUserName() { return commitUserName; }
    String commitUserEmail;         // =${git.commit.user.email}
    public String getCommitUserEmail() { return commitUserEmail; }
    String commitMessageFull;       // =${git.commit.message.full}
    public String getCommitMessageFull() { return commitMessageFull; }
    String commitMessageShort;      // =${git.commit.message.short}
    public String getCommitMessageShort() { return commitMessageShort; }
    String commitTime;          
    public String getCommitTime() { return commitTime; }


    public GitRepositoryState(Properties properties)
    {
	this.branch = properties.get("git.branch").toString();
	this.describe = properties.get("git.commit.id.describe").toString();
	this.commitId = properties.get("git.commit.id").toString();
	this.buildUserName = properties.get("git.build.user.name").toString();
	this.buildUserEmail = properties.get("git.build.user.email").toString();
	this.buildTime = properties.get("git.build.time").toString();
	this.commitUserName = properties.get("git.commit.user.name").toString();
	this.commitUserEmail = properties.get("git.commit.user.email").toString();
	this.commitMessageShort = properties.get("git.commit.message.short").toString();
	this.commitMessageFull = properties.get("git.commit.message.full").toString();
	this.commitTime = properties.get("git.commit.time").toString();
    }
    
    public String getDetails() {
    	return "{ " 
    			+ "branch: " + branch + ", "
    			+ "describe: " + describe + ", "
    			+ "commitId: " + commitId + ", "
    			+ "buildUserName: " + buildUserName + ", "
    			+ "buildUserEmail: " + buildUserEmail + ", "
    			+ "buildTime: " + buildTime + ", "
    			+ "commitUserName: " + commitUserName + ", "
    			+ "commitUserEmail: " + commitUserEmail + ", "
    			+ "commitMessageShort: " + commitMessageShort + ", "
    			+ "commitMessageFull: " + commitMessageFull + ","
    			+ "commitTime: " + commitTime
    			+ " }";

    }
}