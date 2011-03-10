package org.onebusaway.transit_data_federation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.onebusaway.collections.Counter;
import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.collections.tuple.Tuples;

public class IntervalsExperiment {
  public static void main(String[] args) throws IOException {

    BufferedReader reader = new BufferedReader(new FileReader("/tmp/intervals"));
    String line = null;

    int total = 0;
    int overlapBefore = 0;
    int overlapAfter = 0;
    Counter<Pair<Integer>> keys = new Counter<Pair<Integer>>();

    double step = 4 * 60 * 60;

    while ((line = reader.readLine()) != null) {
      String[] tokens = line.split(",");
      int a = Integer.parseInt(tokens[0]);
      int b = Integer.parseInt(tokens[1]);
      if (b - (24 * 60 * 60) > a)
        overlapBefore++;
      a = (int) Math.floor(a / step);
      b = (int) Math.ceil(b / step);
      keys.increment(Tuples.pair(a, b));
      total++;
      if (b - (24 * 60 * 60) > a)
        overlapAfter++;
    }

    System.out.println("total=" + total);
    System.out.println("keys=" + keys.size());
    System.out.println("overlapBefore=" + overlapBefore);
    System.out.println("overlapAfter=" + overlapAfter);

  }
}
