package org.onebusaway.kcmetro2gtfs.impl;

import org.onebusaway.kcmetro2gtfs.model.MetroKCStop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DirectReplacementStopNameStrategyFactory {

  public StopNameStrategy create(File names) throws IOException {
    DirectReplacementStopNameStrategy strategy = new DirectReplacementStopNameStrategy();

    BufferedReader reader = new BufferedReader(new FileReader(names));
    String line = null;
    int lineNumber = 1;

    while ((line = reader.readLine()) != null) {
      int index = line.indexOf(',');
      if (index == -1)
        throw new IllegalArgumentException("invalid line [#" + lineNumber
            + "]: " + line);
      String key = line.substring(0, index);
      String value = line.substring(index + 1);
      try {
        int stopId = Integer.parseInt(key);
        strategy.addName(stopId, value);
      } catch (NumberFormatException ex) {
        throw new IllegalArgumentException("invalid stop number at line #"
            + lineNumber + ": " + key);
      }
      lineNumber++;
    }

    reader.close();

    return strategy;
  }

  private class DirectReplacementStopNameStrategy implements StopNameStrategy {

    private Map<Integer, String> _stopNamesByStopId = new HashMap<Integer, String>();

    public void addName(Integer stopId, String name) {
      _stopNamesByStopId.put(stopId, name);
    }

    public boolean hasNameForStop(MetroKCStop stop) {
      return _stopNamesByStopId.containsKey(stop.getId());
    }

    public String getNameForStop(MetroKCStop stop) {
      return _stopNamesByStopId.get(stop.getId());
    }
  }
}
