/*
 * Copyright 2008 Brian Ferris
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.washington.cs.rse.transit.common.model;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "transit_service_patterns")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class ServicePattern extends EntityBean {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private ServicePatternKey id;

    private Date dbModDate;

    @Column(length = 20)
    private String direction;

    @ManyToOne
    private Route route;

    @Column(length = 6)
    @Index(name = "serviceTypeIndex")
    private String serviceType;

    @Index(name = "schedulePatternId")
    private int schedulePatternId;

    @Column(length = 1)
    private String patternType;

    /***************************************************************************
     * Additional Fields Added By Me
     **************************************************************************/

    @Column(length = 60)
    private String generalDestination;

    @Column(length = 120)
    private String specificDestination;

    @Column(length = 120)
    private String timepointDestination;

    public ServicePatternKey getId() {
        return this.id;
    }

    public void setId(ServicePatternKey id) {
        this.id = id;
    }

    public Date getDbModDate() {
        return dbModDate;
    }

    public void setDbModDate(Date dbModDate) {
        this.dbModDate = dbModDate;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public int getSchedulePatternId() {
        return schedulePatternId;
    }

    public void setSchedulePatternId(int schedulePatternId) {
        this.schedulePatternId = schedulePatternId;
    }

    public String getPatternType() {
        return patternType;
    }

    public void setPatternType(String patternType) {
        this.patternType = patternType;
    }

    public String getGeneralDestination() {
        return generalDestination;
    }

    public void setGeneralDestination(String destination) {
        this.generalDestination = destination;
    }

    public String getSpecificDestination() {
        return specificDestination;
    }

    public void setSpecificDestination(String specificDestination) {
        this.specificDestination = specificDestination;
    }

    public String getTimepointDestination() {
        return timepointDestination;
    }

    public void setTimepointDestination(String timepointDestination) {
        this.timepointDestination = timepointDestination;
    }

    /***************************************************************************
     * 
     **************************************************************************/

    public boolean isExpress() {
        return this.serviceType != null && this.serviceType.equals("E");
    }

    /***************************************************************************
     * 
     **************************************************************************/

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ServicePattern))
            return false;
        ServicePattern sp = (ServicePattern) obj;
        return getId().equals(sp.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public String toString() {
      return "ServicePattern(id=" + this.id + ")";
    }
}
