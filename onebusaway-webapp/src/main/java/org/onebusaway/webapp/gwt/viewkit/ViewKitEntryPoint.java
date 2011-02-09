package org.onebusaway.webapp.gwt.viewkit;

import org.onebusaway.webapp.gwt.viewkit.resources.ViewKitResources;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.StyleInjector;

public class ViewKitEntryPoint implements EntryPoint {

  @Override
  public void onModuleLoad() {
    StyleInjector.injectStylesheet(ViewKitResources.INSTANCE.getCSS().getText());
  }
}
