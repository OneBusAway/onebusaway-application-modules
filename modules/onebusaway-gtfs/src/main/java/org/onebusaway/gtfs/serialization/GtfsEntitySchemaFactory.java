package org.onebusaway.gtfs.serialization;

import org.onebusaway.gtfs.csv.schema.DefaultEntitySchemaFactory;
import org.onebusaway.gtfs.csv.schema.EntitySchemaFactoryHelper;
import org.onebusaway.gtfs.csv.schema.beans.CsvEntityMappingBean;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.FareAttribute;
import org.onebusaway.gtfs.model.FareRule;
import org.onebusaway.gtfs.model.Frequency;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Transfer;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.serialization.mappings.AgencyIdTranslationFieldMappingFactory;
import org.onebusaway.gtfs.serialization.mappings.DateFieldMappingFactory;
import org.onebusaway.gtfs.serialization.mappings.DefaultAgencyIdFieldMappingFactory;
import org.onebusaway.gtfs.serialization.mappings.EntityFieldMappingFactory;
import org.onebusaway.gtfs.serialization.mappings.RouteAgencyFieldMappingFactory;
import org.onebusaway.gtfs.serialization.mappings.RouteValidator;
import org.onebusaway.gtfs.serialization.mappings.StopTimeFieldMappingFactory;

public class GtfsEntitySchemaFactory {

  public static DefaultEntitySchemaFactory createEntitySchemaFactory() {

    DefaultEntitySchemaFactory factory = new DefaultEntitySchemaFactory();
    EntitySchemaFactoryHelper helper = new EntitySchemaFactoryHelper(factory);

    CsvEntityMappingBean agencyId = helper.addEntity(AgencyAndId.class);
    helper.addIgnorableField(agencyId, "agencyId");

    CsvEntityMappingBean agency = helper.addEntity(Agency.class, "agency.txt",
        "agency_");
    helper.addOptionalField(agency, "id", "agency_id",
        new AgencyIdTranslationFieldMappingFactory());
    helper.addOptionalFields(agency, "lang", "phone");

    CsvEntityMappingBean route = helper.addEntity(Route.class, "routes.txt",
        "route_");
    helper.addOptionalField(route, "agency", "agency_id",
        new RouteAgencyFieldMappingFactory());
    // We set the order of the id field to come after the agency field, such
    // that the agency field will be set before we attempt to set the id field
    helper.addField(route, "id", new DefaultAgencyIdFieldMappingFactory(
        "agency.id"), 1);
    helper.addOptionalFields(route, "desc", "shortName", "longName", "url",
        "color", "textColor");
    route.addValidator(new RouteValidator());

    CsvEntityMappingBean shapePoint = helper.addEntity(ShapePoint.class,
        "shapes.txt");
    shapePoint.setRequired(false);
    helper.addIgnorableField(shapePoint, "id");
    helper.addOptionalField(shapePoint, "shapeId",
        new DefaultAgencyIdFieldMappingFactory());
    helper.addOptionalField(shapePoint, "distTraveled", "shape_dist_traveled");
    helper.addField(shapePoint, "lat", "shape_pt_lat");
    helper.addField(shapePoint, "lon", "shape_pt_lon");
    helper.addField(shapePoint, "sequence", "shape_pt_sequence");

    CsvEntityMappingBean stop = helper.addEntity(Stop.class, "stops.txt",
        "stop_");
    helper.addField(stop, "id", new DefaultAgencyIdFieldMappingFactory());
    helper.addOptionalFields(stop, "code", "desc", "url", "direction");
    helper.addOptionalField(stop, "zoneId", "zone_id");
    helper.addOptionalField(stop, "locationType", "location_type");
    helper.addOptionalField(stop, "parentStation", "parent_station");

    CsvEntityMappingBean trip = helper.addEntity(Trip.class, "trips.txt");
    helper.addField(trip, "route", "route_id", new EntityFieldMappingFactory());
    helper.addField(trip, "id", "trip_id",
        new DefaultAgencyIdFieldMappingFactory("route.agency.id"), 1);
    helper.addOptionalFields(trip, "tripShortName", "tripHeadsign",
        "routeShortName", "directionId", "blockId", "blockSequenceId");
    helper.addOptionalField(trip, "serviceId",
        new DefaultAgencyIdFieldMappingFactory());
    helper.addOptionalField(trip, "shapeId",
        new DefaultAgencyIdFieldMappingFactory());

    CsvEntityMappingBean stopTime = helper.addEntity(StopTime.class,
        "stop_times.txt");
    helper.addIgnorableField(stopTime, "id");
    helper.addField(stopTime, "trip", "trip_id",
        new EntityFieldMappingFactory());
    helper.addField(stopTime, "stop", "stop_id",
        new EntityFieldMappingFactory());
    helper.addOptionalField(stopTime, "arrivalTime",
        new StopTimeFieldMappingFactory());
    helper.addOptionalField(stopTime, "departureTime",
        new StopTimeFieldMappingFactory());
    helper.addOptionalFields(stopTime, "stopHeadsign", "routeShortName",
        "pickupType", "dropOffType", "shapeDistTraveled");

    CsvEntityMappingBean calendar = helper.addEntity(ServiceCalendar.class,
        "calendar.txt");
    helper.addIgnorableField(calendar, "id");
    helper.addField(calendar, "serviceId", "service_id",
        new DefaultAgencyIdFieldMappingFactory());
    helper.addField(calendar, "startDate", new DateFieldMappingFactory());
    helper.addField(calendar, "endDate", new DateFieldMappingFactory());

    CsvEntityMappingBean calendarDate = helper.addEntity(ServiceCalendarDate.class,
        "calendar_dates.txt");
    calendarDate.setRequired(false);
    helper.addIgnorableField(calendarDate, "id");
    helper.addField(calendarDate, "serviceId", "service_id",
        new DefaultAgencyIdFieldMappingFactory());
    helper.addField(calendarDate, "date", new DateFieldMappingFactory());

    CsvEntityMappingBean fareAttributes = helper.addEntity(FareAttribute.class,
        "fare_attributes.txt");
    fareAttributes.setRequired(false);
    helper.addField(fareAttributes, "id","fare_id",
        new DefaultAgencyIdFieldMappingFactory());
    helper.addFields(fareAttributes, "price", "currencyType", "paymentMethod");
    helper.addOptionalFields(fareAttributes, "transfers", "transferDuration");

    CsvEntityMappingBean fareRules = helper.addEntity(FareRule.class,
        "fare_rules.txt");
    fareRules.setRequired(false);
    helper.addIgnorableField(fareRules, "id");
    helper.addField(fareRules, "fare", "fare_id",
        new EntityFieldMappingFactory());
    helper.addOptionalField(fareRules, "route", "route_id",
        new EntityFieldMappingFactory());
    helper.addOptionalFields(fareRules, "originId", "destinationId",
        "containsId");

    CsvEntityMappingBean frequencies = helper.addEntity(Frequency.class,
        "frequencies.txt");
    frequencies.setRequired(false);
    helper.addIgnorableField(frequencies, "id");
    helper.addField(frequencies, "trip", "trip_id",
        new EntityFieldMappingFactory());
    helper.addOptionalField(frequencies, "startTime",
        new StopTimeFieldMappingFactory());
    helper.addOptionalField(frequencies, "endTime",
        new StopTimeFieldMappingFactory());
    helper.addFields(frequencies, "headwaySecs");

    CsvEntityMappingBean transfers = helper.addEntity(Transfer.class,
        "transfers.txt");
    transfers.setRequired(false);
    helper.addIgnorableField(transfers, "id");
    helper.addField(transfers, "fromStop", "from_stop_id",
        new EntityFieldMappingFactory());
    helper.addField(transfers, "toStop", "to_stop_id",
        new EntityFieldMappingFactory());
    helper.addField(transfers, "transferType");
    helper.addOptionalField(transfers, "minTransferTime");

    return factory;
  }
}
