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
package org.onebusaway.admin.service.bundle.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.onebusaway.admin.service.BundleDeployerService;
import org.onebusaway.admin.service.RemoteConnectionService;
import org.onebusaway.admin.service.bundle.api.BundleResource;
import org.onebusaway.util.services.configuration.ConfigurationServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("tdmRemoteBundleDeployerImpl")
public class TdmBundleDeployerServiceImpl implements BundleDeployerService{

  private static final String DEFAULT_TDM_URL = "http://tdm";
  private static Logger _log = LoggerFactory.getLogger(BundleResource.class);
  @Autowired
  private ConfigurationServiceClient _configClient;
  @Autowired
  private RemoteConnectionService _remoteConnectionService;

  private ObjectMapper _dateMapper = new ObjectMapper();
  /*
   * override of default TDM location: for local testing use
   * http://localhost:8080/onebusaway-nyc-tdm-webapp This should be set in
   * context.xml
   */
  private String tdmURL;

  @PostConstruct
  public void setup() {
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    _dateMapper.setDateFormat(df);
  }

  /**
   * list the bundle(s) that are on S3, potentials to be deployed.
   */
  @Override
  public Response listStagedBundles(String environment) {
    try {
      _log.info("list with tdm url=" + getTDMURL());
      String url = getTDMURL() + "/api/bundle/deploy/list/" + environment;
      _log.debug("requesting:" + url);
      String json = _remoteConnectionService.getContent(url);
      _log.debug("response:" + json);
      return Response.ok(json).build();
    } catch (Exception e) {
      _log.error("bundle list failed:", e);
      return Response.serverError().build();
    }
  }

  /**
   * request bundles at s3://obanyc-bundle-data/activebundes/{environment} be deployed
   * on the TDM (and hence the rest of the environment)
   * @param environment string representing environment (dev/staging/prod/qa)
   * @return status object with id for querying status
   */
  @Override
  public Response deploy(String environment) {
    String url = getTDMURL() + "/api/bundle/deploy/from/" + environment;
    _log.debug("requesting:" + url);
    String json = _remoteConnectionService.getContent(url);
    _log.debug("response:" + json);
    return Response.ok(json).build();
  }

  /**
   * query the status of a requested bundle deployment
   * @param id the id of a BundleDeploymentStatus
   * @return a serialized version of the requested BundleDeploymentStatus, null otherwise
   */
  @Override
  public Response deployStatus(String id) {
    try {
      String url = getTDMURL() + "/api/bundle/deploy/status/" + id + "/list";
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
  public Response getBundleList() {
    // not implemented
    _log.error("getBundleList not implemented");
    return Response.serverError().build();
  }

  @Override
  public Response getBundleFile(String bundleId, String relativeFilename) {
    // not implemented
    _log.error("getStagedFile not implemented");
    return Response.serverError().build();
  }

  @Override
  public Response getLatestBundle() {
    // not implemented
    _log.error("getLatestBundle not implemented");
    return Response.serverError().build();
  }
  
  @Override
  public String getLatestBundleId() {
    _log.error("getLatestBundleId not implemented");
    return null;
  }

}
