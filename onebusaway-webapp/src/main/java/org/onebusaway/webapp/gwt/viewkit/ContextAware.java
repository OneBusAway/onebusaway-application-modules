package org.onebusaway.webapp.gwt.viewkit;

import java.util.List;
import java.util.Map;

public interface ContextAware {
  public void handleContext(List<String> path, Map<String, String> context);
  
  public void retrieveContext(List<String> path, Map<String, String> context);
}
