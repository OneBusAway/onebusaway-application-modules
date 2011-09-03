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

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;

public abstract class Box {

  public abstract int evaluate();

  public static Box height(final Widget widget) {
    return new Box() {
      @Override
      public int evaluate() {
        return widget.getOffsetHeight();
      }
    };
  }

  public static Box height(final Element element) {
    return new Box() {
      @Override
      public int evaluate() {
        return element.getOffsetHeight();
      }
    };
  }

  public static Box top(final Widget widget) {
    return top(widget.getElement());
  }

  public static Box top(final Element element) {
    return new Box() {
      @Override
      public int evaluate() {
        return element.getAbsoluteTop();
      }
    };
  }

  public static Box bottom(final Widget widget) {
    return bottom(widget.getElement());
  }

  public static Box bottom(final Element element) {
    return new Box() {
      @Override
      public int evaluate() {
        return element.getAbsoluteTop() + element.getOffsetHeight();
      }
    };
  }

  public static Box plus(final Box... boxes) {
    return new Box() {

      @Override
      public int evaluate() {
        int total = 0;
        for (Box box : boxes)
          total += box.evaluate();
        return total;
      }
    };
  }

  public static Box minus(final Box a, final Box b) {
    return new Box() {

      @Override
      public int evaluate() {
        return a.evaluate() - b.evaluate();
      }
    };
  }

}
