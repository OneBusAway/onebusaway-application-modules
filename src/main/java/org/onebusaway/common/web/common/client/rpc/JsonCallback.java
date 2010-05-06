package org.onebusaway.common.web.common.client.rpc;

import com.google.gwt.core.client.JavaScriptObject;

public interface JsonCallback {
  public void onSuccess(JavaScriptObject jso);
  public void onFailure();
}
