package org.onebusaway.transit_data_federation.bundle.model;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class GtfsBundle {

  private File path;
  
  private URL url;

  private String defaultAgencyId;

  private Map<String, String> agencyIdMappings = new HashMap<String, String>();

  public File getPath() {
      return path;
  }

  public void setPath(File path) {
      this.path = path;
  }

  public URL getUrl() {
      return url;
  }

  public void setUrl(URL url) {
      this.url = url;
  }

  public String getDefaultAgencyId() {
      return defaultAgencyId;
  }

  public void setDefaultAgencyId(String defaultAgencyId) {
      this.defaultAgencyId = defaultAgencyId;
  }

  public Map<String, String> getAgencyIdMappings() {
      return agencyIdMappings;
  }

  public void setAgencyIdMappings(Map<String, String> agencyIdMappings) {
      this.agencyIdMappings = agencyIdMappings;
  }

}
