package org.onebusaway.transit_data_federation.bundle.model;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.transit_data_federation.bundle.FederatedTransitDataBundleCreator;

/**
 * A typed collection of {@link GtfsBundle} objects.
 * 
 * @author bdferris
 * @see GtfsBundle
 * @see FederatedTransitDataBundleCreator
 */
public class GtfsBundles {
  private List<GtfsBundle> bundles = new ArrayList<GtfsBundle>();

  public List<GtfsBundle> getBundles() {
    return bundles;
  }

  public void setBundles(List<GtfsBundle> bundles) {
    this.bundles = bundles;
  }
}
