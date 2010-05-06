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

import org.onebusaway.webapp.gwt.common.PageException;
import org.onebusaway.webapp.gwt.common.context.Context;
import org.onebusaway.webapp.gwt.common.context.ContextManager;
import org.onebusaway.webapp.gwt.common.resources.CommonResources;
import org.onebusaway.webapp.gwt.common.widgets.SpanPanel;
import org.onebusaway.webapp.gwt.common.widgets.SpanWidget;
import org.onebusaway.webapp.gwt.where_library.pages.WhereCommonPage;
import org.onebusaway.webapp.gwt.where_library.view.StopFinderPresenter;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

import java.util.Set;

public class IndexPage extends WhereCommonPage {

  private static WhereStopFinderCssResource _css = WhereStopFinderStandardResources.INSTANCE.getCSS();

  private StopFinderImpl _finder;

  public IndexPage(ContextManager contextManager) {
    _finder = new StopFinderImpl(contextManager);
  }

  public Widget create(final Context context) throws PageException {
    return _finder.initialize(context);
  }

  @Override
  public Widget update(Context context) throws PageException {
    _finder.onContextChanged(context);
    return null;
  }

  private class StopFinderImpl extends StopFinderPresenter {

    public StopFinderImpl(ContextManager contextManager) {
      super(contextManager);
    }

    @Override
    protected Widget createTitleWidget(Set<String> styles) {

      SpanPanel titlePanel = new SpanPanel();
      styles.add(_css.stopFinderTopPanelTitlePanel());
      
      Image g = new Image(CommonResources.INSTANCE.getImageBus().getUrl());
      g.addStyleName(_css.stopFinderStandardBusLogo());
      titlePanel.add(g);

      titlePanel.add(new SpanWidget(_msgs.standardIndexPageWhereIsYourBus(),
          _css.stopFinderStandardTitle()));

      return titlePanel;
    }
  }
}
