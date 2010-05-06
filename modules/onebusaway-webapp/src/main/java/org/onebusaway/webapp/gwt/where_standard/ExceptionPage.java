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
package org.onebusaway.webapp.gwt.where_standard;

import org.onebusaway.webapp.gwt.common.AbstractPageSource;
import org.onebusaway.webapp.gwt.common.PageException;
import org.onebusaway.webapp.gwt.common.context.Context;
import org.onebusaway.webapp.gwt.common.widgets.DivWidget;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class ExceptionPage extends AbstractPageSource {

  private static WhereStopFinderCssResource _css = WhereStopFinderStandardResources.INSTANCE.getCSS();
  
  @Override
  public Widget create(Context context) throws PageException {

    
    String message = context.getParam("message");

    FlowPanel panel = new FlowPanel();

    if (message != null)
      panel.add(new DivWidget(message, _css.exceptionPageError()));

    Window.setTitle("ERROR");

    return panel;
  }
}
