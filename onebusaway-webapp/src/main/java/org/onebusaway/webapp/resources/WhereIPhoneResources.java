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
package org.onebusaway.webapp.resources;

import org.onebusaway.presentation.services.resources.WebappSource;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

public interface WhereIPhoneResources extends ClientBundle {

  @WebappSource("/images/where/iphone/arrow.gif")
  public ImageResource ImageArrow();

  @WebappSource("/images/where/iphone/arrow-bus.png")
  public ImageResource ImageArrowBus();

  @WebappSource("/images/where/iphone/bar.png")
  public ImageResource ImageBar();

  @WebappSource("/images/where/iphone/bg.png")
  public ImageResource ImageBackground();

  @WebappSource("/images/where/iphone/bgbot.png")
  public ImageResource ImageBackgroundBot();

  @WebappSource("/images/where/iphone/bgbot2.png")
  public ImageResource ImageBackgroundBot2();

  @WebappSource("/images/where/iphone/busicon.png")
  public ImageResource ImageBusIcon();

  @WebappSource("/images/where/iphone/hborder.png")
  public ImageResource ImageHBorder();

  @WebappSource("/where/iphone/StopIdentification-Schedule.jpg")
  public ImageResource ImageStopIdentificationSchedule();

  @WebappSource("/where/iphone/StopIdentification-Shelter.jpg")
  public ImageResource ImageStopIdentificationShelter();

  @WebappSource("/images/where/iphone/spinner3-black.gif")
  public ImageResource ProgressSpinner();

  @WebappSource("/images/where/iphone/NearbyStopsForRouteArrow.png")
  public ImageResource NearbyStopsForRouteArrow();

  @WebappSource("/WEB-INF/css/where-iphone.css")
  public CssResource getCss();
}
