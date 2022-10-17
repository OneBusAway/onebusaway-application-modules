[Back to API parent page](../index.html)

# Method: arrival-and-departure-for-stop

Get info about a single arrival and departure for a stop

## Sample Requests

http://api.pugetsound.onebusaway.org/api/where/arrival-and-departure-for-stop/1_75403.xml?key=TEST&amp;tripId=1_15551341&amp;serviceDate=1291536000000&amp;vehicleId=1_3521&amp;stopSequence=42

http://api.pugetsound.onebusaway.org/api/where/arrival-and-departure-for-stop/1_75403.xml?key=TEST&amp;tripId=1_47634632&amp;serviceDate=1590476400000

## Sample Response

    <response>
      <version>2</version>
      <code>200</code>
      <text>OK</text>
      <currentTime>1270614730908</currentTime>
      <data class="entryWithReferences">
        <references>...</references>
        <entry class="arrivalAndDeparture">
          <!-- See documentation for the arrivalAndDeparture element, linked below -->
        </entry>
      </data>
    </response>

## Request Parameters

* id - the stop id, encoded directly in the URL:
    * `http://api.pugetsound.onebusaway.org/api/where/arrival-and-departure-for-stop/[ID GOES HERE].xml`
* tripId - the trip id of the arriving transit vehicle
* serviceDate - the service date of the arriving transit vehicle
* vehicleId - the vehicle id of the arriving transit vehicle (optional)
* stopSequence - the stop sequence index of the stop in the transit vehicle's trip
* time - by default, the method returns the status of the system right now.  However, the system
  can also be queried at a specific time.  This can be useful for testing.  See [timestamps](../index.html#Timestamps)
  for details on the format of the `time` parameter.

The key here is uniquely identifying which arrival you are interested in.  Typically, you would first make a call to [arrivals-and-departures-for-stop](arrivals-and-departures-for-stop.html) to get a list of upcoming arrivals and departures at a particular stop.  You can then use information from those results to specify a particular arrival.  At minimum, you must specify the trip id and service date.  Additionally, you are also encouraged to specify the vehicle id if available to help disambiguate between multiple vehicles serving the same trip instance.  Finally, you are encouraged to specify the stop sequence.  This helps in the situation when a vehicle visits a stop multiple times during a trip (it happens) plus there is performance benefit on the back-end as well.

## Response

The method returns an [`<arrivalAndDeparture/>` element](../elements/arrival-and-departure.html) as its content.
