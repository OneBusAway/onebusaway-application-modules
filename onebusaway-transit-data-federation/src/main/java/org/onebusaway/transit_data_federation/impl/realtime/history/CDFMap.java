package org.onebusaway.transit_data_federation.impl.realtime.history;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CDFMap<T> {

  private double _cumulativeProb = 0.0;

  private double[] _cumulativeProbabilities = new double[2];

  private List<T> _entries = new ArrayList<T>();

  public void put(double prob, T object) {

    if (_cumulativeProbabilities.length <= _entries.size()) {
      int c = _cumulativeProbabilities.length << 1;
      double[] cumulativeProbabilities = new double[c];
      System.arraycopy(_cumulativeProbabilities, 0, cumulativeProbabilities, 0,
          _cumulativeProbabilities.length);
      _cumulativeProbabilities = cumulativeProbabilities;
    }

    _cumulativeProb += prob;
    _cumulativeProbabilities[_entries.size()] = _cumulativeProb;
    _entries.add(object);

  }

  public T sample() {

    if (_entries.isEmpty())
      throw new IllegalStateException("No entries in the CDF");

    if (_cumulativeProb == 0.0)
      throw new IllegalStateException("No cumulative probability in CDF");

    double probability = Math.random() * _cumulativeProb;

    int index = Arrays.binarySearch(_cumulativeProbabilities, 0,
        _entries.size(), probability);
    if (index < 0)
      index = -(index + 1);
    return _entries.get(index);
  }

  public List<T> sample(int samples) {

    if (_entries.isEmpty())
      throw new IllegalStateException("No entries in the CDF map");

    if (_cumulativeProb == 0.0)
      throw new IllegalStateException("No cumulative probability in CDF");

    if (samples == 0)
      return Collections.emptyList();

    List<T> sampled = new ArrayList<T>(samples);

    double step = _cumulativeProb / samples;
    int index = 0;

    for (double p = 0; p < _cumulativeProb && sampled.size() < samples; p += step) {
      while (_cumulativeProbabilities[index] <= p)
        index++;
      sampled.add(_entries.get(index));
    }

    return sampled;
  }

  public boolean isEmpty() {
    return _entries.isEmpty();
  }

  public boolean hasProbability() {
    return _cumulativeProb > 0.0;
  }

  public boolean canSample() {
    return !_entries.isEmpty() && _cumulativeProb > 0.0;
  }

  public int size() {
    return _entries.size();
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    b.append("{");
    for (int i = 0; i < _entries.size(); i++) {
      if (i > 0)
        b.append(", ");
      b.append(_cumulativeProbabilities[i]);
      b.append("=");
      b.append(_entries.get(i));
    }
    b.append("}");
    return b.toString();
  }
}
