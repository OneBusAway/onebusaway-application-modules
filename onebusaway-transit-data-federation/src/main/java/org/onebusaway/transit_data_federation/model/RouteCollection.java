package org.onebusaway.transit_data_federation.model;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.IndexColumn;

import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

/**
 * Why RouteCollection? Why not keep a 1-to-1 mapping between route short names
 * and {@link Route} entities? The issue is that some GTFS include multiple
 * Route entities with the same short name. These are often different versions
 * of the same route (local vs express).
 * 
 * @author bdferris
 * 
 */
@Entity
@Table(name = "where_route_collections")
@AccessType("field")
@org.hibernate.annotations.Entity(mutable = false)
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class RouteCollection {

  @EmbeddedId
  @AttributeOverrides( {
      @AttributeOverride(name = "agencyId", column = @Column(name = "agencyId", length = 50)),
      @AttributeOverride(name = "id", column = @Column(name = "id"))})
  @AccessType("property")
  private AgencyAndId id;

  private String shortName;

  private String longName;

  private String description;

  private int type;

  private String url;

  private String color;

  private String textColor;

  @ManyToMany(fetch = FetchType.LAZY)
  @IndexColumn(name = "sequence", base = 1)
  private List<Route> routes;

  public AgencyAndId getId() {
    return id;
  }

  public void setId(AgencyAndId id) {
    this.id = id;
  }

  public String getShortName() {
    return shortName;
  }

  public void setShortName(String shortName) {
    this.shortName = shortName;
  }

  public String getLongName() {
    return longName;
  }

  public void setLongName(String longName) {
    this.longName = longName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public String getTextColor() {
    return textColor;
  }

  public void setTextColor(String textColor) {
    this.textColor = textColor;
  }

  public List<Route> getRoutes() {
    return routes;
  }

  public void setRoutes(List<Route> routes) {
    this.routes = routes;
  }
}
