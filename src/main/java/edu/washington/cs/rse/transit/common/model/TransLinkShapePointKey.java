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
public class TransLinkShapePointKey implements Serializable {

    private static final long serialVersionUID = 1L;

    @ManyToOne(fetch=FetchType.LAZY)
    private TransLink transLink;

    private int sequence;

    public TransLinkShapePointKey() {

    }

    public TransLinkShapePointKey(TransLink link, int sequence) {
        this.transLink = link;
        this.sequence = sequence;
    }

    public TransLink getTransLink() {
        return transLink;
    }

    public void setTransLink(TransLink transLink) {
        this.transLink = transLink;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof TransLinkShapePointKey))
            return false;
        TransLinkShapePointKey key = (TransLinkShapePointKey) obj;
        return this.transLink.equals(key.transLink) && this.sequence == key.sequence;
    }

    @Override
    public int hashCode() {
        return this.transLink.hashCode() + 3 * this.sequence;
    }
}
