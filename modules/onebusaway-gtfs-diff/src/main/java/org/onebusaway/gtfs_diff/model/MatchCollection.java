package org.onebusaway.gtfs_diff.model;

import java.util.ArrayList;
import java.util.List;

public class MatchCollection {

  private List<Match> matches = new ArrayList<Match>();

  private List<Mismatch> mismatches = new ArrayList<Mismatch>();

  public <T extends Match> T addMatch(T match) {
    matches.add(match);
    return match;
  }

  public List<Match> getMatches() {
    return matches;
  }

  public void addMismatch(Mismatch mismatch) {
    mismatches.add(mismatch);
  }

  public List<Mismatch> getMismatches() {
    return mismatches;
  }
}
