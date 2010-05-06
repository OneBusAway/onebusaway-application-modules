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
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

import com.opensymphony.xwork2.conversion.annotations.TypeConversion;
import com.vividsolutions.jts.geom.Point;

@Entity
@Table(name = "transit_trans_link_shape_points")
public class TransLinkShapePoint extends EntityBean implements IHasLocation {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private TransLinkShapePointKey id;

    private Date dbModDate;

    @Type(type = "org.hibernatespatial.GeometryUserType")
    @Column(columnDefinition = "GEOMETRY")
    @Index(name = "location")
    private Point location;

    public TransLinkShapePointKey getId() {
        return id;
    }

    public void setId(TransLinkShapePointKey id) {
        this.id = id;
    }

    public Date getDbModDate() {
        return dbModDate;
    }

    @TypeConversion(converter = "edu.washington.cs.rse.transit.common.MetroKCTimestampConverter")
    public void setDbModDate(Date dbModDate) {
        this.dbModDate = dbModDate;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }
}
