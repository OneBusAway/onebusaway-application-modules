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
package org.onebusaway.transit_data_federation.impl.service_alerts;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.transit_data_federation.services.service_alerts.ServiceAlerts.ServiceAlert;
/**
 * A Service Alert record is a database-serializable record that captures the
 * real-time service alerts from different agencies on a particular route.
 * The record includes service alert data object with service alert id.
 * 
 * This class is meant for internal use.
 * 
 * @author ckhasnis 
 */
@Entity
@Table(name = "transit_data_service_alerts_records")
@org.hibernate.annotations.Table(appliesTo = "transit_data_service_alerts_records", indexes = {
	    @Index(name = "service_alert_idx", columnNames = {
	    		"id","service_alert_id", "service_alert", "last_modified"})})
	@org.hibernate.annotations.Entity(mutable = true)
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)

public class ServiceAlertRecord {
	

  public ServiceAlertRecord(String serviceAlertId, ServiceAlert serviceAlert, AgencyAndId agencyId, long lastModified) {
    super();
    this.serviceAlertId = serviceAlertId;
    this.serviceAlert = serviceAlert;
    this.agencyId = agencyId;
    this.lastModified = lastModified;
  }

  public ServiceAlertRecord(String serviceAlertId, ServiceAlert serviceAlert, AgencyAndId agencyId) {
		super();
		this.serviceAlertId = serviceAlertId;
		this.serviceAlert = serviceAlert;
		this.agencyId = agencyId;
		this.lastModified = System.currentTimeMillis();
	}
	
	public ServiceAlertRecord() {		
		this.serviceAlertId = null;
		this.serviceAlert = null;
		this.agencyId = null;
		this.lastModified = System.currentTimeMillis();
	}

	@Id
	@GeneratedValue
	private final int id = 0;
	
	@Column(nullable = false, name="agency_id", length = 255)
	@Lob
	private AgencyAndId agencyId;
	
	@Column(nullable = false, name="service_alert_id", length = 255)
	private String serviceAlertId;
	
	@Column(nullable = false, name="service_alert")
	@Lob
	private ServiceAlert serviceAlert;	

	// the column is nullable for backwards comparability, but it will return 0 internally
	@Column(nullable = true, name="last_modified")
	private Long lastModified = 0l;
	
	public String getServiceAlertId() {
		return serviceAlertId;
	}

	public void setServiceAlertId(String serviceAlertId) {
	  if (!equals(this.serviceAlertId, serviceAlertId)) updateLastModified();
		this.serviceAlertId = serviceAlertId;
	}

  public ServiceAlert getServiceAlert() {
		return serviceAlert;
	}

	public void setServiceAlert(ServiceAlert serviceAlert) {
	  if (!equals(this.serviceAlert, serviceAlert)) updateLastModified();
		this.serviceAlert = serviceAlert;
	}

  /**
	 * @return the agencyId
	 */
	public AgencyAndId getAgencyId() {
		return agencyId;
	}

	/**
	 * @param agencyId the agencyId to set
	 */
	public void setAgencyId(AgencyAndId agencyId) {
	  if (!equals(this.agencyId, agencyId)) updateLastModified();
		this.agencyId = agencyId;
	}

  public int getId() {
		return id;
	}

	public void setLastModified(long lastModified) {
	  this.lastModified = lastModified;
	}
	
  public long getLastModified() {
    if (lastModified == null) return 0;
    return lastModified;
  }

  private void updateLastModified() {
    lastModified = System.currentTimeMillis();
  }

  private boolean equals(String s1, String s2) {
    if (s1 == null && s1 != s2)
      return false;
    return s1.equals(s2);
  }

  private boolean equals(ServiceAlert s1, ServiceAlert s2) {
    if (s1 == null && s1 != s2)
      return false;
    return s1.equals(s2);
  }

  private boolean equals(AgencyAndId a1, AgencyAndId a2) {
    if (a1 == null && a1 != a2)
      return false;
    return a1.equals(a2);
  }

  
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ServiceAlertRecord [id=" + id + ", agencyId=" + agencyId
				+ ", serviceAlertId=" + serviceAlertId + ", serviceAlert="
				+ serviceAlert + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((agencyId == null) ? 0 : agencyId.hashCode());
		result = prime * result + id;
		result = prime * result
				+ ((serviceAlert == null) ? 0 : serviceAlert.hashCode());
		result = prime * result
				+ ((serviceAlertId == null) ? 0 : serviceAlertId.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServiceAlertRecord other = (ServiceAlertRecord) obj;
		if (agencyId == null) {
			if (other.agencyId != null)
				return false;
		} else if (!agencyId.equals(other.agencyId))
			return false;
		if (id != other.id)
			return false;
		if (serviceAlert == null) {
			if (other.serviceAlert != null)
				return false;
		} else if (!serviceAlert.equals(other.serviceAlert))
			return false;
		if (serviceAlertId == null) {
			if (other.serviceAlertId != null)
				return false;
		} else if (!serviceAlertId.equals(other.serviceAlertId))
			return false;
		return true;
	}	
	
}
