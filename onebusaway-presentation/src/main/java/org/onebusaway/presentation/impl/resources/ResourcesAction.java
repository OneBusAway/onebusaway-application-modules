package org.onebusaway.presentation.impl.resources;

import org.onebusaway.presentation.impl.resources.ClientBundleFactory;
import org.onebusaway.presentation.impl.resources.LocalResource;

import com.opensymphony.xwork2.ActionSupport;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;

import java.io.InputStream;
import java.net.URL;

@Results(value = {@Result(type = "stream", params = {
    "contentType", "contentType"})})
public class ResourcesAction extends ActionSupport {

  private static final long serialVersionUID = 1L;

  private ClientBundleFactory _factory;

  public void setClientBundleFactory(
      ClientBundleFactory factory) {
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
      if (path.endsWith(".png"))
        _contentType = "image/png";
      else if( path.endsWith(".css"))
        _contentType = "text/css";

      _inputStream = localUrl.openStream();
      return SUCCESS;
    }
    return null;
  }

}
