package org.onebusaway.transit_data_federation.model;

import java.util.List;

public class SearchResult<T> {

  private List<T> _results;

  private double[] _scores;

  public SearchResult(List<T> results, double[] scores) {
    _results = results;
    _scores = scores;
  }

  public int size() {
    return _results.size();
  }

  public T getResult(int index) {
    return _results.get(index);
  }
  
  public List<T> getResults() {
    return _results;
  }

  public double getScore(int index) {
    return _scores[index];
  }
}
