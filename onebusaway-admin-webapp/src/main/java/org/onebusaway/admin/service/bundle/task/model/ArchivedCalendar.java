/**
 * Copyright (C) 2016 Cambridge Systematics, Inc.
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
package org.onebusaway.admin.service.bundle.task.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.calendar.ServiceDate;

public class ArchivedCalendar implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;

    private AgencyAndId serviceId;

    private int monday;

    private int tuesday;

    private int wednesday;

    private int thursday;

    private int friday;

    private int saturday;

    private int sunday;

    private ServiceDate startDate;

    private ServiceDate endDate;
    
    private Integer gtfsBundleInfoId;

    public int getId() {
      return id;
    }

    public void setId(int id) {
      this.id = id;
    }

    public AgencyAndId getServiceId() {
      return serviceId;
    }

    public void setServiceId(AgencyAndId serviceId) {
      this.serviceId = serviceId;
    }

    public int getMonday() {
      return monday;
    }

    public void setMonday(int monday) {
      this.monday = monday;
    }

    public int getTuesday() {
      return tuesday;
    }

    public void setTuesday(int tuesday) {
      this.tuesday = tuesday;
    }

    public int getWednesday() {
      return wednesday;
    }

    public void setWednesday(int wednesday) {
      this.wednesday = wednesday;
    }

    public int getThursday() {
      return thursday;
    }

    public void setThursday(int thursday) {
      this.thursday = thursday;
    }

    public int getFriday() {
      return friday;
    }

    public void setFriday(int friday) {
      this.friday = friday;
    }

    public int getSaturday() {
      return saturday;
    }

    public void setSaturday(int saturday) {
      this.saturday = saturday;
    }

    public int getSunday() {
      return sunday;
    }

    public void setSunday(int sunday) {
      this.sunday = sunday;
    }

    public ServiceDate getStartDate() {
      return startDate;
    }

    public void setStartDate(ServiceDate startDate) {
      this.startDate = startDate;
    }

    public ServiceDate getEndDate() {
      return endDate;
    }

    public void setEndDate(ServiceDate endDate) {
      this.endDate = endDate;
    }

    public Integer getGtfsBundleInfoId() {
      return gtfsBundleInfoId;
    }

    public void setGtfsBundleInfoId(Integer gtfsBundleInfoId) {
      this.gtfsBundleInfoId = gtfsBundleInfoId;
    }

    public ArchivedCalendar() {

    }

}
