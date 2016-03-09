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
package org.onebusaway.admin.service.api;

import java.util.UUID;

import org.onebusaway.admin.service.RemoteConnectionService;
import org.onebusaway.admin.service.bundle.api.AuthenticatedResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("/config")
@Component
public class ConfigResource extends AuthenticatedResource implements ServletContextAware {

  private static final String DEFAULT_TDM_URL = "http://tdm";
  private static Logger _log = LoggerFactory.getLogger(ConfigResource.class);
  
  @Autowired
  private RemoteConnectionService _remoteConnectionService;
  private String tdmURL;

  @Path("/generate-api-key")
  @GET
  /**
   * generates an api key
   */
  public Response generateApiKey() {
	  return Response.ok(UUID.randomUUID().toString()).build();
  }
  
  @Path("/deploy/list/depot/{environment}")
  @GET
  /**
   * list the bundle(s) that are on S3, potentials to be deployed.
   */
  public Response listDepots(@PathParam("environment") String environment) {
    try {
    String url = getTDMURL() + "/api/tdm/config/deploy/list/depot/" + environment;
    _log.debug("requesting:" + url);
    String json = _remoteConnectionService.getContent(url);
    _log.debug("response:" + json);
    return Response.ok(json).build();
    } catch (Exception e) {
      return Response.serverError().build();
    }
  }  

  @Path("/deploy/list/dsc/{environment}")
  @GET
  /**
   * list the bundle(s) that are on S3, potentials to be deployed.
   */
  public Response listDscs(@PathParam("environment") String environment) {
    try {
    String url = getTDMURL() + "/api/tdm/config/deploy/list/dsc/" + environment;
    _log.debug("requesting:" + url);
    String json = _remoteConnectionService.getContent(url);
    _log.debug("response:" + json);
    return Response.ok(json).build();
    } catch (Exception e) {
      return Response.serverError().build();
    }
  }  

  @Path("/deploy/from/{environment}")
  @GET
  /**
   * deploy files staged on S3 to local directories.
   */
  public Response deploy(@PathParam("environment") String environment) {
    if (!isAuthorized()) {
      return Response.noContent().build();
    }

    try {
    String url = getTDMURL() + "/api/tdm/config/deploy/from/" + environment;
    _log.debug("requesting:" + url);
    String json = _remoteConnectionService.getContent(url);
    _log.debug("response:" + json);
    return Response.ok(json).build();
    } catch (Exception e) {
      return Response.serverError().build();
    }
  }  

  @Path("/deploy/status/{id}/list")
  @GET
  /**
   * get the status of an in-progress deployment
   */
  public Response status(@PathParam("id") String id) {
    try {
    String url = getTDMURL() + "/api/tdm/config/deploy/status/" + id + "/list";
    _log.debug("requesting:" + url);
    String json = _remoteConnectionService.getContent(url);
    _log.debug("response:" + json);
    return Response.ok(json).build();
    } catch (Exception e) {
      return Response.serverError().build();
    }
  }  

  private String getTDMURL() {
    if (tdmURL != null && tdmURL.length() > 0) {
      return tdmURL;
    }
    return DEFAULT_TDM_URL;
  }

  @Override
  public void setServletContext(ServletContext context) {
      if (context != null) {
        String url = context.getInitParameter("tdm.host");
        if (url != null && url.length() > 0) {
          tdmURL = url;
          _log.error("tdmURL="+ tdmURL);
        }
      }
  }

}
