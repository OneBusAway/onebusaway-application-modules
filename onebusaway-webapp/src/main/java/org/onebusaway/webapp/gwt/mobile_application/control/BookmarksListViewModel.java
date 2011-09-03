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

import org.onebusaway.webapp.gwt.mobile_application.MobileApplicationContext;
import org.onebusaway.webapp.gwt.mobile_application.model.Bookmark;
import org.onebusaway.webapp.gwt.viewkit.IndexPath;
import org.onebusaway.webapp.gwt.viewkit.ListViewController;
import org.onebusaway.webapp.gwt.viewkit.ListViewModel;
import org.onebusaway.webapp.gwt.viewkit.ListViewRow;
import org.onebusaway.webapp.gwt.viewkit.NavigationController;
import org.onebusaway.webapp.gwt.viewkit.ViewController;
import org.onebusaway.webapp.gwt.viewkit.ListViewRow.ListViewRowStyle;

public class BookmarksListViewModel extends ListViewModel {

  private List<Bookmark> _bookmarks = new ArrayList<Bookmark>();

  @Override
  public void willReload() {
    MobileApplicationDao dao = MobileApplicationContext.getDao();
    _bookmarks = dao.getBookmarks();
  }

  @Override
  public int getNumberOfSections() {
    return 1;
  }

  @Override
  public int getNumberOfRowsInSection(int sectionIndex) {
    return _bookmarks.isEmpty() ? 1 : _bookmarks.size();
  }

  @Override
  public ListViewRow getListViewRowForSectionAndRow(int sectionIndex,
      int rowIndex) {

    if (_bookmarks.isEmpty()) {
      ListViewRow row = new ListViewRow();
      row.setStyle(ListViewRowStyle.DEFAULT);
      row.setText("No bookmarks");
      return row;
    }

    Bookmark bookmark = _bookmarks.get(rowIndex);

    ListViewRow row = new ListViewRow();
    row.setStyle(ListViewRowStyle.DEFAULT);
    row.setText(bookmark.getName());

    return row;
  }

  @Override
  public void handleContext(List<String> path, Map<String, String> context) {
    if (path.isEmpty())
      return;

    String rawBookmarkId = path.remove(0);
    try {
      int bookmarkId = Integer.parseInt(rawBookmarkId);
      for (Bookmark bookmark : _bookmarks) {
        if (bookmark.getId() == bookmarkId) {
          String stopId = bookmark.getStopId();
          ViewController next = Actions.ensureStopIsSelected(getListViewController(), stopId);
          next.handleContext(path, context);
          return;
        }
      }
    } catch (NumberFormatException ex) {
      System.err.println("invalid bookmark id=" + rawBookmarkId);
    }

    // If we've made it this far, it means we didn't find a match. Let's clear
    // the remainder of the view stack
    ListViewController vc = getListViewController();
    NavigationController nav = vc.getNavigationController();
    nav.popToViewController(vc);
  }

  @Override
  public void retrieveContext(List<String> path, Map<String, String> context) {
    ListViewController vc = getListViewController();
    IndexPath index = vc.getSelectionIndex();
    System.out.println(index);
    if (index != null) {
      Bookmark bookmark = _bookmarks.get(index.getRow());
      path.add(Integer.toString(bookmark.getId()));
    }
  }

  @Override
  public boolean willRespondToRowClicks() {
    return true;
  }

  @Override
  public void onRowClick(ListViewController listViewController,
      int sectionIndex, int rowIndex) {

    if (_bookmarks.isEmpty())
      return;

    Bookmark bookmark = _bookmarks.get(rowIndex);
    String stopId = bookmark.getStopId();

    Actions.showArrivalsAndDeparturesForStop(
        listViewController.getNavigationController(), stopId);
  }

}
