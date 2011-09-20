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
package org.onebusaway.webapp.gwt.viewkit.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.CssResource.Strict;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;

public interface ViewKitResources extends ClientBundle {

  public static ViewKitResources INSTANCE = GWT.create(ViewKitResources.class);

  @Source("ViewKit.css")
  @Strict
  public ViewKitCssResource getCSS();
  
  @Source("background.png")
  @ImageOptions(repeatStyle=RepeatStyle.Both)
  public ImageResource backgroundImage();
  
  @Source("NavigationBarBackground.png")
  @ImageOptions(repeatStyle=RepeatStyle.Both)
  public ImageResource NavigationBarBackgroundImage();
  
  @Source("NavigationBarButtonLeftArrow.png")
  public ImageResource NavigationBarButtonLeftArrowImage();
  
  @Source("NavigationBarButtonLeftRounded.png")
  public ImageResource NavigationBarButtonLeftRoundedImage();
  
  @Source("NavigationBarButtonBackground.png")
  @ImageOptions(repeatStyle=RepeatStyle.Horizontal)
  public ImageResource NavigationBarButtonBackgroundImage();
  
  @Source("NavigationBarButtonRightRounded.png")
  public ImageResource NavigationBarButtonRightRoundedImage();
  
  @Source("SystemBarButtonCrossHairs.png")
  public ImageResource BarButtonSystemCrossHairsImage();
  
  @Source("SystemBarButtonRefresh.png")
  public ImageResource BarButtonSystemRefreshImage();  
}
