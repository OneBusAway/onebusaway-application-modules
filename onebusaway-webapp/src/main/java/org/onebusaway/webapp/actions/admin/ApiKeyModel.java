/**
 * 
 */
package org.onebusaway.webapp.actions.admin;

public class ApiKeyModel {
  private String apiKey;
  private Long minApiRequestInterval;

  public String getApiKey() {
    return apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  public Long getMinApiRequestInterval() {
    return minApiRequestInterval;
  }

  public void setMinApiRequestInterval(Long minApiRequestInterval) {
    this.minApiRequestInterval = minApiRequestInterval;
  }
}