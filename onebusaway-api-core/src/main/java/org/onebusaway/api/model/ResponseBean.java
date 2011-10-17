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
package org.onebusaway.api.model;

import java.io.Serializable;

import org.onebusaway.csv_entities.schema.annotations.CsvField;

public class ResponseBean implements Serializable {

  private static final long serialVersionUID = 1L;

  private int version;
  private int code;
  private long currentTime = System.currentTimeMillis();
  
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
  
  public long getCurrentTime() {
    return currentTime;
  }

  public Object getData() {
    return data;
  }
}
