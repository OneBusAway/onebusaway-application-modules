package org.onebusaway.transit_data.model.tripplanning;

/**
 * Why is this a string and not an enum? To allow for the future addition of new
 * modes without a recompile. These string keys are offered mostly for
 * convenience.
 * 
 * @author bdferris
 */
public class Modes {

  public static final String WALK = "walk";

  public static final String TRANSIT = "transit";
}
