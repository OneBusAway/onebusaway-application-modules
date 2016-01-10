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
package org.onebusaway.admin.service.bundle.api;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.onebusaway.admin.service.BundleArchiverService;
import org.onebusaway.admin.service.BundleDeployerService;
import org.onebusaway.admin.service.BundleStagerService;
import org.onebusaway.admin.service.RemoteConnectionService;
import org.onebusaway.util.services.configuration.ConfigurationServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

@Path("/bundle")
@Component
public class BundleResource extends AuthenticatedResource implements ServletContextAware{
  
  private static Logger _log = LoggerFactory.getLogger(BundleResource.class);
  @Autowired
  private ConfigurationServiceClient _configClient;
  @Autowired
  RemoteConnectionService _remoteConnectionService;
  @Autowired
  private BundleArchiverService _localBundleArchiver;
  @Autowired
  private BundleStagerService _localBundleStager;
  @Autowired
  @Qualifier("localBundleDeployerImpl")
  private BundleDeployerService _localBundleDeployer;
  @Autowired
  @Qualifier("tdmRemoteBundleDeployerImpl")
  private BundleDeployerService _tdmBundleDeployer;
  
  private String tdmURL;

  private Boolean isTdm = null;
  
  
  @Path("/stagerequest/{environment}/{bundleDir}/{bundleName}")
  @GET
  /**
   * request just-built bundle is staged for deployment
   * @return status object with id for querying status
   */
  public Response stage(@PathParam("environment")
    String environment, @PathParam("bundleDir")
    String bundleDir, @PathParam("bundleName")
    String bundleName) {
      // TODO this should follow the deployer pattern with an async response
      // object
      if (!isAuthorized()) {
    	return Response.noContent().build();
	  }
	  
	  return _localBundleStager.stage(environment, bundleDir, bundleName);
    
    /*String json = "{ERROR}";
      try {
        _bundleStager.stage(environment, bundleDir, bundleName);
        _bundleStager.notifyOTP(bundleName);
        json = "{SUCCESS}";
      } catch (Exception any) {
        _log.error("stage failed:", any);
      }
      return Response.ok(json).build();*/
  }
  
  @Path("/stage/status/{id}/list")
  @GET
  public Response stageStatus(@PathParam("id")
  String id) {  
    return _localBundleStager.stageStatus(id);
  }
  
  @Path("/staged/list")
  @GET
  public Response getStagedBundleList() {
    return _localBundleStager.getBundleList();
  }
  
  @Path("/archive/list-datasets")
  @GET
  public Response getArchiveBundleList() {
    return _localBundleArchiver.getArchiveBundleList();
  }

  @Path("/archive/list-by-name/{dataset}/{name}")
  @GET
  public Response getArchiveBundleByName(@PathParam("dataset")
  String dataset, @PathParam("name")
  String name) {
    return _localBundleArchiver.getArchiveBundleByName(dataset, name);
  }

  @Path("/archive/list-by-id/{id}")
  @GET
  public Response getArchiveBundleById(@PathParam("id")
  String id) {
    return _localBundleArchiver.getArchiveBundleById(id);
  }

  @Path("/archive/get-by-name/{dataset}/{name}/{file:.+}")
  @GET
  public Response getFileByName(@PathParam("dataset")
  String dataset, @PathParam("name")
  String name, @PathParam("file")
  String file) {
    return _localBundleArchiver.getFileByName(dataset, name, file);
  }
  
  @Path("/archive/get-by-id/{id}/{file:.+}")
  @GET
  public Response getFileById(@PathParam("id")
  String id, @PathParam("file")
  String file) {
    return _localBundleArchiver.getFileById(id, file);
  }

  @Path("/staged/{bundleId}/file/{bundleFileFilename: [a-zA-Z0-9_./]+}/get")
  @GET
  public Response getStagedFile(@PathParam("bundleId") String bundleId,
      @PathParam("bundleFileFilename") String relativeFilename) {
    return _localBundleStager.getBundleFile(bundleId, relativeFilename);
  }
  
  @Path("/deploy/list/{environment}")
  @GET
  public Response listStagedBundles(@PathParam("environment")
  String environment) {
    if (isTdm()) {
      return _tdmBundleDeployer.listStagedBundles(environment);
    }
    return _localBundleDeployer.listStagedBundles(environment);
  }
  
  @Path("/latest")
  @GET
  public Response getLatestBundle() {
    if (isTdm()) {
      return _tdmBundleDeployer.getLatestBundle();
    }
    return _localBundleDeployer.getLatestBundle();
  }

  @Path("/latest/final")
  @GET
  public Response getLatestBundleFinal() {
    String latestBundleId;
    if (isTdm()) {
      latestBundleId = _tdmBundleDeployer.getLatestBundleId();
    } else {
      latestBundleId = _localBundleDeployer.getLatestBundleId();
    }
    if (latestBundleId == null) {
      _log.error("no latest bundle found");
      return Response.serverError().build();
    }
    
    return _localBundleArchiver.getArchiveBundleById(latestBundleId, "/final/");
  }

  
  @Path("/deploy/from/{environment}")
  @GET
  public Response deploy(@PathParam("environment")
  String environment) {
	 if (!isAuthorized()) {
		 return Response.noContent().build();
	 }
	  
    if(isTdm()){
      return _tdmBundleDeployer.deploy(environment);
    }
    return _localBundleDeployer.deploy(environment);
  }
  
  @Path("/deploy/status/{id}/list")
  @GET
  public Response deployStatus(@PathParam("id")
  String id) {
    if (isTdm()) {
      return _tdmBundleDeployer.deployStatus(id);
    }
    return _localBundleDeployer.deployStatus(id);
  }
  

  @Path("/list")
  @GET
  public Response getBundleList() {
    if (isTdm()) {
      return _tdmBundleDeployer.getBundleList();
    }
    return _localBundleDeployer.getBundleList();
  }
  
  
  @Path("/deploy/{bundleId}/file/{bundleFileFilename: [a-zA-Z0-9_./]+}/get")
  @GET
  public Response getBundleFile(@PathParam("bundleId") String bundleId,
      @PathParam("bundleFileFilename") String relativeFilename) {
    
    if (isTdm()) {
      return _tdmBundleDeployer.getBundleFile(bundleId, relativeFilename);
    }
    return _localBundleDeployer.getBundleFile(bundleId, relativeFilename);
  }
  
  
  private boolean isTdm() {
    if (isTdm != null)
      return isTdm;
    try {
      String useTdm = _configClient.getItem("admin", "useTdm");
      isTdm = "true".equalsIgnoreCase(useTdm);
    } catch (Exception e) {
      _log.error("isTdm caugh e:", e);
    }
    return Boolean.TRUE.equals(isTdm);
  }

  @Override
  public void setServletContext(ServletContext context) {
    if (context != null) {
      String url = context.getInitParameter("tdm.host");
      if (url != null && url.length() > 0) {
        tdmURL = url;
        _log.debug("tdmURL=" + tdmURL);
      }
    }
  }
}
