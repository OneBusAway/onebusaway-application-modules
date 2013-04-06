# GTFS-realtime Export API

OneBusAway provides an API for exporting data in the [GTFS-realtime](https://developers.google.com/transit/gtfs-realtime/)
format.  It supports all three major GTFS-realtime datatypes:

* alerts
* trip updates
* vehicle positions

Specifically, we support methods for retrieving GTFS-realtime feeds on a per agency basis.  The API URLs take the
following form:

* http://localhost:8080/api/gtfs_realtime/alerts-for-agency/1.pb?key=TEST
* http://localhost:8080/api/gtfs_realtime/trip-updates-for-agency/1.pb?key=TEST
* http://localhost:8080/api/gtfs_realtime/vehicle-positions-for-agency/1.pb?key=TEST

Here, replace `localhost:8080` with the hostname and port of your OneBusAway API server.  Data will be requested
for a particular agency, as specified by the agency id encoded directly in the URL ("1" in the example urls).

## Parameters

* **removeAgencyIds=true** - By default, entity ids are prefixed with agency ids in the OBA system (eg. 1_456, where 1 is
  the agency id and 456 is the entity id).  If the removeAgencyIds parameter is specified, the agency id prefixes
  will be stripped from ids in the resulting GTFS-realtime feed.
* **time=...** - By default, the GTFS-realtime feeds represent the status of the system right now.  However, the system
  can also be queried at a specific time.  This can be useful for testing.  See [timestamps](where/index.html#Timestamps)
  for details on the format of the `time` parameter.
  
## Output Format

By default, these methods return GTFS-realtime data encoded as a binary protocol buffer, per the GTFS-realtime spec.
To make it easier to debug your system, we also provide a simple way to see a textual representation of each
GTFS-realtime feed as well.  Simple switch the ".pb" extension in the URL with ".pbtext".  For example:

## Modules

The GTFS-realtime export API is powered by the `onebusaway-api-webapp` module.