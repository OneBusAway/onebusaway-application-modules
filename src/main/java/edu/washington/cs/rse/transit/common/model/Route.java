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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Index;

@Entity
@Table(name = "transit_routes")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class Route extends IdentityBean implements IHasId {

    private static final long serialVersionUID = 1L;

    @Id
    private int id;

    @Index(name = "number")
    private int number;

    @Column(length = 6)
    private String code;

    private Date dbModDate;

    private Date effectiveBeginDate;

    private Date effectiveEndDate;

    private int transitAgencyId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Date getDbModDate() {
        return dbModDate;
    }

    public void setDbModDate(Date dbModDate) {
        this.dbModDate = dbModDate;
    }

    public Date getEffectiveBeginDate() {
        return effectiveBeginDate;
    }

    public void setEffectiveBeginDate(Date effectiveBeginDate) {
        this.effectiveBeginDate = effectiveBeginDate;
    }

    public Date getEffectiveEndDate() {
        return effectiveEndDate;
    }

    public void setEffectiveEndDate(Date effectiveEndDate) {
        this.effectiveEndDate = effectiveEndDate;
    }

    public int getTransitAgencyId() {
        return transitAgencyId;
    }

    public void setTransitAgencyId(int transitAgencyId) {
        this.transitAgencyId = transitAgencyId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Route))
            return false;
        Route r = (Route) obj;
        return this.id == r.id;
    }

    @Override
    public int hashCode() {
        return this.id;
    }
}
