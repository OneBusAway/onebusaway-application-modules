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
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

@Embeddable
public class TPIPathKey implements Serializable {

    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    private TPI tpi;

    private int sequence;

    public TPIPathKey() {

    }

    public TPIPathKey(TPI tpi, int sequence) {
        this.tpi = tpi;
        this.sequence = sequence;
    }

    public TPI getTransLink() {
        return tpi;
    }

    public void setTransLink(TPI tpi) {
        this.tpi = tpi;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    /**
     * Special equals method that compares the TPI id directly so as not to
     * invoke TPI if it's a proxy
     * 
     * @param key
     * @return
     */
    public boolean equalsByProxy(TPIPathKey key) {
        return this.tpi.getId() == key.tpi.getId() && this.sequence == key.sequence;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof TPIPathKey))
            return false;
        TPIPathKey key = (TPIPathKey) obj;
        return this.tpi.equals(key.tpi) && this.sequence == key.sequence;
    }

    @Override
    public int hashCode() {
        return this.tpi.hashCode() + 3 * this.sequence;
    }
}
