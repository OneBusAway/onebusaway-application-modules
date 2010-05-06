package org.onebusaway.common.web.api;

import java.io.Serializable;

public class ResponseBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private String version;
  private int code;
  private String text;
  private Object data;

  public ResponseBean(String version, int code, String text, Object data) {
    this.version = version;
    this.code = code;
    this.text = text;
    this.data = data;
  }

  public int getCode() {
    return code;
  }

  public String getText() {
    return text;
  }

  public String getVersion() {
    return version;
  }

  public Object getData() {
    return data;
  }
}
