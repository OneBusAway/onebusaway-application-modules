/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.presentation.impl.resources;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.presentation.services.resources.Resource;
import org.onebusaway.presentation.services.resources.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;

@Results(value = {
    @Result(type = "stream"),
    @Result(type = "httpheader", name = "NotFound", params = {"status", "404"})})
public class ResourceAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  private ResourceService _resourceService;

  @Autowired
  public void setResourceService(ResourceService resourceService) {
    _resourceService = resourceService;
  }

  protected String _id;

  private Resource _resource;

  public void setId(String id) {
    _id = id;
  }

  public String getId() {
    return _id;
  }

  public InputStream getInputStream() throws IOException {
    if (_resource == null)
      return null;

    URL localUrl = _resource.getLocalUrl();

    return localUrl.openStream();
  }

  public String getContentType() {
    if (_resource == null)
      return null;

    URL localUrl = _resource.getLocalUrl();

    String path = localUrl.getPath();
    if (path.endsWith(".png"))
      return "image/png";
    else if (path.endsWith(".css"))
      return "text/css";
    else if (path.endsWith(".js"))
      return "text/javascript";
    else if (path.endsWith(".mp3"))
      return "audio/mpeg";
    return null;
  }

  public long getContentLength() {
    if (_resource == null)
      return -1;
    return _resource.getContentLength();
  }
  
  public String getContentDisposition() {
    return "";
  }

  public Date getLastModified() {

    ensureResource();
    
    if (_resource == null)
      return null;

    return new Date(_resource.getLastModifiedTime());
  }

  @Override
  public String execute() throws Exception {

    ensureResource();

    if (_resource == null)
      return "NotFound";

    return SUCCESS;
  }

  protected void ensureResource() {
    if (_resource == null) {
      if( _id == null) {
        HttpServletRequest request = ServletActionContext.getRequest();
        _id = request.getParameter("id");
      }
      _resource = _resourceService.getLocalResourceForExternalId(_id, getLocale());
    }
  }
}
