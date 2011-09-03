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
package org.onebusaway.webapp.gwt.viewkit;

import com.google.gwt.event.dom.client.ClickHandler;

public class BarButtonItem {

  public enum EBarButtonSystemItem {
    CROSS_HAIRS, REFRESH
  }

  private ClickHandler clickHandler;

  private EBarButtonSystemItem systemItem = null;

  private String title;

  private BarButtonItem(ClickHandler clickHandler) {
    this.clickHandler = clickHandler;
  }

  public BarButtonItem(EBarButtonSystemItem systemItem,
      ClickHandler clickHandler) {
    this(clickHandler);
    this.systemItem = systemItem;
  }

  public BarButtonItem(String title, ClickHandler clickHandler) {
    this(clickHandler);
    this.title = title;
  }

  public ClickHandler getClickHandler() {
    return clickHandler;
  }

  public void setClickHandler(ClickHandler clickHandler) {
    this.clickHandler = clickHandler;
  }

  public EBarButtonSystemItem getSystemItem() {
    return systemItem;
  }

  public void setSystemItem(EBarButtonSystemItem systemItem) {
    this.systemItem = systemItem;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }
}
