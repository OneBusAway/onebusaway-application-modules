package org.onebusaway.transit_data_federation.impl.otp.graph.tp;

public interface HasPathStateVertex {

  public TPState getPathState();

  public boolean isDeparture();
}
