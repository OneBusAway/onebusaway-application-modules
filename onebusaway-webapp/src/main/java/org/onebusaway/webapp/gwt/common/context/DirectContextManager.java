package org.onebusaway.webapp.gwt.common.context;

public class DirectContextManager extends AbstractContextManager {

  @Override
  public void setContext(Context context) {
    fireContextChanged(context);
  }

  @Override
  public String getContextAsString(Context context) {
    return context.getParams().toString();
  }
}
