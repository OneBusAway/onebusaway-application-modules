[Back to API parent page](../index.html)

# ~~Method: plan-trip~~

~~This method allows you to plan a trip using public transit between locations.  Detailed information about the sequence of steps for the trip, including walking and transit, are included in the response.  A variety of parameters can be used to control which trip plans are favored in a particular request.~~

**This method is deprecated and no longer supported**

Check out the [OpenTripPlanner](http://www.opentripplanner.org/) open-source project for multimodal trip planning capabilities.

## Sample Request

http://soak-api.pugetsound.onebusaway.org/api/where/plan-trip.xml?key=TEST&amp;latFrom=47.669940&amp;lonFrom=-122.387958&amp;latTo=47.598941&amp;lonTo=-122.331138&amp;time=2011-01-31_12-48-00

## Sample Response

    <response>
      <version>2</version>
      <code>200</code>
      <text>OK</text>
      <currentTime>1270614730908</currentTime>
      <data class="entryWithReferences">
        <references>...</references>
        <entry class="itineraries">
          <from>
            <location>
              <lat>47.66994</lat>
              <lon>-122.387958</lon>
            </location>
          </from>
          <to>
            <location>
              <lat>47.598941</lat>
              <lon>-122.331138</lon>
            </location>
          </to>
          <itineraries>
            <itinerary>
              <startTime>1296507182000</startTime>
              <endTime>1296509365000</endTime>
              <legs>
                <!-- Details about legs below -->
                <leg>...</leg>
                <leg>...</leg>
              </legs>
            </itinerary>
          </itineraries>
        </entry>
      </data>
    </response>

The `<leg/>` element comes in two flavors: a walking leg or a transit leg.  Let's look at a walking leg first.

    <leg>
      <startTime>1296507182000</startTime>
      <endTime>1296507221000</endTime>
      <distance>52.095607420905495</distance>
      <mode>walk</mode>
      <streetLegs>
        <streetLeg>
          <streetName>Northwest 57th Street</streetName>
          <path>uq}aHv{~iV?eA</path>
          <distance>26.498495129442432</distance>
        </streetLeg>
        <streetLeg>...</streetLeg>
      </streetLegs>
    </leg>

Now for a transit leg:

    <leg>
      <startTime>1296507201000</startTime>
      <endTime>1296508308500</endTime>
      <distance>95938.29255425672</distance>
      <mode>transit</mode>
      <transitLeg>
        <tripId>1_15435926</tripId>
        <serviceDate>1296460800000</serviceDate>
        <fromStopId>1_19360</fromStopId>
        <fromStopSequence>12</fromStopSequence>
        <toStopId>...</toStopId>
        <toStopSequence>...</toStopSequence>
        <path>
    gp}aHny~iVbB?|CA?sF?cI?q@HK~IaLd@i@nEwFFGDEbCyCDG@CtCmD
    \m@Ti@BGFWJo@Hs@Bw@?k@?gFH?fCBpC?@Cr@Gb@G^KD?H?`A?dA?j@?dB?dB?
    d@Af@?f@?rC?lA?L?tH@L?nB?l@?Z?^Aj@?b@?r@?b@?r@?|D@lDPzCMfA
    \hC^hC`@f@FbE?fB?|AY|@Ir@MdAa@lAIjCIF?x@CZ?n@?R?zHAD?T?Z?bAAjA@nB?
    fD?xKAdB?zA?xBC`A?pC?hFA|A?tC?J?F?P?d@?b@EZIh@Sb@W
    \YTYBALSnCmDxEsGLQdI}KNUbB}Bt@eANWb@m@bBiC
    \i@r@eABeAj@aC~@sCnAmERs@^uANi@HQFMLSr@y@^e@JQDGBM@KBU?mA?cC@_C?
    aC@cC?cC?aC?_C@cC?_C?cCdG?hFBfF?hF@fFB@{B?iC\w@\o@
        </path>
        <scheduledDepartureTime>1296507201000</scheduledDepartureTime>
        <predictedDepartureTime>0</predictedDepartureTime>
        <scheduledArrivalTime>1296508308500</scheduledArrivalTime>
        <predictedArrivalTime>0</predictedArrivalTime>
      </transitLeg>
    </leg>

## Request Parameters

* latFrom,lonFrom - the lat,lon of the origin point
* latTo,lonTo - the lat,lon of the destination point
* time - the time you wish to plan your trip from (default=NOW)
* arriveBy - if set to true, the `time` param is the target time to arrive at your destination by
* resultCount - how many different itineraries you would like
* useRealTime - whether real-time tracking info should be used when planning
* mode=walk - restrict the set of modes used when planning.  Mostly useful to include a walking-only trip.  Can be specified multiple times to specify multiple modes (ex. `mode=walk&mode=transit`)
* walkSpeed - walking speed in meters per second (default=1.33)
* walkReluctance - how much you dislike walking, see discussion below (default = 2.2)
* maxWalkingDistance - how far you are willing to walk
* initialWaitReluctance - how much you dislike the initial wait before you start your trip, see discussion below (default=0.1)
* waitReluctance - how much you dislike waiting for public transit (default = 2.5)
* minTransferTime - minimum time, in seconds, to require when transferring from one transit vehicle to another.  This time is in addition to any time that may be required to walk to a different stop.
* transferCost - how much to penalize a transfer, as time in seconds, see discussion below (default=14 mins)
* maxTransfers - maximum number of transfers to allow

## Response

The root entry contains information about the origin and destination in the `<from/>` and `<to/>` elements.  However, the most interesting content is in the `<itineraries/>` list.  Each `<itinerary/>` contained within has a stat and end time (ms since unix epoch) and a number of `<leg/>` elements that capture the distinct legs of a trip: walking, transit, walking, etc.

The `<leg/>` element has a start and end time that captures its portion of the full itinerary, as well as a `mode` property that describes the type of leg.  For now, it will either be `walk` or `transit`.

In the case of a `walk` leg, a `<walkLegs/>` element will be present in the leg that captures the sequence of individual `<walkLeg/>` segments of a walk.  Each `<walkLeg/>` has a `streetName` that captures the name of the street, a `path` encoded-polyline of the shape of the walk segment, and a `distance` of the walk leg.

In the case of a `transitLeg`, a single `<transitLeg/>` will be present in the leg that captures the transit-specific details of the leg.  The `tripId` references the [`<trip/>` element](../elements/trip.html) in the `<references/>` section.  The `serviceDate` captures the service date of the trip (see the [Glossary#Service_Date glossary entry]).  The `fromStopId` and `toStopId` reference the [`<stop/>` elements](../elements/stop.html) for the stops to board and alight from.

Note that these elements may be missing in the case of an interlined-route, where the route number switches mid-operation, but the rider should just remain on-board.  In such a case, the trip will be modeled as two separate `<leg/>` elements, with the `toStopId` missing from the first leg and the `fromStopId` missing from the second leg.

The `path` captures an encoded-polyline of the path the trip takes, where available.

The `scheduledDepartureTime` and `scheduledArrivalTime` capture the schedule departure and arrival times for the segment.  As we begin to introduce real-time functionality into the trip planner, the `predictedDepartureTime` and `predictedArrivalTime` will eventually capture the predicted departure and arrival time.  For now, however, they are zero.

## Tuning Your Trip

Determining the *best* trip between two locations is a subjective art.  Often two people will disagree on the suitability of a given itinerary based on their preferences around walking, waiting, transferring and other factors.  As such, we give you a lot of power to tweak the results returned by our trip planner, though we strive to return reasonable defaults as well.  In any case, it is helpful to understand just how we go about scoring potential trips.

A typical trip can be broken up into a number of distinct phases:

* the initial wait: the time between when you plan your trip and when you walk out the door
* the walk to the stop
* some minimal wait time before your bus arrives
* riding the bus
* arrival at a stop
* walk to a different stop
* wait for your next bus
* riding the bus
* arrival at a stop
* walk to your destination

While that trip has total cumulative time, we often talk about the perceived time of a trip.  That is to say, the time you spend waiting at the stop for a bus feels different than the time you spend riding a bus.  In fact, research has show that five minutes spent waiting for a bus feels more like ten minutes.  Similarly, walking to a stop and transfers both have perceived time penalties as well.  We can actually construct a simple equation for perceived wait time:

    T_perceived = initialWaitReluctance * initialWaitTime +
                  walkReluctance * walkTime +
                  waitReluctance * waitTime +
                  vehicleTime +
                  numOfTransfer * transferCost

By setting all the parameters to 1.0, you can achieve a perceived time function that matches actual time.  However, you can adjust the parameters relative to each other to achieve a trip that favors lots of walking vs transfers vs a desire to avoid long waits.  Simply increase the parameter to increase the pain of a particular trip segment.

How did we pick our default values?  Transportation researches have done broad surveys of riders to estimate these biases.  We use the summary statistics from 'Transit Capacity and Quality of Service Manual' - Part 3 - Exhibit 3.9, available at:

http://onlinepubs.trb.org/Onlinepubs/tcrp/tcrp100/part%203.pdf
