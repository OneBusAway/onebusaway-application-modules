[Back to API parent page](../index.html)

# The &lt;vehicleStatus/&gt; Element

The `<vehicleStatus/>` element captures extended information about an active
vehicle.

## Example

~~~
<vehicleStatus>
  <vehicleId>1_244</vehicleId>
  <lastUpdateTime>1365194918000</lastUpdateTime>
  <lastLocationUpdateTime>1365194918000</lastLocationUpdateTime>
  <location>
    <lat>47.238071</lat>
    <lon>-122.293922</lon>
  </location>
  <tripId>3_4989224-13FEB-MVS-WKD-Weekday-04</tripId>
  <tripStatus>...</tripStatus>
</vehicleStatus>
~~~

## Details

* vehicledId - the id of the vehicle
* lastUpdateTime - the last known real-time update from the transit vehicle
* lastLocationUpdateTime - the last known real-time update from the transit vehicle containing a location update
* location - the last known location of the vehicle
* tripId - the id of the vehicle's current trip, which can be used to look up the referenced [`<trip/>` element](trip.html) in the `<references/>` section.
* tripStatus - [`<tripStatus/>` element](trip-status.html), providing additional status information for the vehicle's trip. 

A vehicle may not have an actively assigned trip.