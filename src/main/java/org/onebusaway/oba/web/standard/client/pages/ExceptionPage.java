/*
 * Copyright 2008 Brian Ferris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.oba.web.standard.client.pages;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import org.onebusaway.where.web.common.client.AbstractPageSource;
import org.onebusaway.where.web.common.client.Context;
import org.onebusaway.where.web.common.client.PageException;
import org.onebusaway.where.web.common.client.widgets.DivWidget;


public class ExceptionPage extends AbstractPageSource {

  @Override
  public Widget create(Context context) throws PageException {

    String message = context.getParam("message");

    FlowPanel panel = new FlowPanel();

    if (message != null)
      panel.add(new DivWidget("error", message));

    Window.setTitle("ERROR");

    return panel;
  }
}
