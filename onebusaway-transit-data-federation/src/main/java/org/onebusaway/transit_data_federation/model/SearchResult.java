/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.transit_data_federation.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A search results captures a list of results along with the scores for each
 * result.
 * 
 * @author bdferris
 * 
 * @param <T> the result type
 */
public class SearchResult<T> {

  private List<T> _results;

  private double[] _scores;

  public SearchResult() {
    _results = Collections.emptyList();
    _scores = new double[] {};
  }

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

  public List<T> getResultsByTopScore() {
    if (_scores == null || _scores.length == 0) {
      return _results;
    }
    // we do a trivial search here expecting elements to be small
    List<ScoredResult<T>> sortedElements = new ArrayList<ScoredResult<T>>();
    for (int i = 0; i < _results.size(); i++) {
      sortedElements.add(new ScoredResult<>(_results.get(i), _scores[i]));
    }
    Collections.sort(sortedElements, new TopScoreComparator());
    List<T> sortedIds = new ArrayList<T>();
    for (ScoredResult<T> sr : sortedElements) {
      sortedIds.add(sr.getResult());
    }
    return sortedIds;
  }

  public double getScore(int index) {
    return _scores[index];
  }

  /**
   * Associate a score with T so it can be sorted
   * @param <T> the element that has a score
   */
  public class ScoredResult<T> {
    private T _result;
    private double _score;

    public ScoredResult(T aresult, double ascore) {
      this._result = aresult;
      this._score = ascore;
    }

    public T getResult() { return _result; }
    public double getScore() { return _score; }
  }

  /**
   * Sort based on highest score.
   * @param <T>
   */
  public class TopScoreComparator<T> implements Comparator<T> {
    public int compare(T a, T b) {
      ScoredResult<T> sra = (ScoredResult<T>) a;
      ScoredResult<T> srb = (ScoredResult<T>) b;
      return Double.compare(sra.getScore(), srb.getScore());
    }
  }

}
