# Troubleshooting Guide

## How can I check if OneBusAway is running correctly?  

If you've installed and configured OneBusAway, but aren't sure if everything is running correctly, here are some things
you can check.

For all the following examples, we assume you've deployed OneBusAway at [http://localhost:8080/](http://localhost:8080/).
This is the default for the [Quick-Start](quickstart-guide.html).  If you've deployed OneBusAway at a different
location, update the paths appropriately.

### Checking the Logs for Exceptions

When you start up any of the webapps, they produce a lot of logging messages.  Most of the messages are just
info diagnostics that you can safely ignore.  Unfortunately, sometimes an exception that will prevent the application
from running properly can be buried in these logs as well.  It can be a good idea to go back and scan the logs to see
if there were any exceptions thrown that might indicate a larger problem.

### Checking the Webapp

First, browse to the root of the webapp:

[http://localhost:8080/](http://localhost:8080/)

You should see either the welcome page from the default wiki if you are running the quick-start or perhaps other
content if you configured a different content source.

Next, check for agencies:

[http://localhost:8080/where/standard/agencies.action](http://localhost:8080/where/standard/agencies.action)

Assuming that you have properly built and loaded your [Transit Data Bundle](transit-data-bundle-guide.html), you should
see your transit agencies listed there, along with the general service area highlighted on the map.  If you don't see
any agencies listed, then it indicates a problem with your transit data bundle (improperly built?  improper path
specified to the webapp?).

Next, check the main map view:

[http://localhost:8080/where/standard/](http://localhost:8080/where/standard/)

This should bring up a browseable map.  If you get an error about your Google Maps API key, you need to specify your
own API key.  Check out the `defaultWebappConfigurationSource.googleMapsApiKey` parameter in the
[parameter config documentation](../oba-configs/index.html).

You should be able to search for routes by name and zoom in to find transit stops on the map.  If you can't find any
routes or stops, you might jump ahead to the API section, which will let you interrogate all data loaded in your bundle.

If you know the specific stop id of a stop, you can see arrivals and departures for that stop by going to the following
url:

[http://localhost:8080/where/standard/stop.action?id=AGENCYID_STOPID](http://localhost:8080/where/standard/stop.action?id=AGENCYID_STOPID)

where AGENCYID and STOPID are the agency id and stop id configured for the stop in your GTFS.

### Checking the API Webapp

First, request the set of agencies currently loaded in your system ([method documentation](../api/where/methods/agencies-with-coverage.html)):

[http://localhost:8080/api/where/agencies-with-coverage.xml?key=TEST](http://localhost:8080/api/where/agencies-with-coverage.xml?key=TEST)

You should see a response like:

~~~
<response>
  <version>2</version>
  <code>200</code>
  <currentTime>1334485773643</currentTime>
  <text>OK</text>
  <data class="listWithReferences">
    <references>
      <agencies>
        <agency>
          <id>3</id>
          <name>Pierce Transit</name>
          ...
        </agency>
      </agencies>
    </references>
    <list>
      <agencyWithCoverage>
        <agencyId>3</agencyId>
        <lat>47.221315000000004</lat>
        <lon>-122.4051325</lon>
        ...
      </agencyWithCoverage>
    </list>
  </data>
</response>
~~~

If you don't see any entries, it probably means your transit data bundle wasn't configured properly.

Next, for a specific agency, you can query the set of routes ([method documentation](../api/where/methods/routes-for-agency.html))
and stops ([method documentation](../api/where/methods/stops.html)) for the agency.

For routes, use the following URL, replace `AGENCYID` with the agency id from the [<agency/> element](../api/where/elements/agency.html)
in the agencies response.  

[http://localhost:8080/api/where/routes-for-agency/AGENCYID.xml?key=TEST](http://localhost:8080/api/where/routes-for-agency/AGENCYID.xml?key=TEST)

You should see a number of [<route/> elements](../api/where/elements/route.html) for the routes in your system.

~~~
<response>
  <version>2</version>
  <code>200</code>
  <currentTime>1334486660558</currentTime>
  <text>OK</text>
  <data class="listWithReferences">
    <references>
      <agencies>...</agencies>
    </references>
    <list>
      <route>
        <id>1_1</id>
        <shortName>1</shortName>
        ...
      </route>
      ...
~~~

If you don't see any elements, it could indicate a problem with your transit data bundle.

For stops, use the following URL, again updating `AGENCYID` appropriately:

[http://localhost:8080/api/where/stop-ids-for-agency/AGENCYID.xml?key=TEST](http://localhost:8080/api/where/stop-ids-for-agency/AGENCYID.xml?key=TEST)

Here, you should see a list of stop ids for all the stops in your system.

~~~
<response>
  <version>2</version>
  <code>200</code>
  <currentTime>1334486349294</currentTime>
  <text>OK</text>
  <data class="listWithReferences">
    <references/>
    <list>
      <string>1_46660</string>
      <string>1_69670</string>
      <string>1_75580</string>
      ...
~~~

If you don't see any elements, it could indicate a problem with your transit data bundle.

### Checking the SMS Webapp 



## Why does searching for an intersection return the same set of results for any address?

First, check that your geocoder implementation is set appropriately. In data-sources.xml, ensure the
"externalGeocoderImpl" line looks like this:

~~~
<bean id="externalGeocoderImpl" class="org.onebusaway.geocoder.impl.GoogleGeocoderImpl" />
~~~

Secondly, flush the geocoder cache. To do that, connect to the OBA database and run:

~~~
delete * from oba_geocoder_results;
~~~

## I'm running OBA in a virtualized environment, and seeing OBA's memory consumption keep going up. What can I do?

On certain virtualized environments, the JDK can have trouble detecting how much memory to allocate to each webapp.
The solution is to set a maximum memory limit by setting the JAVA_OPTS environment variable as so:

~~~
-Xmx256M -server 
~~~

For more information on the specific interaction with OpenVZ and the JDK, see
http://forum.proxmox.com/threads/1495-Proxmox-OpenVZ-memory-Java-VMs-and-Zimbra

## After upgrading from a previous version, hibernate didn't update my DB schema, and now it's complaining that "duplicate key value violates unique constraint 'oba_nyc_raw_location_pkey'"!

The best way to fix this is to either add the changed columns yourself (not recommended) or, your better bet, drop the
database and let hibernate regenerate it all over again. We'd had bad luck dropping individual tables and waiting for
those to be rebuilt. 

If you need to migrate data from a previous schema version, we found this little script handy:

~~~
-- create function to prevent optimizer from removing the select nextval
CREATE FUNCTION volatile_nextval(text) RETURNS bigint VOLATILE AS 'BEGIN RETURN nextval($1); END;' LANGUAGE 'plpgsql';

-- move old data into temp table
select bearing, destinationsigncode, deviceid, gga, latitude, longitude, rmc, time, -1 AS timereceived, vehicle_agencyid, vehicle_id, (select volatile_nextval('hibernate_sequence') AS id) AS id into temporary table test from oba_nyc_raw_location_old;

-- update id value to sequence value (can't do this in a select...)
update test set id=volatile_nextval('hibernate_sequence');

-- move back to real table
insert into oba_nyc_raw_location(bearing, destinationsigncode, deviceid, gga, latitude, longitude, rmc, time, timereceived, vehicle_agencyid, vehicle_id, id) select bearing, destinationsigncode, deviceid, gga, latitude, longitude, rmc, time, -1, vehicle_agencyid, vehicle_id, id from test;
~~~