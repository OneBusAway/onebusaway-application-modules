package org.onebusaway.transit_data_federation.impl.probability;

public class DeviationModel {

  private final HalfNormal _dist;

  public DeviationModel(double sigma) {
    _dist = new HalfNormal(sigma);
  }

  public double probability(double deviation) {
    return 1 - _dist.cdf(deviation);
  }
}
