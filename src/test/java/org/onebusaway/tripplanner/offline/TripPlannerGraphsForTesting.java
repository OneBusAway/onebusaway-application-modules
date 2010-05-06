package org.onebusaway.tripplanner.offline;

import org.onebusaway.tripplanner.services.TripPlannerGraph;

public class TripPlannerGraphsForTesting {

  public static TripPlannerGraph createGraphA() {

    MockTripPlannerGraphFactory factory = new MockTripPlannerGraphFactory();

    double di = 100;
    double dc = 15;

    for (int i = 0; i < 3; i++) {
      factory.addStop("E" + i + "0", i * 5280 + di, -dc);
      factory.addStop("E" + i + "2", i * 5280 + di, 2 * 5280 - dc);

      factory.addStop("W" + i + "0", i * 5280 - di, dc);
      factory.addStop("W" + i + "2", i * 5280 - di, 2 * 5280 + dc);

      factory.addStop("N0" + i, dc, i * 5280 + di);
      factory.addStop("N2" + i, 2 * 5280 + dc, i * 5280 + di);

      factory.addStop("S0" + i, -dc, i * 5280 - di);
      factory.addStop("S2" + i, 2 * 5280 - dc, i * 5280 - di);
    }

    factory.addTransferManhattanDistance("N00", "W00", "S00", "E00");
    factory.addTransferManhattanDistance("W10", "E10");
    factory.addTransferManhattanDistance("N20", "W20", "S20", "E20");
    factory.addTransferManhattanDistance("N21", "S21");
    factory.addTransferManhattanDistance("N22", "W22", "S22", "E22");
    factory.addTransferManhattanDistance("W12", "E12");
    factory.addTransferManhattanDistance("N02", "W02", "S02", "E02");
    factory.addTransferManhattanDistance("N01", "S01");

    factory.addTrip("T00", "A", "R1", new String[] {"E00", "E10", "E20"}, new int[] {mins(0), mins(3), mins(6)});
    factory.addTrip("T01", "A", "R2", new String[] {"N20", "N21", "N22"}, new int[] {mins(16), mins(19), mins(22)});
    factory.addTrip("T03", "A", "R2", new String[] {"S22", "S21", "S20"}, new int[] {mins(25), mins(29), mins(32)});
    factory.addTrip("T04", "A", "R1", new String[] {"W20", "W10", "W00"}, new int[] {mins(42), mins(45), mins(49)});

    factory.addBlock("T00", "T01", "T03", "T04");

    factory.addTrip("T10", "A", "R3", new String[] {"N00", "N01", "E02", "E12", "E22"}, new int[] {
        mins(4), mins(8), mins(12), mins(16), mins(20)});
    factory.addTrip("T11", "A", "R4", new String[] {"W22", "W12", "S02", "S01", "S00"}, new int[] {
        mins(28), mins(33), mins(38), mins(43), mins(48)});

    factory.addBlock("T10", "T11");

    return factory.getGraph();
  }

  private static int mins(double m) {
    return (int) (m * 60);
  }
}
