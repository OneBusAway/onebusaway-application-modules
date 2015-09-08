[Back to API parent page](../index.html)

# The &lt;situation/&gt; Element

Real-time arrival information for public transit vehicles is one of the key pieces of information provided by OneBusAway, but it's only a part of the picture.  Information about cancellations, detours, and explanations for why a transit vehicle is delayed are critical details for riders.  While transit agencies have made some progress in publishing timely emails, SMS, and web notifications about service alert information, we think these approaches are generally too broad.  We want to provide targeted service alerts through OneBusAway that tell you service alert information for just the transit services you are actively using.

## API Representation

We introduce a new `<situation/>` element.  It's closely modeled on the PtSituation model from the [SIRI](http://user47094.vs.easily.co.uk/siri/) Situation Exchange schema.  Let's see an example:

    <situation>
      <id>1_1289972401385</id>
      <creationTime>1289972401385</creationTime>
      <environmentReason>heavySnowFall</environmentReason>
      <summary>
        <value>Snow Reroute</value>
      </summary>
      <description>
        <value>Route diverted from Sandpoint Way and Princeton Ave to Campus Pkwy and University Way</value>
      </description>
      <affects>
        <vehicleJourneys>
          <vehicleJourney>
            <lineId>1_30</lineId>
            <direction>1</direction>
            <calls>
              <call>
                <stopId>1_9980</stopId>
              </call>
              …
          </vehicleJourney>
        </vehicleJourneys>
      </affects>
      <consequences>
        <consequence>
          <condition>diversion</condition>
          <conditionDetails>
            <diversionPath>
              <points>ue}aHt~hiVYxHt@lIxAjD|`@pb@tDbHh@|EHvEU~l@fAfN`C~E|DvDbIvB|NdClMxCbEbA`CxDfB`FLrKsNl]gA{@gPGKjF</points>
            </diversionPath>
            <diversionStopIds>
              <string>1_9972</string>
              <string>1_9974</string>
              ...
            </diversionStopIds>
          </conditionDetails>
        </consequence>
      </consequences>
    </situation>

The `<situation/>` element can be broken up into a couple of relevant sections:

## Base Properties

* `id` - the unique id for this service alert situation
* `creationTime` - Unix timestamp of when this situation was created
* reason for the service alert taken from TPEG codes - only one of these will be set
    * `equipmentReason - ex. engineFailure
    * `environmentReason - ex. heavySnowfall
    * `personnelReason - ex. staffAbsence
    * `miscellaneousReason` - ex. securityAlert
    * `securityAlert` - free text field
* descriptive text fields - each with a sub `<value/>` element, each also optional
    * `summary` - Short summary
    * `description` - Longer description
    * `advice` - Advice to the rider

## Affects

The `<affects/>` element captures information about what transit entities are affected by a particular situation.  Right now it supports two sub-elements:

* `stops` - transit stops
* `vehicleJourneys` - transit vehicle journeys

The `<stops/>` element has `<stop>` sub-elements:

    <stop>
      <stopId>1_75403</stopId>
    </stop>

The `<vehicleJourneys/>` element has `<vehicleJourney/>` sub-elements:

    <vehicleJourney>
      <lineId>1_30</lineId>
      <direction>1</direction>
      <calls>
        <call>
          <stopId>1_9980</stopId>
        </call>
        ...
       </calls>
    </vehicleJourney>

The `<vehicleJourney/>` element has the following properties:

  * `lineId` - this is equivalent to a route id
  * `direction` - an optional direction id specifying the direction of travel
  * `calls` - optional elements specifying specific stops along the vehicle journey that are affected

## Consequences

The `<consquences/>` element captures a list of `<consequence/>` elements that provide details about the consequences of the service alert.  Right now, we mostly use this to share reroute information:

    <consequences>
      <consequence>
        <condition>diversion</condition>
        <conditionDetails>
          <diversionPath>
            <points>ue}aHt~hiVYxHt@lIxAjD|`@pb@tDbHh@|EHvEU~l@fAfN`C~E|DvDbIvB|NdClMxCbEbA`CxDfB`FLrKsNl]gA{@gPGKjF</points>
          </diversionPath>
          <diversionStopIds>
            <string>1_9972</string>
            <string>1_9974</string>
            ...
          </diversionStopIds>
        </conditionDetails>
      </consequence>
    </consequences>

Here we model an adverse weather reroute.  The `<consequence/>` element specifies a `condition` of `diversion` and the supplies optional condition details that indicates the path of the diversion and stop ids along the diverted path.  This extended diversion information is optional.

## Situations in API Methods

The `<situation/>` element will appear in one place: under a `<situations/>` element that is a new addition to the `<references/>` element (see [the main API reference](../index.html)).  Since a situation can potentially be referenced multiple times in an API call, we felt putting situations in the references section was the best way to keep the response concise.

Situations will be referenced by id in API calls.  For now, that primarily means the [arrivals-and-departures-for-stop](../../methods/arrivals-and-departures-for-stop.html) API call.  Situations ids can appear in a number of places, depending on the context of a situation.

### Stop-Specific Situations

If a situation affects a stop directly (as opposed to the routes serving that stop), it will appear directly under the `<entry/>` element:

    <entry class="stopWithArrivalsAndDepartures">
      <stopId>1_75403</stopId>
      <arrivalsAndDepartures>
        <arrivalAndDeparture>...</arrivalAndDeparture>
        ...
      </arrivalsAndDepartures>
      <nearbyStopIds>
        <string>1_75414</string>
      </nearbyStopIds>
      <situationIds>
        <string>1_1289973261968</string>
      </situationIds>
    </entry>

### Trip-Specific Situations

If a situation affects a specific trip or a call (an arrival/departure) at a stop by a specific trip, it will appear in the `<situationIds/>` element of the `<arrivalsAndDepartures/>` element.