package org.onebusaway.gtfs_diff.model;

import org.onebusaway.gtfs.model.AgencyAndId;

public class GtfsDifferences extends MatchCollection {

  private String _idA = "PREFIXA";

  private String _idB = "PREFIXB";

  public String getModelIdA() {
    return _idA;
  }

  public String getModelIdB() {
    return _idB;
  }

  @SuppressWarnings("unchecked")
  public <T> T translateId(T key, String prefix) {
    if (key instanceof String) {
      String id = (String) key;
      if (id.startsWith(prefix))
        return (T) id.substring(prefix.length());
    } else if (key instanceof AgencyAndId) {
      AgencyAndId id = (AgencyAndId) key;
      String agencyId = id.getAgencyId();
      if (agencyId.startsWith(prefix)) {
        agencyId = agencyId.substring(prefix.length());
        return (T) new AgencyAndId(agencyId, id.getId());
      }
    }
    return key;
  }

  public <T> T translateId(T key) {
    key = translateId(key, _idA);
    key = translateId(key, _idB);
    return key;
  }
}
