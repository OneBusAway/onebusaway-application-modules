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
package org.onebusaway.webapp.gwt.mobile_application;

import org.onebusaway.webapp.gwt.mobile_application.resources.MobileApplicationResources;
import org.onebusaway.webapp.gwt.mobile_application.view.BookmarksViewController;
import org.onebusaway.webapp.gwt.mobile_application.view.RecentStopsViewController;
import org.onebusaway.webapp.gwt.mobile_application.view.SearchViewController;
import org.onebusaway.webapp.gwt.viewkit.AbstractApplication;
import org.onebusaway.webapp.gwt.viewkit.NavigationController;
import org.onebusaway.webapp.gwt.viewkit.TabBarController;
import org.onebusaway.webapp.gwt.viewkit.ViewController;
import org.onebusaway.webapp.gwt.viewkit.events.ViewControllerSelectedEvent;
import org.onebusaway.webapp.gwt.viewkit.events.ViewControllerSelectedHandler;

import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Image;

public class MobileApplication extends AbstractApplication {

  private static final MobileApplicationResources _resources = MobileApplicationResources.INSTANCE;

  public MobileApplication() {

    TabBarController tabs = MobileApplicationContext.getRootController();

    NavigationController maps = createNavigationController("map", "Map",
        _resources.getCrossHairsIcon(),
        MobileApplicationContext.getMapViewController());

    NavigationController bookmarks = createNavigationController("bookmarks",
        "Bookmarks", _resources.getBookmarksIcon(),
        new BookmarksViewController());

    NavigationController recentStops = createNavigationController("recent",
        "Recent", _resources.getClockIcon(), new RecentStopsViewController());

    NavigationController search = createNavigationController("search",
        "Search", _resources.getMagnifyingGlassIcon(),
        new SearchViewController());

    tabs.addViewController(maps);
    tabs.addViewController(bookmarks);
    tabs.addViewController(recentStops);
    tabs.addViewController(search);

    tabs.addVieControllerSelectedHandler(new ViewControllerSelectedHandler() {
      @Override
      public void handleViewControllerSelected(ViewControllerSelectedEvent event) {
        NavigationController nav = (NavigationController) event.getViewController();
        nav.popToRootViewController();
      }
    });

    setRootViewController(tabs);

    StyleInjector.inject(_resources.getCSS().getText());
  }

  private NavigationController createNavigationController(String id,
      String title, ImageResource img, ViewController firstView) {

    Image image = new Image(img.getURL(), img.getLeft(), img.getTop(),
        img.getWidth(), img.getHeight());

    NavigationController navigationController = new NavigationController();
    navigationController.getTabBarItem().setName(title);
    navigationController.getTabBarItem().setImage(image);
    navigationController.getNavigationItem().setId(id);
    navigationController.getNavigationItem().setTitle(title);
    navigationController.pushViewController(firstView);
    return navigationController;
  }
}
