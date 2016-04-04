# Real-Time Configuration Guide  
  
While OneBusAway can work with just static schedule data, all the most interesting features require real-time transit
information.

## GTFS-realtime

We support [GTFS-realtime](https://developers.google.com/transit/gtfs-realtime) out of the box, including support for trip updates,
vehicle positions, and alerts.  To add support, create a
[GtfsRealtimeSource](./apidocs/org/onebusaway/transit_data_federation/impl/realtime/gtfs_realtime/GtfsRealtimeSource.html)
bean in your `data-sources.xml` file.  Then specify URLs for the three different types of GTFS-realtime data as
properties of the bean.  Here is a full example: 
  
~~~
<bean class="org.onebusaway.transit_data_federation.impl.realtime.gtfs_realtime.GtfsRealtimeSource">
  <property name="tripUpdatesUrl" value="http://url/to/gtfs-realtime/trip-updates" />
  <property name="vehiclePositionsUrl" value="http://url/to/gtfs-realtime/vehicle-positions" />
  <property name="alertsUrl" value="http://url/to/gtfs-realtime/alerts" />
  <!-- Optionally set the refresh interval - how often we query the URLs, in seconds (default=30) -->
  <property name="refreshInterval" value="30"/>
  <!-- Optionally configure the agency id we use to match incoming data -->
  <property name="agencyId" value="SomeAgencyId" />
  <property name="agencyIds>
    <list>
      <value>SomeAgencyIdA</value>
      <value>SomeAgencyIdB</value>
    </list>
  </property>
</bean>
~~~

You can additionally specify a `refreshInterval` that controls how often we query the URLs, in seconds.  If you
are deploying a multi-agency instance of OneBusAway, you may also need to specify the `agencyId` or `agencyIds`
of the transit agency to associate the incoming GTFS-realtime data with.  This allows you to define multiple incoming
GTFS-realtime data-sources from different agencies in the same system.

You must provide both a TripUpdates feed and a VehiclePositions feed in order for OneBusAway to report vehicle positions.
Without a TripUpdates feed OneBusAway will discard the vehicle positions.

## SIRI VM

We support [SIRI](http://siri.org.uk/) out of the box, including support for vehicle monitoring (VM) and situation
exchange (SX).  To add support, create a [SiriController](./apidocs/org/onebusaway/transit_data_federation_webapp/siri/SiriController.html)
bean in your `data-sources.xml` file.  Then specify
[/onebusaway-siri/${onebusaway-siri-version}/cli-request-spec.html}SIRI endpoint requests](${site_base_url) indicating
your SIRI data-source.  Here is a full example:

~~~
<!-- The "name" parameter controls which URL the SIRI client listens for pub-sub data.  See "clientUrl" below. -->
<bean name="/siri.action" class="org.onebusaway.transit_data_federation_webapp.siri.SiriController">
  <!-- Specify the SIRI endpoint -->
  <property name="endpoint" value="Url=http://host/siri-endpoing.xml,ModuleType=VEHICLE_MONITORING" />
  <!-- Or you can specify a series of endpoints -->
  <property name="endpoints">
    <list>
      <value>...</value>
      <value>...</value>
    </list>
  </property>
  <!-- Control the URL your SIRI client publically broadcasts to endpoints for pub-sub data exchange -->
  <property name="clientUrl" value="http://localhost:8080/onebusaway-transit-data-federation-webapp/remoting/siri.action" />  
  <!-- Want to see what's going on behind the scenes with SIRI XML messages? -->
  <property name="logRawXmlType" value="CONTROL"/>
</bean>
~~~

## Orbital/ACS/Xerox OrbCAD

A number of agencies have Orbital/ACS/Xerox OrbCAD AVL systems.  Some agencies have configured the data-export option of
these systems to spit out a CSV file of vehicle locations and schedule deviations for all the vehicles in their fleet,
which is then shared through a webserver.  OneBusAway supports data of this form.  To add support, create a
[OrbcadRecordHttpSource](./apidocs/org/onebusaway/transit_data_federation/impl/realtime/orbcad/OrbcadRecordHttpSource.html)
bean in your `data-sources.xml` file.  Then specify the URL for your real-time data file.  Here is a full example:

~~~
<bean class="org.onebusaway.transit_data_federation.impl.realtime.orbcad.OrbcadRecordHttpSource">
  <property name="url" value="http://host/busdata.txt" />
  <!-- Optionally configure the agency id we use to match incoming data -->
  <property name="agencyIds">
    <list>
      <value>SomeAgencyId</value>
    </list>
  </property>
</bean>
~~~

## Custom Implementation

Interested in adapting your own real-time system to OneBusAway?  The best option is to create an adapter between your system
and an existing specification like [GTFS-realtime](http://code.google.com/transit/realtime/) or [SIRI](http://siri.org.uk/)
and then use OneBusAway's native support for those formats.

However, if that's not going to cut it, you can create your own custom real-time data-source plugin to OneBusAway.  This
might be a good solution if you need direct access to OneBusAway data-structures and service interfaces to process your
real-time data.

Creating a custom implementation typically involves the following steps:
  
* Create a Java class that implements your real-time data-source.
* Add your class to the OneBusAway application server class path.
* Create an instance of your class in your `data-source.xml` file.
* Have your implementation periodically notify OneBusAway of new real-time data using the [VehicleLocationListener](./apidocs/org/onebusaway/realtime/api/VehicleLocationListener.html) interface.
  
Here is a quick example to get you started:
  
~~~
public MyRealTimeSource {

  private VehicleLocationListener _listener

  @Autowired
  public void setVehicleLocationListener(VehicleLocationListener listener) {
    _listener = listener;
  }
  
  @PostConstruct
  public void start() {
    // Span a thread that periodically poll your data-source, or some other method of receiving data
  }
  
  @PreDestroy {
  public void stop() {
    // Shut-down any threads you started
  }
  
  public void processRealTimeData(...) {
    VehicleLocationRecord record = createRercordFromRealTimeData(...);
    _listener.handleVehicleLocationRecord(record);
  }
}
~~~
