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
package com.google.gwt.user.client.ui;

import com.google.gwt.dom.client.Style.Unit;

public class ResizableDockLayoutPanel extends DockLayoutPanel {

  public ResizableDockLayoutPanel(Unit unit) {
    super(unit);
  }
  
  public void setWidgetSize(Widget widget, double size) {
    LayoutData data = (LayoutData) widget.getLayoutData();
    data.size = size;
  }
}
