package org.onebusaway.gtfs_diff.impl.serialization;

import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs_diff.model.EntityMatch;
import org.onebusaway.gtfs_diff.model.EntityPropertyMismatch;
import org.onebusaway.gtfs_diff.model.GtfsDifferences;
import org.onebusaway.gtfs_diff.model.Match;
import org.onebusaway.gtfs_diff.model.MatchCollection;
import org.onebusaway.gtfs_diff.model.Mismatch;
import org.onebusaway.gtfs_diff.model.EntityMismatch;
import org.onebusaway.gtfs_diff.model.PotentialEntityMatch;
import org.onebusaway.gtfs_diff.model.ServiceId;
import org.onebusaway.gtfs_diff.model.ServiceIdDateMismatch;
import org.onebusaway.gtfs_diff.services.GtfsDifferencesSerializationService;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;

public class PrintGtfsDifferencesSerializationServiceImpl implements
    GtfsDifferencesSerializationService {

  private static DateFormat _dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);

  private File _path;

  private PrintWriter _out;

  private GtfsDifferences _differences;

  public PrintGtfsDifferencesSerializationServiceImpl(File path) {
    _path = path;
  }

  public void serializeDifferences(GtfsDifferences differences) {

    try {
      _out = new PrintWriter(new FileWriter(_path));
    } catch (IOException ex) {
      throw new IllegalStateException(ex);
    }

    _differences = differences;
    handleMatchCollection(differences, "");
    _out.close();
  }

  private void handleMatchCollection(MatchCollection collection, String prefix) {
    for (Match match : collection.getMatches())
      handleMatch(match, prefix);
    for (Mismatch mismatch : collection.getMismatches())
      handleMismatch(mismatch, prefix);
  }

  private void handleMatch(Match match, String prefix) {
    if (match instanceof EntityMatch)
      handleEntityMatch((EntityMatch<?>) match, prefix);
    handleMatchCollection(match, prefix + "  ");
  }

  private void handleEntityMatch(EntityMatch<?> match, String prefix) {
    StringBuilder b = new StringBuilder();
    b.append(prefix);
    b.append("entity match: ");
    b.append(value(match.getEntityA()));
    b.append(" ");
    b.append(value(match.getEntityB()));
    if (match instanceof PotentialEntityMatch) {
      PotentialEntityMatch<?> pem = (PotentialEntityMatch<?>) match;
      b.append(" score=");
      b.append(pem.getScore());
    }
    _out.println(b.toString());
  }

  private void handleMismatch(Mismatch mismatch, String prefix) {
    if (mismatch instanceof EntityMismatch) {
      handleOnlyInOneModelMismatch((EntityMismatch) mismatch, prefix);
    } else if (mismatch instanceof EntityPropertyMismatch) {
      handleEntityPropertyMismatch((EntityPropertyMismatch) mismatch, prefix);
    } else if (mismatch instanceof ServiceIdDateMismatch) {
      handleServiceIdDateMismatch((ServiceIdDateMismatch) mismatch, prefix);
    }
    handleMatchCollection(mismatch, prefix + "  ");
  }

  private void handleOnlyInOneModelMismatch(EntityMismatch mismatch,
      String prefix) {
    Object entityA = mismatch.getEntityA();
    Object entityB = mismatch.getEntityB();
    if (entityA == null && entityB == null) {
      _out.println(prefix + "entity mismatch: null vs null");
    } else if (entityA == null) {
      _out.println(prefix + "only in model B: " + value(entityB));
    } else if (entityB == null) {
      _out.println(prefix + "only in model A: " + value(entityA));
    } else {
      _out.println(prefix + "entity mismatch: " + value(entityA) + " "
          + value(entityB));
    }

  }

  private void handleEntityPropertyMismatch(EntityPropertyMismatch mismatch,
      String prefix) {
    BeanWrapper a = new BeanWrapperImpl(mismatch.getEntityA());
    BeanWrapper b = new BeanWrapperImpl(mismatch.getEntityB());
    Object valueA = a.getPropertyValue(mismatch.getPropertyName());
    Object valueB = b.getPropertyValue(mismatch.getPropertyName());
    _out.println(prefix + "entity property mismatch: entityA="
        + value(mismatch.getEntityA()) + " entityB="
        + value(mismatch.getEntityB()) + " property="
        + mismatch.getPropertyName() + " valueA=" + value(valueA) + " valueB="
        + value(valueB));
  }

  private void handleServiceIdDateMismatch(ServiceIdDateMismatch mismatch,
      String prefix) {
    Date dateA = mismatch.getDateA();
    Date dateB = mismatch.getDateB();
    if (dateA == null && dateB == null) {
      _out.println(prefix + "service id date mismatch: serviceIdA="
          + value(mismatch.getServiceIdA()) + " serviceIdB="
          + value(mismatch.getServiceIdB()));
    } else if (dateA == null) {
      _out.println(prefix + "service id date mismatch: serviceIdA="
          + value(mismatch.getServiceIdA()) + " serviceIdB="
          + value(mismatch.getServiceIdB()) + " onlyInB=" + date(dateB));
    } else if (dateB == null) {
      _out.println(prefix + "service id date mismatch: serviceIdA="
          + value(mismatch.getServiceIdA()) + " serviceIdB="
          + value(mismatch.getServiceIdB()) + " onlyInA=" + date(dateA));
    } else {
      _out.println(prefix + "service id date mismatch: serviceIdA="
          + value(mismatch.getServiceIdA()) + " serviceIdB="
          + value(mismatch.getServiceIdB()) + " dateA=" + date(dateA)
          + " dateB=" + date(dateB));
    }
  }

  private String value(Object entity) {

    if (entity == null)
      return "null";

    if (entity instanceof Agency) {
      Agency agency = (Agency) entity;
      return "Agency(" + id(agency.getId()) + ")";
    } else if (entity instanceof Route) {
      Route route = (Route) entity;
      return "Route(" + id(route.getId()) + " " + route.getShortName() + ")";
    } else if (entity instanceof Trip) {
      Trip trip = (Trip) entity;
      return "Trip(" + id(trip.getId()) + ")";
    } else if (entity instanceof ServiceId) {
      ServiceId serviceId = (ServiceId) entity;
      return "ServiceId(" + id(serviceId.getServiceId()) + ")";
    }

    return entity.toString();
  }

  private String date(Date date) {
    return _dateFormat.format(date);
  }

  private AgencyAndId id(AgencyAndId id) {
    String aid = id(id.getAgencyId());
    return new AgencyAndId(aid, id.getId());
  }

  private String id(String id) {
    if (id.startsWith(_differences.getModelIdA()))
      id = id.substring(_differences.getModelIdA().length());
    if (id.startsWith(_differences.getModelIdB()))
      id = id.substring(_differences.getModelIdB().length());
    return id;
  }
}
