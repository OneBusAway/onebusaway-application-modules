package org.onebusaway.common.web.common.client.context;

public interface ContextManager {

  public void addContextListener(ContextListener listener);

  public void removeContextListener(ContextListener listener);

  public Context getContext();

  public void setContext(Context context);
}
