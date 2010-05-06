package org.onebusaway.common.web.common.client.context;


public class DirectContextManager extends AbstractContextManager {

  public void setContext(Context context) {
    fireContextChanged(context);
  }

}
