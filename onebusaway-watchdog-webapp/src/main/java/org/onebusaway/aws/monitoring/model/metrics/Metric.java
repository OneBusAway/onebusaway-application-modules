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
package org.onebusaway.aws.monitoring.model.metrics;

public class Metric implements java.io.Serializable {
  private long currentTimestamp;
  private String metricName;
  private Object metricValue;
  private String response;
  private String errorMessage;
  
  public long getCurrentTimestamp() {
    return currentTimestamp;
  }
  public void setCurrentTimestamp(long currentTimestamp) {
    this.currentTimestamp = currentTimestamp;
  }
  public String getMetricName() {
    return metricName;
  }
  public void setMetricName(String metricName) {
    this.metricName = metricName;
  }
  public Object getMetricValue() {
    return metricValue;
  }
  public void setMetricValue(Object metricValue) {
    this.metricValue = metricValue;
  }
  public String getResponse() {
    return response;
  }
  public void setResponse(String response) {
    this.response = response;
  }
  public String getErrorMessage() {
    return errorMessage;
  }
  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
  
  
}
