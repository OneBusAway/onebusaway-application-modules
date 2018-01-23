[Back to API parent page](../index.html)

# Method: report-problem-with-stop

Submit a user-generated problem report for a particular stop.  The reporting mechanism provides lots of fields that can
be specified to give more context about the details of the problem (which trip, stop, vehicle, etc was involved),
making it easier for a developer or transit agency staff to diagnose the problem.  These reports feed into the
problem reporting admin interface.

## Sample Request

http://api.pugetsound.onebusaway.org/api/where/report-problem-with-stop/1_75403.xml?key=TEST&amp;code=stop_name_wrong

## Sample Response

~~~~
<response>
  <version>2</version>
  <code>200</code>
  <currentTime>1318879898047</currentTime>
  <text>OK</text>
  <data/>
</response>
~~~~

## Request Parameters

* stopId - the trip id, encoded directly in the URL:
    * `http://api.pugetsound.onebusaway.org/api/where/report-problem-with-stop/[ID GOES HERE].xml`
* code - a string code identifying the nature of the problem
    * `stop_name_wrong` - the stop name in OneBusAway differs from the actual stop's name
    * `stop_number_wrong` - the stop number in OneBusAway differs from the actual stop's number
    * `stop_location_wrong` - the stop location in OneBusAway differs from the actual stop's location
    * `route_or_trip_missing` - an expected route or trip is missing from the stop
    * `other` - catch-all for everythign else
* userComment - additional comment text supplied by the user describing the problem
* userLat - the reporting user's current latitude
* userLon - the reporting user's current longitude
* userLocationAccuracy - the reporting user's location accuracy, in meters

In general, everything but the stop id itself is optional, but generally speaking, providing more fields in the report
will make it easier to diagnose the actual underlying problem.  Note that while we record specific location information
for the user, we do not store any identifying information for the user in order to make it hard to link the user to
their location as some point in the future.

