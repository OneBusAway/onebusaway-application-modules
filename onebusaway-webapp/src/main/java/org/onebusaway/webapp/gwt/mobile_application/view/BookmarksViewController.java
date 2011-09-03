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
package org.onebusaway.webapp.gwt.mobile_application.view;

import org.onebusaway.webapp.gwt.mobile_application.MobileApplicationContext;
import org.onebusaway.webapp.gwt.mobile_application.control.BookmarksListViewModel;
import org.onebusaway.webapp.gwt.mobile_application.control.MobileApplicationDao;
import org.onebusaway.webapp.gwt.viewkit.BarButtonItem;
import org.onebusaway.webapp.gwt.viewkit.ListViewController;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class BookmarksViewController extends ListViewController {
  
  public BookmarksViewController() {
    setModel(new BookmarksListViewModel());
    
    getNavigationItem().setTitle("Bookmarks");
    getNavigationItem().setRightBarButtonItem(
        new BarButtonItem("Clear", new ClearBookmarksHandler()));
  }

  @Override
  public void viewWillAppear() {
    super.viewWillAppear();
    refreshModel();
  }

  private class ClearBookmarksHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent arg0) {
      MobileApplicationDao dao = MobileApplicationContext.getDao();
      dao.clearBookmarks();
      refreshModel();
    }

  }
}
