package org.onebusaway.tripplanner;

import org.onebusaway.common.impl.ObjectSerializationLibrary;

import java.io.File;
import java.io.IOException;

public class MemoryTest {

  private static Object _object;

  public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
    Thread.sleep(60 * 1000);
    System.out.println("in");
    File path = new File("/Users/bdferris/l10n/org.onebusaway/data/cache/TripPlannerGraph.obj");
    _object = ObjectSerializationLibrary.readObject(path);
    System.out.println("out");
    Thread.sleep(60 * 1000);
    System.out.println(_object);
  }
}
