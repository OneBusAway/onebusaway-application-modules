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
package org.onebusaway.webapp.actions.admin.vehiclepredictions;

import java.io.InputStream;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.onebusaway.webapp.actions.OneBusAwayNYCAdminActionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Namespace(value="/admin/vehiclepredictions")
@Result(name = "success",
        type = "stream",
        params = {
            "contentType", "${type}",
            "inputName", "stream",
            "bufferSize", "1024"
        })
public class LinkProxyAction extends OneBusAwayNYCAdminActionSupport {
  
  private Logger _log = LoggerFactory.getLogger(LinkProxyAction.class);
  
  @Autowired
  private ConfigurationService _configurationService;

  private String type = "application/xml";
  private InputStream stream;

  public String execute() {
    CloseableHttpClient httpclient = HttpClients.createDefault();
    try {
      HttpHost target = new HttpHost(getLinkServiceHost(), getLinkServicePort(), getLinkServiceProtocol());
      HttpGet request = new HttpGet(getLinkServicePath());
      CloseableHttpResponse response = httpclient.execute(target, request);
      stream = response.getEntity().getContent();
      return SUCCESS;
    } catch (Exception e) {
      _log.error("proxy error:", e);
    }
    return ERROR;
  }

  private String getLinkServiceProtocol() {
    return _configurationService.getConfigurationValueAsString("admin.link.service.protocol", "http");
  }

  
  private String getLinkServiceHost() {
    return _configurationService.getConfigurationValueAsString("admin.link.service.host", "localhost");
  }
  
  private int getLinkServicePort() {
    return _configurationService.getConfigurationValueAsInteger("admin.link.service.port", 9764);
  }
  
  private String getLinkServicePath() {
    return _configurationService.getConfigurationValueAsString("admin.link.service.path", "/services/tss_lab/GetOnScheduleTrains?TimeInterval=5");
  }

  public String getType() {
    return this.type;
  }
  
  public InputStream getStream() {
    return this.stream;
  }
  
    
}
