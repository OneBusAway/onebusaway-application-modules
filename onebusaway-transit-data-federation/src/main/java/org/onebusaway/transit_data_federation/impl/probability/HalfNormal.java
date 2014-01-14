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
