package org.onebusaway.api.model;

import java.io.Serializable;

import org.onebusaway.gtfs.csv.schema.annotations.CsvField;

public class ResponseBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private int version;
  private int code;
  
  @CsvField(optional = true)
  private String text;
  
  @CsvField(optional = true)
  private Object data;

  public ResponseBean(int version, int code, String text, Object data) {
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

  public int getVersion() {
    return version;
  }

  public Object getData() {
    return data;
  }
}
