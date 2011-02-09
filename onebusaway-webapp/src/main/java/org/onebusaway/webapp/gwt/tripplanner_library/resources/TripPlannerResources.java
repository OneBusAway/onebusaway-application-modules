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
