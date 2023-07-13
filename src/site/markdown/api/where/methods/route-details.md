[Back to API parent page](../index.html)

# Method: route-details

Retrieve canonical or ideal patterns and shapes for a specific route by id.

## Sample Request

http://api.acta.onebusaway.org/api/where/route-details/MTASBWY_A.xml?key=TEST (this example isn't public)

## Sample Response

    <response>
        <version>2</version>
        <code>200</code>
        <currentTime>1678308792760</currentTime>
        <text>OK</text>
        <data class="listWithReferences">
          <list>
            <routeGrouping>
              <routeId>
                <agencyId>MTASBWY</agencyId>
                <id>A</id>
              </routeId>
              <stopGroupings>
                <stopGrouping>
                  <type>canonical</type>
                  <ordered>false</ordered>
                    <stopGroups>
                      <stopGroup>
                        <id>0</id>
                        <name>
                        <type>name</type>
                        <names>
                          <string>Inwood-207 St</string>
                        </names>
                        </name>
                        <stopIds>
                          <string>MTASBWY_A02</string>
                          ....
                        </stopIds>
                        <polylines>
                          <encodedPolyline>
                            <points>
                              m`mxFjndbMj....
                            </points>
                          </encodedPolyline>
                        </polylines>
                      </stopGroup>
                    </stopGroups>
                  </stopGrouping>
                </stopGroupings>
            </routeGrouping>
          </list>
          <references>
            ...
          </references>
          <limitExceeded>false</limitExceeded>
        </data>
    </response>

## Request Parameters

* `id` - the id of the route, encoded directly in the URL:
    * `http://api.acta.onebusaway.org/api/where/route-details/[ID GOES HERE].xml`

## Response

The response is inspired by the StopsForRouteBean though the information may flow from GTFS and GTFS extension sources.

Three distinct types of StopGroups are currently possible:
* **direction**: the typical OneBusAway method of representing a GTFS route via destination directions
* **heuristic**: like direction above, but synthetically generated to present an ideal view of the route
* **canonical**: an ideal view of the route for stop map or strip map purposes.

