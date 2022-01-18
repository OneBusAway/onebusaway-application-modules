/**
 * Copyright (C) 2011 Metropolitan Transportation Authority
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
package org.onebusaway.enterprise.webapp.actions.api;


import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.onebusaway.util.git.GitRepositoryHelper;
import org.onebusaway.util.git.GitRepositoryState;
import org.onebusaway.enterprise.webapp.actions.OneBusAwayEnterpriseActionSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Action for release (status)  page
 * 
 */
public class ReleaseAction extends OneBusAwayEnterpriseActionSupport {

  private static Logger _log = LoggerFactory.getLogger(ReleaseAction.class);
  private static final long serialVersionUID = 1L;
  private GitRepositoryState gitRepositoryState;
  private ObjectMapper _mapper = new ObjectMapper();    

  public GitRepositoryState getGitRepositoryState() {
	if (gitRepositoryState == null)
		gitRepositoryState = new GitRepositoryHelper().getGitRepositoryState();
	return gitRepositoryState;
  }

  public String getCommitId() {
      if (getGitRepositoryState() != null) {
	  return getGitRepositoryState().getCommitId();
      }
      return "unknown";
  }

  public String getDetails() throws IOException {
      if (getGitRepositoryState() != null) {
	  return _mapper.writeValueAsString(getGitRepositoryState());
      }
      return "unknown";
  }
}