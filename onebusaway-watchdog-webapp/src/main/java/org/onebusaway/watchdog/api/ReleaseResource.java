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
package org.onebusaway.watchdog.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.map.ObjectMapper;
import org.onebusaway.utility.GitRepositoryHelper;
import org.onebusaway.utility.GitRepositoryState;
import org.springframework.stereotype.Component;

import com.sun.jersey.api.spring.Autowire;

/**
 * Webservice to show git status.
 * 
 */
@Component
@Autowire
@Path("/release")
public class ReleaseResource {

  private ObjectMapper _mapper = new ObjectMapper();
  private GitRepositoryState gitState = null;

  @GET
  @Produces("application/json")
  public Response getDetails() throws Exception {
    if (gitState == null) {
      gitState = new GitRepositoryHelper().getGitRepositoryState();
    }
    return Response.ok(_mapper.writeValueAsString(gitState)).build();
  }

}
