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
package org.onebusaway.webapp.gwt.where_standard;

import org.onebusaway.webapp.gwt.common.context.HistoryContextManager;
import org.onebusaway.webapp.gwt.common.resources.StandardApplicationContainer;
import org.onebusaway.webapp.gwt.where_library.view.StopFinderPresenter;
import org.onebusaway.webapp.gwt.where_library.view.StopFinderWidget;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.StyleInjector;

public class WhereStopFinderStandardApplication implements EntryPoint {

  @Override
  public void onModuleLoad() {
    
    HistoryContextManager manager = new HistoryContextManager();

    StopFinderWidget widget = new StopFinderWidget();
    widget.setTitleWidget(new TitleWidget());

    StopFinderPresenter stopFinder = new StopFinderPresenter(manager);

    widget.setStopFinder(stopFinder);
    stopFinder.setWidget(widget);

    StandardApplicationContainer.add(widget);

    StyleInjector.inject(WhereStopFinderStandardResources.INSTANCE.getCSS().getText());

    stopFinder.initialize();
  }
}
