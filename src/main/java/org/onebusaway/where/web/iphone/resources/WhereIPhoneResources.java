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
package org.onebusaway.where.web.iphone.resources;

import org.onebusaway.common.web.gwt.resources.CssDataResource;
import org.onebusaway.common.web.gwt.resources.DataResource;
import org.onebusaway.common.web.gwt.resources.ImmutableResourceBundle;

public interface WhereIPhoneResources extends ImmutableResourceBundle {

  @Resource("/images/where/iphone/arrow.gif")
  public DataResource getImageArrow();

  @Resource("/images/where/iphone/arrow-bus.png")
  public DataResource getImageArrowBus();

  @Resource("/images/where/iphone/bar.png")
  public DataResource getImageBar();

  @Resource("/images/where/iphone/bg.png")
  public DataResource getImageBackground();

  @Resource("/images/where/iphone/bgbot.png")
  public DataResource getImageBackgroundBot();

  @Resource("/images/where/iphone/bgbot2.png")
  public DataResource getImageBackgroundBot2();

  @Resource("/images/where/iphone/busicon.png")
  public DataResource getImageBusIcon();

  @Resource("/images/where/iphone/hborder.png")
  public DataResource getImageHBorder();

  @Resource("/where/iphone/StopIdentification-Schedule.jpg")
  public DataResource getImageStopIdentificationSchedule();
  
  @Resource("/where/iphone/StopIdentification-Shelter.jpg")
  public DataResource getImageStopIdentificationShelter();
  
  @Resource("/images/where/iphone/spinner3-black.gif")
  public DataResource getProgressSpinner();
  
  @Resource("/images/where/iphone/NearbyStopsForRouteArrow.png")
  public DataResource getNearbyStopsForRouteArrow();
 
  @Resource("/css/where/iphone.css")
  public CssDataResource getCss();
}
