/**
 * 
 */
package org.onebusaway.users.impl.authentication;

class PluginAction {

  private String baseUrl;

  private String plugin;

  private String action;

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public String getPlugin() {
    return plugin;
  }

  public void setPlugin(String plugin) {
    this.plugin = plugin;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }
}