package org.onebusaway.tcip.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("stoppoint")
public class CPTStoppointIden {

  @XStreamAlias("stoppoint-id")
  private String stoppointId;

  @XStreamAlias("agency-id")
  private String agencyId;

  private String name;

  public CPTStoppointIden() {

  }

  public CPTStoppointIden(String agencyId, String stoppointId) {
    this.agencyId = agencyId;
    this.stoppointId = stoppointId;
  }

  public String getStoppointId() {
    return stoppointId;
  }

  public void setStoppointId(String stoppointId) {
    this.stoppointId = stoppointId;
  }

  public String getAgencyId() {
    return agencyId;
  }

  public void setAgencyId(String agencyId) {
    this.agencyId = agencyId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((agencyId == null) ? 0 : agencyId.hashCode());
    result = prime * result
        + ((stoppointId == null) ? 0 : stoppointId.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    CPTStoppointIden other = (CPTStoppointIden) obj;
    if (agencyId == null) {
      if (other.agencyId != null)
        return false;
    } else if (!agencyId.equals(other.agencyId))
      return false;
    if (stoppointId == null) {
      if (other.stoppointId != null)
        return false;
    } else if (!stoppointId.equals(other.stoppointId))
      return false;
    return true;
  }

}
