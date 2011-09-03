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
package org.onebusaway.webapp.gwt.common.layout;

import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.List;

public class BoxLayoutManager {

  private List<Constraint> _constraints = new ArrayList<Constraint>();

  private ResizeHandlerImpl _handler = new ResizeHandlerImpl();

  public BoxLayoutManager() {
    Window.addResizeHandler(_handler);
  }

  public void addMatch(Widget parent, Widget child, EBoxLayoutDirection direction) {
    addConstraint(new MatchConstraint(direction, parent, child));
  }

  public void addSetHeightConstraint(Box source, Widget... targets) {
    addConstraint(new BoxConstraint(EBoxLayoutDirection.VERTICAL,source,targets));
  }

  public void addFillRemaining(EBoxLayoutDirection direction, ComplexPanel parent, Widget target, Widget... additional) {
    addConstraint(new FillRemainingSpace(direction, parent, target, additional));
  }

  public void addMapWidget(MapWidget mapWidget) {
    addConstraint(new MapWidgetConstraint(mapWidget));
  }

  public void refresh() {
    DeferredCommand.addCommand(new Command() {
      public void execute() {
        _handler.onResize(new ResizeEventImpl(Window.getClientWidth(), Window.getClientHeight()));
      }
    });
  }

  /*****************************************************************************
   * 
   ****************************************************************************/

  public void addConstraint(Constraint c) {
    _constraints.add(c);
  }

  /*****************************************************************************
   * 
   ****************************************************************************/

  private class ResizeHandlerImpl implements ResizeHandler {
    public void onResize(ResizeEvent arg0) {
      for (Constraint constraint : _constraints)
        constraint.apply();
    }
  }

  public interface Constraint {
    public void apply();
  }

  private static abstract class DirectedConstraint implements Constraint {

    private EBoxLayoutDirection _direction;

    public DirectedConstraint(EBoxLayoutDirection direction) {
      _direction = direction;
    }

    protected int getDirectedSize(Widget widget) {
      return _direction.equals(EBoxLayoutDirection.VERTICAL) ? widget.getOffsetHeight() : widget.getOffsetWidth();
    }

    protected void setDirectedSize(Widget widget, int size) {
      if (_direction.equals(EBoxLayoutDirection.VERTICAL))
        widget.setHeight(size + "px");
      else
        widget.setWidth(size + "px");
    }

  }

  public static class BoxConstraint extends DirectedConstraint {

    private Box _box;

    private Widget[] _widgets;

    public BoxConstraint(EBoxLayoutDirection direction, Box box, Widget... widgets) {
      super(direction);
      _box = box;
      _widgets = widgets;
    }

    public void apply() {
      int size = _box.evaluate();
      size = Math.max(0, size);
      Window.setStatus("Size: " + size);
      for (Widget widget : _widgets)
        setDirectedSize(widget, size);
    }
  }

  private static class MatchConstraint extends DirectedConstraint {

    private Widget _parent;

    private Widget _widget;

    public MatchConstraint(EBoxLayoutDirection direction, Widget parent, Widget widget) {
      super(direction);
      _parent = parent;
      _widget = widget;
    }

    public void apply() {
      int size = getDirectedSize(_parent);
      setDirectedSize(_widget, size);
    }
  }

  private class FillRemainingSpace extends DirectedConstraint {

    private ComplexPanel _parent;
    private Widget _widget;
    private Widget[] _additional;

    public FillRemainingSpace(EBoxLayoutDirection direction, ComplexPanel parent, Widget widget, Widget[] children) {
      super(direction);
      _parent = parent;
      _widget = widget;
      _additional = children;
    }

    public void apply() {

      System.out.println("=== HERE WE GO ===");

      int totalSize = getDirectedSize(_parent);
      int otherSize = 0;
      for (int i = 0; i < _parent.getWidgetCount(); i++) {
        Widget w = _parent.getWidget(i);
        if (w.equals(_widget))
          continue;
        otherSize += getDirectedSize(w);
      }

      int remainingSize = totalSize - otherSize;

      System.out.println("  total=" + totalSize + " other=" + otherSize + " remaining=" + remainingSize);

      remainingSize = Math.max(0, remainingSize);

      for (Widget widget : _additional)
        setDirectedSize(widget, remainingSize);

      setDirectedSize(_widget, remainingSize);
    }
  }

  private static class MapWidgetConstraint implements Constraint {

    private MapWidget _map;

    public MapWidgetConstraint(MapWidget map) {
      _map = map;
    }

    public void apply() {
      _map.checkResize();
    }
  }
  
  private static class ResizeEventImpl extends ResizeEvent {
    protected ResizeEventImpl(int width, int height) {
      super(width, height);
    }
  }
}
