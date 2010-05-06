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
package edu.washington.cs.rse.transit.web.oba.iphone.client.pages;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.washington.cs.rse.transit.web.oba.common.client.AbstractPageSource;
import edu.washington.cs.rse.transit.web.oba.common.client.Context;
import edu.washington.cs.rse.transit.web.oba.common.client.widgets.DivWidget;

public abstract class GenericRedirectPage extends AbstractPageSource {

  public Widget create(final Context context) {

    final String url = getRedirectUrl(context);

    FlowPanel panel = new FlowPanel();
    panel.addStyleName("panel");

    DivWidget description = new DivWidget(
        "The iPhone app has been updated and URLs have changed.");
    description.addStyleName("label");
    description.addStyleName("update");
    panel.add(description);

    DivWidget bookmarks = new DivWidget("<em>Please update your bookmarks.</em>");
    bookmarks.addStyleName("label");
    bookmarks.addStyleName("update");
    panel.add(bookmarks);

    DivWidget redirect = new DivWidget(
        "We will redirect you shortly or you can just click <a href=\"" + url
            + "\">here</a>.");
    redirect.addStyleName("label");
    redirect.addStyleName("update");
    panel.add(redirect);

    Timer t = new Timer() {
      @Override
      public void run() {
        Location.assign(url);
      }
    };
    t.schedule(10000);

    return panel;
  }

  public abstract String getRedirectUrl(Context context);
}
