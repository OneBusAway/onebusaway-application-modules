/**
 * Copyright (C) 2014 Lenny Caraballo <lcaraballo@camsys.com>
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

package org.onebusaway.webapp.actions;

import java.io.IOException;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.onebusaway.utility.GitRepositoryHelper;
import org.onebusaway.utility.GitRepositoryState;
import com.opensymphony.xwork2.ActionSupport;

/**
 * Action for release (status)  page
 * 
 */

@ParentPackage("json-default")
@Result(type="json", params={"root", "repositoryState"})
public class ReleaseAction extends ActionSupport {

  private static final long serialVersionUID = 1L;
  private GitRepositoryState gitRepositoryState;

/*  public GitRepositoryState getGitRepositoryState() {
	return gitRepositoryState;
  }*/
  
  public GitRepositoryState getRepositoryState(){
	  return gitRepositoryState; 
  }

  public String execute() throws IOException {
	  if (gitRepositoryState == null)
			gitRepositoryState = new GitRepositoryHelper().getGitRepositoryState();
      
	  return SUCCESS;
  }
}