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

import java.io.InputStream;
import java.net.URL;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;

import com.opensymphony.xwork2.ActionSupport;

@Results(value = {@Result(type = "stream", params = {
    "contentType", "contentType"})})
public class ResourcesAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  private ClientBundleFactory _factory;

  public void setClientBundleFactory(ClientBundleFactory factory) {
    _factory = factory;
  }

  private String _id;

  private String _contentType;

  private InputStream _inputStream;

  public void setId(String id) {
    _id = id;
  }

  public String getId() {
    return _id;
  }

  public InputStream getInputStream() {
    return _inputStream;
  }

  public String getContentType() {
    return _contentType;
  }

  @Override
  public String execute() throws Exception {
    
    System.out.println(_id);
    
    LocalResource resource = _factory.getResourceForExternalUrl(_id);
    if (resource != null) {
      URL localUrl = resource.getLocalUrl();
      String path = localUrl.getPath();
      if (path.endsWith(".png"))
        _contentType = "image/png";
      else if (path.endsWith(".css"))
        _contentType = "text/css";

      _inputStream = localUrl.openStream();
      return SUCCESS;
    }
    return null;
  }

}
