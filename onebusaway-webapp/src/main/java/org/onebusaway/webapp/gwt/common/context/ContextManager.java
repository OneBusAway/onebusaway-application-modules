package org.onebusaway.webapp.gwt.common.context;

public interface ContextManager {

  public void addContextListener(ContextListener listener);

  public void removeContextListener(ContextListener listener);

  public Context getContext();
  
  public String getContextAsString(Context context);

  public void setContext(Context context);
}
