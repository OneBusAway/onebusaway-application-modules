package org.onebusaway.tripplanner.web.common.client.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.libideas.resources.client.CssResource;
import com.google.gwt.libideas.resources.client.DataResource;
import com.google.gwt.libideas.resources.client.ImmutableResourceBundle;

public interface TripPlannerResources extends ImmutableResourceBundle {

  public static final TripPlannerResources INSTANCE = GWT.create(TripPlannerResources.class);

  @Resource("BusTripTypeIcon.png")
  public DataResource getBusTripTypeIcon();

  @Resource("WalkTripTypeIcon.png")
  public DataResource getWalkTripTypeIcon();

  @Resource("Bubble-Left.png")
  public DataResource getBubbleLeft();

  @Resource("Bubble-Right.png")
  public DataResource getBubbleRight();

  @Resource("Bus14x14.png")
  public DataResource getBus14x14();
  
  @Resource("TripPlannerResources.css")
  public CssResource getCSS();
}
