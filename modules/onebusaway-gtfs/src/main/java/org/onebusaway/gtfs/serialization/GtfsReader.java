package org.onebusaway.gtfs.serialization;

import org.onebusaway.gtfs.csv.CsvEntityContext;
import org.onebusaway.gtfs.csv.CsvEntityReader;
import org.onebusaway.gtfs.csv.EntityHandler;
import org.onebusaway.gtfs.csv.schema.DefaultEntitySchemaFactory;
import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.FareAttribute;
import org.onebusaway.gtfs.model.FareRule;
import org.onebusaway.gtfs.model.Frequency;
import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Transfer;
import org.onebusaway.gtfs.model.Trip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GtfsReader extends CsvEntityReader {

  private final Logger _log = LoggerFactory.getLogger(GtfsReader.class);
  
  public static final String KEY_CONTEXT = GtfsReader.class.getName()
      + ".context";

  private List<Class<?>> _entityClasses = new ArrayList<Class<?>>();

  private GtfsReaderContextImpl _context = new GtfsReaderContextImpl();

  private GtfsEntityStore _entityStore = new GtfsDaoImpl();

  private List<Agency> _agencies = new ArrayList<Agency>();

  private Map<Class<?>, Map<String, String>> _agencyIdsByEntityClassAndId = new HashMap<Class<?>, Map<String, String>>();

  private String _defaultAgencyId;

  private Map<String, String> _agencyIdMapping = new HashMap<String, String>();

  public GtfsReader() {

    _entityClasses.add(Agency.class);
    _entityClasses.add(ShapePoint.class);
    _entityClasses.add(Route.class);
    _entityClasses.add(Stop.class);
    _entityClasses.add(Trip.class);
    _entityClasses.add(StopTime.class);
    _entityClasses.add(ServiceCalendar.class);
    _entityClasses.add(ServiceCalendarDate.class);
    _entityClasses.add(FareAttribute.class);
    _entityClasses.add(FareRule.class);
    _entityClasses.add(Frequency.class);
    _entityClasses.add(Transfer.class);

    setTrimValues(true);

    /**
     * Prep the Entity Schema Factories
     */
    DefaultEntitySchemaFactory schemaFactory = createEntitySchemaFactory();
    setEntitySchemaFactory(schemaFactory);

    CsvEntityContext ctx = getContext();
    ctx.put(KEY_CONTEXT, _context);

    addEntityHandler(new EntityHandlerImpl());
  }

  public List<Agency> getAgencies() {
    return _agencies;
  }

  public void setAgencies(List<Agency> agencies) {
    _agencies = new ArrayList<Agency>(agencies);
  }

  public void setDefaultAgencyId(String feedId) {
    _defaultAgencyId = feedId;
  }

  public void addAgencyIdMapping(String fromAgencyId, String toAgencyId) {
    _agencyIdMapping.put(fromAgencyId, toAgencyId);
  }

  public GtfsEntityStore getEntityStore() {
    return _entityStore;
  }

  public void setEntityStore(GtfsEntityStore entityStore) {
    _entityStore = entityStore;
  }

  public List<Class<?>> getEntityClasses() {
    return _entityClasses;
  }

  public void run() throws IOException {

    List<Class<?>> classes = getEntityClasses();

    for (Class<?> entityClass : classes) {
      _log.info("reading entities: " + entityClass.getName());
      readEntities(entityClass);
      _entityStore.flush();
    }

    _entityStore.flush();
  }

  /****
   * Protected Methods
   ****/

  protected DefaultEntitySchemaFactory createEntitySchemaFactory() {
    return GtfsEntitySchemaFactory.createEntitySchemaFactory();
  }

  protected Object getEntity(Class<?> entityClass, Serializable id) {
    if (entityClass == null)
      throw new IllegalArgumentException("entity class must not be null");
    if (id == null)
      throw new IllegalArgumentException("entity id must not be null");
    return _entityStore.load(entityClass, id);
  }

  protected String getDefaultAgencyId() {
    if (_defaultAgencyId != null)
      return _defaultAgencyId;
    if (_agencies.size() == 1)
      return _agencies.get(0).getId();
    throw new IllegalStateException("no default agency has been specified");
  }

  protected String getTranslatedAgencyId(String agencyId) {
    String id = _agencyIdMapping.get(agencyId);
    if (id != null)
      return id;
    return agencyId;
  }

  protected String getAgencyForEntity(Class<?> entityType, String entityId) {

    Map<String, String> agencyIdsByEntityId = _agencyIdsByEntityClassAndId.get(entityType);

    if (agencyIdsByEntityId != null) {
      String id = agencyIdsByEntityId.get(entityId);
      if (id != null)
        return id;
    }

    throw new IllegalStateException("no agency id for entity: type="
        + entityType.getName() + " id=" + entityId);
  }

  /****
   * Private Internal Classes
   ****/

  private class EntityHandlerImpl implements EntityHandler {

    public void handleEntity(Object entity) {

      if (entity instanceof Agency) {
        Agency agency = (Agency) entity;
        if (agency.getId() == null && _defaultAgencyId != null)
          agency.setId(_defaultAgencyId);

        // If we already have this agency from a previous load, then we don't
        // add it or save it to the entity store
        if (_agencies.contains(agency))
          return;

        _agencies.add((Agency) entity);

      } else if (entity instanceof Route) {
        Route route = (Route) entity;
        registerAgencyId(Route.class, route.getId());
      } else if (entity instanceof Trip) {
        Trip trip = (Trip) entity;
        registerAgencyId(Trip.class, trip.getId());
      } else if (entity instanceof Stop) {
        Stop stop = (Stop) entity;
        registerAgencyId(Stop.class, stop.getId());
      } else if (entity instanceof FareAttribute) {
        FareAttribute fare = (FareAttribute) entity;
        registerAgencyId(FareAttribute.class, fare.getId());
      }

      if (entity instanceof IdentityBean) {
        _entityStore.save(_context, entity);
      }

    }

    private void registerAgencyId(Class<?> entityType, AgencyAndId id) {

      Map<String, String> agencyIdsByEntityId = _agencyIdsByEntityClassAndId.get(entityType);

      if (agencyIdsByEntityId == null) {
        agencyIdsByEntityId = new HashMap<String, String>();
        _agencyIdsByEntityClassAndId.put(entityType, agencyIdsByEntityId);
      }

      if (agencyIdsByEntityId.containsKey(id.getId()))
        throw new IllegalStateException("duplicate entity id: type="
            + entityType.getName() + " agencyId=" + id.getAgencyId() + " id="
            + id.getId());
      agencyIdsByEntityId.put(id.getId(), id.getAgencyId());
    }
  }

  private class GtfsReaderContextImpl implements GtfsReaderContext {

    public Object getEntity(Class<?> entityClass, Serializable id) {
      return GtfsReader.this.getEntity(entityClass, id);
    }

    public String getDefaultAgencyId() {
      return GtfsReader.this.getDefaultAgencyId();
    }

    public List<Agency> getAgencies() {
      return GtfsReader.this.getAgencies();
    }

    public String getAgencyForEntity(Class<?> entityType, String entityId) {
      return GtfsReader.this.getAgencyForEntity(entityType, entityId);
    }

    public String getTranslatedAgencyId(String agencyId) {
      return GtfsReader.this.getTranslatedAgencyId(agencyId);
    }
  }
}
