/**
 * Copyright (C) 2015 Cambridge Systematics
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
package org.onebusaway.nextbus.actions.api;

import org.apache.struts2.rest.DefaultHttpHeaders;
import org.apache.struts2.rest.HttpHeaders;
import org.onebusaway.util.git.GitRepositoryHelper;
import org.onebusaway.util.git.GitRepositoryState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensymphony.xwork2.ModelDriven;

/**
 * Action for release (status) page
 * 
 */
public class ReleaseAction implements ModelDriven<GitRepositoryState> {

  private static final long serialVersionUID = 1L;
  private GitRepositoryState gitRepositoryState;

  public GitRepositoryState getGitRepositoryState() {
    if (gitRepositoryState == null)
      gitRepositoryState = new GitRepositoryHelper().getGitRepositoryState();
    return gitRepositoryState;
  }
  
  public HttpHeaders index() {
    return new DefaultHttpHeaders("success");
  }
  
  @Override
  public GitRepositoryState getModel() {
    return getGitRepositoryState();
  }
}