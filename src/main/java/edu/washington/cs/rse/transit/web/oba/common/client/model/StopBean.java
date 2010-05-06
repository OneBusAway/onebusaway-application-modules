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
package edu.washington.cs.rse.transit.web.oba.common.client.model;

public class StopBean extends ApplicationBean {

    private static final long serialVersionUID = 1L;

    private int _id;

    private double _lat;

    private double _lon;

    private String _direction;

    private String _mainStreet;

    private String _crossStreet;

    public StopBean() {

    }

    public int getId() {
        return _id;
    }

    public void setId(int id) {
        _id = id;
    }

    public double getLat() {
        return _lat;
    }

    public void setLat(double lat) {
        _lat = lat;
    }

    public double getLon() {
        return _lon;
    }

    public void setLon(double lon) {
        _lon = lon;
    }

    public String getDirection() {
        return _direction;
    }

    public void setDirection(String direction) {
        _direction = direction;
    }

    public void setMainStreet(String mainStreet) {
        _mainStreet = mainStreet;
    }

    public String getMainStreet() {
        return _mainStreet;
    }

    public void setCrossStreet(String crossStreet) {
        _crossStreet = crossStreet;
    }

    public String getCrossStreet() {
        return _crossStreet;
    }

    /***************************************************************************
     * {@link Object} Interface
     **************************************************************************/

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof StopBean))
            return false;
        StopBean stop = (StopBean) obj;
        return _id == stop._id;
    }

    @Override
    public int hashCode() {
        return _id;
    }
}
