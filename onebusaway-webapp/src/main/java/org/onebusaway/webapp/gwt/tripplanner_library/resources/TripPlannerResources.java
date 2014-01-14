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
package org.onebusaway.webapp.gwt.tripplanner_library.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.CssResource.Strict;

public interface TripPlannerResources extends ClientBundle {

  public static final TripPlannerResources INSTANCE = GWT.create(TripPlannerResources.class);

  @Source("BusTripTypeIcon.png")
  public DataResource getBusTripTypeIcon();

  @Source("WalkTripTypeIcon.png")
  public DataResource getWalkTripTypeIcon();

  @Source("Bubble-Left.png")
  public DataResource getBubbleLeft();

  @Source("Bubble-Right.png")
  public DataResource getBubbleRight();

  @Source("Bus14x14.png")
  public DataResource getBus14x14();

  @Source("TripPlannerResources.css")
  public TripPlannerCssResource getCss();
}
