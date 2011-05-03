package org.onebusaway.transit_data_federation.impl.probability;

import cern.jet.stat.Probability;

public class HalfNormal {

  private static final double SQRT_TWO = Math.sqrt(2.0);

  private final double _sigma;

  public HalfNormal(double sigma) {
    _sigma = sigma;
  }

  public double cdf(double x) {
    if (x < 0)
      return 0.0;
    return Probability.errorFunction(x / (SQRT_TWO * _sigma));
  }
}
