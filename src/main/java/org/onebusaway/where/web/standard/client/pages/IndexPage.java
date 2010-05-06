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
package org.onebusaway.where.web.standard.client.pages;

import org.onebusaway.common.web.common.client.PageException;
import org.onebusaway.common.web.common.client.context.Context;
import org.onebusaway.common.web.common.client.context.ContextManager;
import org.onebusaway.common.web.common.client.resources.CommonResources;
import org.onebusaway.common.web.common.client.widgets.SpanPanel;
import org.onebusaway.common.web.common.client.widgets.SpanWidget;
import org.onebusaway.where.web.common.client.pages.WhereCommonPage;
import org.onebusaway.where.web.common.client.view.StopFinderPresenter;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

public class IndexPage extends WhereCommonPage {

  private StopFinderImpl _finder;

  public IndexPage(ContextManager contextManager) {
    _finder = new StopFinderImpl(contextManager);
  }

  public Widget create(final Context context) throws PageException {
    _finder.onContextChanged(context);
    return _finder.getWidget();
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
    protected Widget createTitleWidget() {

      SpanPanel titlePanel = new SpanPanel();

      Image g = new Image(CommonResources.INSTANCE.getImageBus().getUrl());
      g.addStyleName("StopFinder-Standard-BusLogo");
      titlePanel.add(g);

      titlePanel.add(new SpanWidget(_msgs.standardIndexPageWhereIsYourBus(), "StopFinder-Standard-Title"));

      return titlePanel;
    }
  }
}
