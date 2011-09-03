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
package org.onebusaway.webapp.gwt.mobile_application.control;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.webapp.gwt.mobile_application.MobileApplicationContext;
import org.onebusaway.webapp.gwt.viewkit.ListViewController;
import org.onebusaway.webapp.gwt.viewkit.ListViewModel;
import org.onebusaway.webapp.gwt.viewkit.ListViewRow;
import org.onebusaway.webapp.gwt.viewkit.NavigationController;
import org.onebusaway.webapp.gwt.viewkit.ViewController;
import org.onebusaway.webapp.gwt.viewkit.ListViewRow.ListViewRowStyle;

public class RecentStopsListViewModel extends ListViewModel {

  private List<StopBean> _recentStops = new ArrayList<StopBean>();

  @Override
  public void willReload() {
    MobileApplicationDao dao = MobileApplicationContext.getDao();
    _recentStops = dao.getRecentStops();
  }

  @Override
  public int getNumberOfSections() {
    return 1;
  }

  @Override
  public int getNumberOfRowsInSection(int sectionIndex) {
    return _recentStops.isEmpty() ? 1 : _recentStops.size();
  }

  @Override
  public ListViewRow getListViewRowForSectionAndRow(int sectionIndex,
      int rowIndex) {

    if (_recentStops.isEmpty()) {
      ListViewRow row = new ListViewRow();
      row.setStyle(ListViewRowStyle.DEFAULT);
      row.setText("No recent stops");
      return row;
    }

    StopBean stop = _recentStops.get(rowIndex);

    ListViewRow row = new ListViewRow();
    row.setStyle(ListViewRowStyle.DETAIL);
    row.setText(stop.getName());

    StringBuilder b = new StringBuilder();

    if (stop.getDirection() != null)
      b.append(stop.getDirection()).append(" bound - ");
    b.append("Routes:");
    boolean first = true;
    for (RouteBean route : stop.getRoutes()) {
      if (!first)
        b.append(",");
      b.append(" ");
      b.append(route.getShortName());
      first = false;
    }
    row.setDetailText(b.toString());

    return row;
  }

  @Override
  public boolean willRespondToRowClicks() {
    return true;
  }

  @Override
  public void onRowClick(ListViewController listViewController,
      int sectionIndex, int rowIndex) {

    if (_recentStops.isEmpty())
      return;

    StopBean stop = _recentStops.get(rowIndex);
    Actions.showArrivalsAndDeparturesForStop(
        listViewController.getNavigationController(), stop.getId());
  }

  @Override
  public void handleContext(List<String> path, Map<String, String> context) {
    
    if( path.isEmpty() )
      return;
    
    String stopId = path.remove(0);
    ViewController next = Actions.ensureStopIsSelected(getListViewController(), stopId);
    next.handleContext(path, context);
  }

  @Override
  public void retrieveContext(List<String> path, Map<String, String> context) {
    ListViewController controller = getListViewController();
    NavigationController nav = controller.getNavigationController();
    ViewController next = nav.getNextController(controller);
    String stopId = Actions.getStopIdForViewController(next);
    if (stopId != null)
      path.add(stopId);
  }
}
