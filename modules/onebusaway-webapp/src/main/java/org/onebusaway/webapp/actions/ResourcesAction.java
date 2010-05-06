package org.onebusaway.webapp.actions;

import org.onebusaway.webapp.impl.resources.ImmutableResourceBundleFactory;
import org.onebusaway.webapp.services.resources.LocalResource;

import com.opensymphony.xwork2.ActionSupport;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;

import java.io.InputStream;
import java.net.URL;

@Results(value = {@Result(type = "stream", params = {
    "contentType", "contentType"})})
public class ResourcesAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  private ImmutableResourceBundleFactory _factory;

  public void setImmutableResourceBundleFactory(
      ImmutableResourceBundleFactory factory) {
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
    LocalResource resource = _factory.getResourceForExternalUrl(_id);
    if (resource != null) {
      URL localUrl = resource.getLocalUrl();
      String path = localUrl.getPath();
      if (path.endsWith(".png")) {
        _contentType = "image/png";
      }

      _inputStream = localUrl.openStream();
      return SUCCESS;
    }
    return null;
  }

}
