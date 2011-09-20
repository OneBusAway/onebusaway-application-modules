/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
