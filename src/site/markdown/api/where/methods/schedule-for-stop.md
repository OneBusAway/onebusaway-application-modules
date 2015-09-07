[Back to API parent page](../index.html)

# Method: schedule-for-stop

Retrieve the full schedule for a stop on a particular day

## Sample Request

http://api.pugetsound.onebusaway.org/api/where/schedule-for-stop/1_75403.xml?key=TEST

## Sample Response

    <response>
      <version>2</version>
      <code>200</code>
      <text>OK</text>
      <currentTime>1270614730908</currentTime>
      <data class="entryWithReferences">
        <references>...</references>
        <entry class="stopSchedule">
          <date>1270623339481</date>
          <stopId>1_75403</stopId>
          <stopRouteSchedules>
            <stopRouteSchedule>
              <routeId>1_31</routeId>
              <stopRouteDirectionSchedules>
                <stopRouteDirectionSchedule>
                  <tripHeadsign>Central Magnolia</tripHeadsign>
                  <scheduleStopTimes>
                    <scheduleStopTime>
                      <arrivalTime>1270559769000</arrivalTime>
                      <departureTime>1270559769000</departureTime>
                      <serviceId>1_114-WEEK</serviceId>
                      <tripId>1_11893408</tripId>
                    </scheduleStopTime>
                    <!-- More schduleStopTime entries... -->
                  </scheduleStopTimes>
                </stopRouteDirectionSchedule>
              </stopRouteDirectionSchedules>
              <!-- More stopRouteDirectionSchedule entries -->
            </stopRouteSchedule>
            <!-- More stopRouteSchedule entries -->
          </stopRouteSchedules>
          <timeZone>America/Los_Angeles</timeZone>
          <stopCalendarDays>
            <stopCalendarDay>
              <date>1276239600000</date>
              <group>1</group>
              </stopCalendarDay>
            <!-- More stopCalendarDay entries -->
          </stopCalendarDays>
        </entry>
      </data>
    </response>

## Request Parameters

* id - the stop id to request the schedule for, encoded directly in the URL:
	* `http://api.pugetsound.onebusaway.org/api/where/schedule-for-stop/[ID GOES HERE].xml`
* date - The date for which you want to request a schedule of the format YYYY-MM-DD (optional, defaults to current date)

## Response

The response is pretty complex, so we'll describe the details at a high-level along with references to the various elements in the response.

The response can be considered in two parts.  The first part lists specific arrivals and departures at a stop on a given date (`<stopRouteSchedules/>` section) while the second part lists which days the stop currently has service defined (the `<stopCalendarDays/>` section).  By convention, we refer to the arrival and departure time details for a particular trip at a stop as a stop time.

We break up the stop time listings in a couple of ways.  First, we split the stop times by route (corresponds to each `<stopRouteSchedule/>` element).  We next split the stop times for each route by direction of travel along the route (corresponds to each `<stopRouteDirectionSchedule/>` element).  Most stops will serve just one direction of a particular route, but some stops will serve both directions, and it may be useful to present those listings separately.  Each `<stopRouteDirectionSchedule/>` element has a `tripHeadsign` property that indicates the direction of travel.

Finally we get down to the unit of a stop time, as represented by the `<scheduleStopTime/>` element.  Each element has the following set of properties:

* arrivalTime - time in milliseconds since the Unix epoch that the transit vehicle will arrive
* departureTime - time in milliseconds since the Unix epoch that the transit vehicle will depart
* tripId - the id for the trip of the scheduled transit vehicle
* serviceId - the serviceId for the schedule trip (see the [GTFS spec](http://code.google.com/transit/spec/transit_feed_specification.html) for more details

In addition to all the `<scheduleStopTime/>` elements, the response also contains `<stopCalendarDay/>` elements which list out all the days that a particular stop has service.  This element has the following properties:

* date - the date of service in milliseconds since the Unix epoch
* group - we provide a group id that groups `<stopCalendarDay/>` into collections of days with similar service.  For example, Monday-Friday might all have the same schedule and the same group id as result, while Saturday and Sunday have a different weekend schedule, so they'd get their own group id.

In addition to all the `<scheduleStopTime/>` elements, the main entry also has the following properties:

* date - the active date for the returned calendar
* stopId - the stop id for the requested stop, which can be used to access the [`<stop/>` element](../elements/stop.html) in the `<references/>` section
* timeZone - the time-zone the stop is located in

### Proposed Additions

<font color="red">BETA: These are proposed additions and are subject to change, even if they are available on test or production servers.</font>

In order to better support frequency-based scheduling, we propose the addition of some new elements to model a frequency-based schedule.

    <stopRouteDirectionSchedule>
      ...
      <scheduleFrequencies>
        <scheduleFrequency>
          <serviceDate>1289548800000</serviceDate>
          <startTime>1289566800000</startTime>
          <endTime>1289570399000</endTime>
          <headway>900</headway>
          <serviceId>1_116-WEEK</serviceId>
          <tripId>40_15043574</tripId>
        </scheduleFrequency>
        <scheduleFrequency>...</scheduleFrequency>
      </scheduleFrequencies>
    </stopRouteDirectionSchedule>

Much like the `<stopRouteDirectionSchedule>` currently has a list of `<scheduleStopTime>` element that captures scheduled arrivals, we propose the addition of a list of `<scheduleFrequency>` elements that capture frequency-based arrivals.  The frequency defines the service interval, the frequency of service, and some details about the trip.
