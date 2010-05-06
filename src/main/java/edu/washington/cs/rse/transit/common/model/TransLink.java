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
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.AccessType;

import com.opensymphony.xwork2.conversion.annotations.TypeConversion;

@Entity
@Table(name = "transit_trans_links")
@AccessType("field")
public class TransLink extends IdentityBean {

    private static final long serialVersionUID = 1L;

    /**
     * We make the id a "property" access element such that a call to
     * {@link #getId()} when the object is proxied does not result in proxy
     * insantiation
     */
    @Id
    @AccessType("property")
    private int id;

    private Date dbModDate;

    @ManyToOne(fetch = FetchType.LAZY)
    private StreetName streetName;

    @ManyToOne(fetch = FetchType.LAZY)
    private TransNode transNodeFrom;

    @ManyToOne(fetch = FetchType.LAZY)
    private TransNode transNodeTo;

    private int addrLeftFrom;

    private int addrLeftTo;

    private int addrRightFrom;

    private int addrRightTo;

    @Column(length = 1)
    private String countyClass;

    private double linkLen;

    private int zipLeft;

    private int zipRight;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDbModDate() {
        return dbModDate;
    }

    @TypeConversion(converter = "edu.washington.cs.rse.transit.common.MetroKCTimestampConverter")
    public void setDbModDate(Date dbModDate) {
        this.dbModDate = dbModDate;
    }

    public StreetName getStreetName() {
        return streetName;
    }

    public void setStreetName(StreetName streetName) {
        this.streetName = streetName;
    }

    public TransNode getTransNodeFrom() {
        return transNodeFrom;
    }

    public void setTransNodeFrom(TransNode transNodeFrom) {
        this.transNodeFrom = transNodeFrom;
    }

    public TransNode getTransNodeTo() {
        return transNodeTo;
    }

    public void setTransNodeTo(TransNode transNodeTo) {
        this.transNodeTo = transNodeTo;
    }

    public int getAddrLeftFrom() {
        return addrLeftFrom;
    }

    public void setAddrLeftFrom(int addrLeftFrom) {
        this.addrLeftFrom = addrLeftFrom;
    }

    public int getAddrLeftTo() {
        return addrLeftTo;
    }

    public void setAddrLeftTo(int addrLeftTo) {
        this.addrLeftTo = addrLeftTo;
    }

    public int getAddrRightFrom() {
        return addrRightFrom;
    }

    public void setAddrRightFrom(int addrRightFrom) {
        this.addrRightFrom = addrRightFrom;
    }

    public int getAddrRightTo() {
        return addrRightTo;
    }

    public void setAddrRightTo(int addrRightTo) {
        this.addrRightTo = addrRightTo;
    }

    public String getCountyClass() {
        return countyClass;
    }

    public void setCountyClass(String countyClass) {
        this.countyClass = countyClass;
    }

    public double getLinkLen() {
        return linkLen;
    }

    public void setLinkLen(double linkLen) {
        this.linkLen = linkLen;
    }

    public int getZipLeft() {
        return zipLeft;
    }

    public void setZipLeft(int zipLeft) {
        this.zipLeft = zipLeft;
    }

    public int getZipRight() {
        return zipRight;
    }

    public void setZipRight(int zipRight) {
        this.zipRight = zipRight;
    }

    @Override
    public String toString() {
        return "TransLink(id=" + getId() + ")";
    }
}
