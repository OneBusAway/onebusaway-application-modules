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

import com.google.gwt.i18n.client.BidiUtils;
import com.google.gwt.i18n.client.HasDirection.Direction;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FocusWidget;

public abstract class AbstractElementWidget extends FocusWidget {

  public AbstractElementWidget(String body, String... styles) {
    Element el = createElement();
    setElement(el);
    for (String style : styles)
      addStyleName(style);
    DOM.setInnerHTML(getElement(), body);
  }

  protected AbstractElementWidget(Element el) {
    this.setElement(el);
  }

  protected abstract Element createElement();

  public String getHTML() {
    return DOM.getInnerHTML(getElement());
  }

  public void setHTML(String html) {
    DOM.setInnerHTML(getElement(), html);
  }

  public Direction getDirection() {
    return BidiUtils.getDirectionOnElement(getElement());
  }

  public String getText() {
    return DOM.getInnerText(getElement());
  }

  public boolean getWordWrap() {
    return !DOM.getStyleAttribute(getElement(), "whiteSpace").equals("nowrap");
  }

  public void setDirection(Direction direction) {
    BidiUtils.setDirectionOnElement(getElement(), direction);
  }

  public void setText(String text) {
    DOM.setInnerText(getElement(), text);
  }

  public void setWordWrap(boolean wrap) {
    DOM.setStyleAttribute(getElement(), "whiteSpace", wrap ? "normal"
        : "nowrap");
  }
}
