package org.onebusaway.gtfs.serialization;

import java.io.IOException;

public interface GtfsSource {
  public void close() throws IOException;
}
