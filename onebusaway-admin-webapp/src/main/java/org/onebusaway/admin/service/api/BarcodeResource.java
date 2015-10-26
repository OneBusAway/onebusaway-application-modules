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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/barcode/")
@Component
/**
 * Proxy barcode request through to TDM to hide QR service from users. (TDM may not
 * be addressable/accessible). 
 *
 */
public class BarcodeResource {

  private static Logger _log = LoggerFactory.getLogger(BarcodeResource.class);
  
  private static final String DEFAULT_TDM_URL = "http://tdm";

  
  /*
   * override of default TDM location:  for local testing use 
   * http://localhost:8080/onebusaway-nyc-tdm-webapp
   * This should be set in context.xml
   */
  private String tdmURL;

  @Path("/getByStopId/{stopId}")
  @GET
  @Produces("image/jpeg")
  public Response proxy(@PathParam("stopId") String stopId, 
      @DefaultValue("99") @QueryParam("img-dimension") String imgDimension) {
    // proxy request here to TDM
    String uri = getTDMURL() + "/api/barcode/getByStopId/" + stopId;
    uri = uri + "?" + "img-dimension=" + imgDimension;

    Response response = Response.ok(proxyRequest(uri)).build();
    return response;
  }

  protected InputStream proxyRequest(String uri) {
    HttpURLConnection connection = null;
    InputStream is = null;
    try {
      connection = (HttpURLConnection) new URL(uri).openConnection();
      connection.setRequestMethod("GET");
      connection.setDoOutput(true);
      connection.setReadTimeout(10000);
      is = connection.getInputStream();
    } catch (Exception any) {
      _log.error("proxyRequest failed:", any);
    }
    return is;
  }

    private String getTDMURL() {
    if (tdmURL != null && tdmURL.length() > 0) {
      return tdmURL;
    }
    return DEFAULT_TDM_URL;
  }

}
