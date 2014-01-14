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

import java.util.Collections;
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

  public double getScore(int index) {
    return _scores[index];
  }
}
