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
package edu.washington.cs.rse.transit.web.oba.standard.client.pages;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.washington.cs.rse.transit.web.oba.common.client.AbstractPageSource;
import edu.washington.cs.rse.transit.web.oba.common.client.Context;
import edu.washington.cs.rse.transit.web.oba.common.client.model.RouteBean;

public class RoutesPage extends AbstractPageSource {

    public Widget create(final Context context) {

        VerticalPanel panel = new VerticalPanel();

        panel.add(new HTML("<h1>Routes</h1>"));

        VerticalPanel routes = new VerticalPanel();

        _service.getActiveRoutes(new RoutesHandler(routes));
        panel.add(routes);

        return panel;
    }

    private class RoutesHandler implements AsyncCallback<List<RouteBean>> {

        private VerticalPanel _panel;

        public RoutesHandler(VerticalPanel panel) {
            _panel = panel;
        }

        public void onSuccess(List<RouteBean> routes) {
            while (_panel.getWidgetCount() > 0)
                _panel.remove(0);
            for (RouteBean route : routes) {
                String target = getTarget("service_patterns", "route", route.getNumber());
                _panel.add(new Hyperlink(Integer.toString(route.getNumber()), target));
            }
        }

        public void onFailure(Throwable ex) {
            handleException(ex);
        }
    }
}
