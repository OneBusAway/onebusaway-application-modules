package org.onebusaway.presentation.impl.resources;

import java.io.InputStream;
import java.net.URL;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.presentation.services.resources.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;

@Results(value = {
    @Result(type = "stream", params = {"contentType", "contentType"}),
    @Result(type = "httpheader", name = "NotFound", params = {
        "status", "404"})})
public class ResourceAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  private ResourceService _resourceService;

  @Autowired
  public void setResourceService(ResourceService resourceService) {
    _resourceService = resourceService;
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

    URL localUrl = _resourceService.getLocalUrlForExternalId(_id);

    if (localUrl != null) {
      String path = localUrl.getPath();
      if (path.endsWith(".png"))
        _contentType = "image/png";
      else if (path.endsWith(".css"))
        _contentType = "text/css";
      else if (path.endsWith(".js"))
        _contentType = "text/javascript";

      _inputStream = localUrl.openStream();
      return SUCCESS;
    }

    return "NotFound";
  }

}
