package org.onebusaway.kcmetro_tcip;

import edu.washington.cs.rse.collections.coverage.CoverageMap;
import edu.washington.cs.rse.collections.coverage.ITimeInterval;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Go {
  public static void main(String[] args) throws NumberFormatException,
      IOException {
    File root = new File("/Users/bdferris/oba/trunk/data/kcm/avl/09-22");
    File[] logs = root.listFiles(new FileFilter() {
      public boolean accept(File pathname) {
        return pathname.getName().endsWith(".log");
      }
    });

    Map<String, CoverageMap> coverageByTripIds = new HashMap<String, CoverageMap>();

    for (File log : logs) {
      BufferedReader reader = new BufferedReader(new FileReader(log));
      String line = null;

      while ((line = reader.readLine()) != null) {
        String[] tokens = line.split(" ");
        String tripId = tokens[2];
        long time = Long.parseLong(tokens[8]);
        CoverageMap coverageMap = coverageByTripIds.get(tripId);
        if (coverageMap == null) {
          coverageMap = new CoverageMap(10 * 60 * 1000);
          coverageByTripIds.put(tripId, coverageMap);
        }
        coverageMap.addValue(time);
      }
    }

    double max = 0;
    String maxId = null;

    for (Map.Entry<String, CoverageMap> entry : coverageByTripIds.entrySet()) {
      String tripId = entry.getKey();
      CoverageMap coverage = entry.getValue();
      for (ITimeInterval interval : coverage.getIntervals()) {
        if (interval.getLength() > max) {
          max = interval.getLength();
          maxId = tripId;
        }
        max = Math.max(interval.getLength(), max);
      }
    }

    System.out.println((int) (max / 1000));
    System.out.println(maxId);
  }
}
