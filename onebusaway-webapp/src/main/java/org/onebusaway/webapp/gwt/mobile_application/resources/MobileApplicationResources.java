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
package org.onebusaway.webapp.gwt.mobile_application.resources;

import org.onebusaway.webapp.gwt.viewkit.resources.ViewKitCssResource;
import org.onebusaway.webapp.gwt.where_library.resources.WhereLibraryCssResource;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.CssResource.Import;

public interface MobileApplicationResources extends ClientBundle {

  public static MobileApplicationResources INSTANCE = GWT.create(MobileApplicationResources.class);

  @Source("MobileApplication.css")
  @Import({ViewKitCssResource.class,WhereLibraryCssResource.class})
  public MobileApplicationCssResource getCSS();
  
  @Source("BookmarksIcon.png")
  public ImageResource getBookmarksIcon();
  
  @Source("ClockIcon.png")
  public ImageResource getClockIcon();
  
  @Source("CrossHairsIcon.png")
  public ImageResource getCrossHairsIcon();
  
  @Source("MagnifyingGlassIcon.png")
  public ImageResource getMagnifyingGlassIcon();
  
  /****
   * Map Control Icons
   ****/
  
  @Source("MapControlUp.png")
  public ImageResource MapControlUpImage();
  
  @Source("MapControlDown.png")
  public ImageResource MapControlDownImage();
  
  @Source("MapControlLeft.png")
  public ImageResource MapControlLeftImage();
  
  @Source("MapControlRight.png")
  public ImageResource MapControlRightImage();
  
  @Source("MapControlZoomIn.png")
  public ImageResource MapControlZoomInImage();
  
  @Source("MapControlZoomOut.png")
  public ImageResource MapControlZoomOutImage();
}
