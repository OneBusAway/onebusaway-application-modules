[Back to API parent page](../index.html)

# Method: register-alarm-for-arrival-and-departure-at-stop

Register an alarm for a single arrival and departure at a stop, with a callback URL to be requested when the alarm is fired.

## Sample Request

http://api.pugetsound.onebusaway.org/api/where/register-alarm-for-arrival-and-departure-at-stop/1_75403.xml?key=TEST&amp;tripId=1_15551341&amp;serviceDate=1291536000000&amp;vehicleId=1_3521&amp;stopSequence=42&amp;alarmTimeOffset=120&amp;url=http://host/callback_url

## Sample Response

~~~~
<response>
  <version>2</version>
  <code>200</code>
  <currentTime>1318879898047</currentTime>
  <text>OK</text>
  <data class="entryWithReferences">
    <references/>
    <entry class="registeredAlarm">
      <alarmId>1_7deee53d-9eb5-4f6b-8623-8bff398fcd5b</alarmId>
    </entry>
  </data>
</response>
~~~~

## Request Parameters

* id, tripId, serviceDate, vehicleId, stopSequence - see discussion in [arrival-and-departure-for-stop](arrival-and-departure-for-stop.html) API method for discussion of how to specify a particular arrival or departure
* url - callback URL that will be requested when the alarm is fired
* alarmTimeOffset - time, in seconds, that controls how long before the arrival/departure the alarm will be fired.  Default is zero.
* onArrival - set to true to indicate the alarm should be fired relative to vehicle arrival, false for departure.  The default is false for departure.

We provide an arrival-departure alarm callback mechanism that allows you to register an alarm for an arrival or departure event and received a callback in the form of a GET request to a URL you specify.

In order to specify an alarm for something like "5 minutes before a bus departs, we provide the `alarmTimeOffset` which specifies when the alarm should be fired relative to the actual arrival or departure event.  A value of 60 indicates that the alarm should be fired 60 seconds before, while a value of -30 would be fired 30 seconds after.

*A note about scheduled vs real-time arrivals and departures:*  You can register alarms for trips where we don't have any real-time data (aka a scheduled arrival and departure) and we will fire the alarm at the appropriate time.  Things get a bit trickier when you've registered an alarm for a scheduled arrival and we suddenly have real-time for the trip after you've registered.  In these situations, we will automatically link your alarm to the real-time arrival and departure.

## Response

The response is the alarm id.  Note that if you include `#ALARM_ID#` anywhere in your callback URL, we will automatically replace it with the id of the alarm being fired.  This can be useful when you register multiple alarms and need to be able to distinguish between them.

Also see the [cancel-alarm](cancel-alarm.html) API method, which also accepts the alarm id as an argument.