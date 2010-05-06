package org.onebusaway.webapp.gwt.common.rpc;

import com.google.gwt.core.client.JavaScriptObject;

public interface JsonCallback {
  public void onSuccess(JavaScriptObject jso);
  public void onFailure();
}
