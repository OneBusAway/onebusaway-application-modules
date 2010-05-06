package org.onebusaway.webapp.gwt.viewkit;

import org.onebusaway.webapp.gwt.common.PageException;
import org.onebusaway.webapp.gwt.common.context.Context;
import org.onebusaway.webapp.gwt.where_library.pages.WhereCommonPage;

import com.google.gwt.user.client.ui.Widget;

public class ViewControllerPage extends WhereCommonPage {

  private ViewController _controller;

  public ViewControllerPage(ViewController controller) {
    _controller = controller;
  }

  public Widget create(final Context context) throws PageException {
    Widget view = _controller.getView();
    _controller.viewWillAppear();
    _controller.viewDidAppear();
    return view;
  }

  @Override
  public Widget update(Context context) throws PageException {
    return null;
  }

}
