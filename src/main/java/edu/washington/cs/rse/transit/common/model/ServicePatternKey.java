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

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;

@Embeddable
public class ServicePatternKey implements Serializable, Comparable<ServicePatternKey> {

    private static final long serialVersionUID = 1L;

    @ManyToOne
    private ChangeDate changeDate;

    private int id;

    public ServicePatternKey() {

    }

    public ServicePatternKey(ChangeDate changeDate, int id) {
        this.changeDate = changeDate;
        this.id = id;
    }

    public ChangeDate getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(ChangeDate changeDate) {
        this.changeDate = changeDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int patternId) {
        this.id = patternId;
    }

    public int compareTo(ServicePatternKey o) {
        int rc = this.changeDate.compareTo(o.changeDate);
        if (rc == 0) {
            int id1 = this.id;
            int id2 = o.id;
            return id1 == id2 ? 0 : (id1 < id2 ? -1 : 1);
        }
        return rc;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ServicePatternKey))
            return false;

        ServicePatternKey spk = (ServicePatternKey) obj;
        return changeDate.equals(spk.changeDate) && this.id == spk.id;
    }

    @Override
    public int hashCode() {
        return changeDate.hashCode() * 7 + id * 13;
    }

    @Override
    public String toString() {
        return changeDate + " " + id;
    }

}
