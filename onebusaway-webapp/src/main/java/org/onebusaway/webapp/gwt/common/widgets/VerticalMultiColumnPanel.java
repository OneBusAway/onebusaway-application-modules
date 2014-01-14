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
package org.onebusaway.webapp.gwt.common.widgets;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * This class manages a HorizontalPanel that contains VerticalPanels. Thus, it
 * is like a VerticalPanel but with more than one column.
 * 
 * You specify the number of widgets allowed per column (VerticalPanel) and it
 * will create as many columns as needed.
 */
public class VerticalMultiColumnPanel extends Composite {
  private HorizontalPanel _hp;
  private int _maxWidgetsPerColumn;
  private int _numWidgets = 0;

  public VerticalMultiColumnPanel(int maxWidgetsPerColumn) {
    _maxWidgetsPerColumn = maxWidgetsPerColumn;
    _hp = new HorizontalPanel();
    initWidget(_hp); // Let Composite know our root
  }

  /** Add widget to the panel. */
  public void insert(Widget w, int beforeIndex) {
    _numWidgets++;

    // add new column if needed
    if (_hp.getWidgetCount() * _maxWidgetsPerColumn < _numWidgets) {
      VerticalPanel vp = new VerticalPanel();
      _hp.add(vp);
    }

    // insert widget
    int column = beforeIndex / _maxWidgetsPerColumn;
    int position = beforeIndex % _maxWidgetsPerColumn;
    ((VerticalPanel) _hp.getWidget(column)).insert(w, position);

    // "carry" overflow to next columns as needed
    for (int i = 0; i < _hp.getWidgetCount() - 1; i++) {
      VerticalPanel vp1 = (VerticalPanel) _hp.getWidget(i);
      if (vp1.getWidgetCount() > _maxWidgetsPerColumn) {
        VerticalPanel vp2 = (VerticalPanel) _hp.getWidget(i + 1);
        Widget move = vp1.getWidget(_maxWidgetsPerColumn);
        vp2.insert(move, 0);
        vp1.remove(move);
      }
    }
  }

  /** Remove a widget. Currently does not rebalance columns. */
  public void remove(Widget w) {
    for (int i = 0; i < _hp.getWidgetCount(); i++) {
      VerticalPanel vp = (VerticalPanel) _hp.getWidget(i);
      if (vp.remove(w)) {
        _numWidgets--;
      }
    }
  }

}