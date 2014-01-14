[Back to API parent page](../index.html)

# The &lt;blockConfiguration/&gt; Element

A `<blockConfiguration/>` captures a sequence of block trips active on a particular service date.  Recall that a block is a sequence of linked trips operated by the same vehicle.  Unfortunately, that set of trips is not always the same.  For example, the last trip of a block may typically be active, but might be left of during a holiday.

To help model these situations correctly, we have the concept of a block configuration: a sequence of block trips that are active for a particular block on a particular service date.  The active service date is determined by the combination of active and inactive service ids on that date.  See the [`<trip/>`](trip.html) element and the [GTFS spec](http://code.google.com/transit/spec/transit_feed_specification.html#trips_txt___Field_Definitions) for more discussion of service ids and how they determine when a trip is active.

In addition to service id information, the block configuration contains the list of active block trips.

## Example

    <blockConfiguration>
      <activeServiceIds>
        <string>1_116-WEEK</string>
      </activeServiceIds>
      <inactiveServiceIds/>
      <trips>
        <blockTrip>
          <tripId>1_15415757</tripId>
          <blockStopTimes>
            <blockStopTime>
              <blockSequence>0</blockSequence>
              <distanceAlongBlock>45.79241448571604</distanceAlongBlock>
              <accumulatedSlackTime>0.0</accumulatedSlackTime>
              <stopTime>
                <stopId>1_75995</stopId>
                <arrivalTime>21607</arrivalTime>
                <departureTime>21607</departureTime>
                <pickupType>0</pickupType>
                <dropOffType>0</dropOffType>
              </stopTime>
            </blockStopTime>
            <blockStopTime>...</blockStopTime>
            ...
          </blockStopTimes>
          <accumulatedSlackTime>0</accumulatedSlackTime>
          <distanceAlongBlock>0.0</distanceAlongBlock>
        </blockTrip>
      </trips>
    </blockConfiguration>
    
## Details

The `<blockConfiguration/>` element has the following sub-elements:

* activeServiceIds - a collection of strings indicating which service ids are active for the block configuration
* inactiveServiceIds - a collection of strings indicating which service ids are NOT active for the block configuration
* trips - the sequence of `<blockTrip/>` elements for each block trip in the configuration

The block trip has the following properties:

* tripId - id of the referenced [`<trip/>`](trip.html) element
* block stop times - see below
* accumulated slack time - how much slack time from layovers has been accumulated from previous block trips in the block up to the start of this block
* distanceAlongBlock - how far along the block, in meters, is the start of this trip

Block stop times capture the individually scheduled stops along each trip.  We provide the following fields:

* blockSequence - the index of the block stop time in the list of all scheduled stops for the block configuration
* distanceAlongBlock - how far along the block, in meters, this stop occurs
* accumulatedSlackTime - how much slack time from layovers has been accumulated previous along the block up until, but not including, this stop
* stopTime - reference to the general stop time for the trip - see below

A stop time is a more general notion of a scheduled stop along a trip:

* stopId -  id of the referenced [`<stop/>`](stop.html) element
* arrivalTime - time, in seconds from the start of the service date
* departureTime - time, in seconds from the start of the service date
* pickupType - see the [GTFS spec](http://code.google.com/transit/spec/transit_feed_specification.html#stop_times_txt___Field_Definitions)
* dropOffType - see the [GTFS spec](http://code.google.com/transit/spec/transit_feed_specification.html#stop_times_txt___Field_Definitions)
