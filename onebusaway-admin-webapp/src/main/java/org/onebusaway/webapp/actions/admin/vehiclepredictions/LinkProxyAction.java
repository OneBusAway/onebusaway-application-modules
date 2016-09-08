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
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.onebusaway.util.DisableSSLLibrary;
import org.onebusaway.util.services.configuration.ConfigurationService;
import org.onebusaway.webapp.actions.OneBusAwayNYCAdminActionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.util.UrlUtils;

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
 
    String path = UrlUtils.buildFullRequestUrl(getLinkServiceProtocol(), getLinkServiceHost(), getLinkServicePort(), getLinkServicePath(), null);
    try {
      URL url = new URL(path);
      URLConnection conn = url.openConnection();
      if (getLinkServiceProtocol().equals("https") && getDisableSSL() 
          && conn instanceof HttpsURLConnection) {
        DisableSSLLibrary.disableSSL((HttpsURLConnection) conn);
      }
      stream = conn.getInputStream();
      _log.info("connect to ({})", path);
    
      return SUCCESS;
    } catch (Exception e) {
      _log.error("proxy error to (" 
          + getLinkServiceProtocol() + "://" +
          getLinkServiceHost() + ":" +
          getLinkServicePath() 
          + "):"
          , e);
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
  
  private boolean getDisableSSL() {
    return Boolean.parseBoolean(_configurationService.getConfigurationValueAsString("admin.link.disableSSL", "false"));
  }
  
  public String getType() {
    return this.type;
  }
  
  public InputStream getStream() {
    return this.stream;
  }
  
    
}
