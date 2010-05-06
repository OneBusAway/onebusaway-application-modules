package org.onebusaway.webapp.gwt.common.context;


public class DirectContextManager extends AbstractContextManager {

  public void setContext(Context context) {
    fireContextChanged(context);
  }

}
