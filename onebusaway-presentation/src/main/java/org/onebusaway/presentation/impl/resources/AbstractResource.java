package org.onebusaway.presentation.impl.resources;

import java.net.URL;

import org.onebusaway.presentation.services.resources.Resource;

class AbstractResource implements Resource {

  private String externalId;

  private String externalUrl;

  private URL localUrl;
  
  private long contentLength = -1;

  private long lastModifiedTime;

  public String getExternalId() {
    return externalId;
  }

  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }

  public String getExternalUrl() {
    return externalUrl;
  }

  public void setExternalUrl(String externalUrl) {
    this.externalUrl = externalUrl;
  }

  public URL getLocalUrl() {
    return localUrl;
  }

  public void setLocalUrl(URL localUrl) {
    this.localUrl = localUrl;
  }

  public long getLastModifiedTime() {
    return lastModifiedTime;
  }

  public void setLastModifiedTime(long lastModifiedTime) {
    this.lastModifiedTime = lastModifiedTime;
  }

  public long getContentLength() {
    return contentLength;
  }

  public void setContentLength(long contentLength) {
    this.contentLength = contentLength;
  }
}