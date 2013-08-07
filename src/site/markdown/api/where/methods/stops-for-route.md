[Back to API parent page](../index.html)

# Method: stops-for-route

Retrieve the set of stops serving a particular route, including groups by direction of travel.  The `stops-for-route` method first and foremost provides a method for retrieving the set of stops that serve a particular route.  In addition to the full set of stops, we provide various "stop groupings" that are used to group the stops into useful collections.  Currently, the main grouping provided organizes the set of stops by direction of travel  for the route.  Finally, this method also returns a set of polylines that can be used to draw the path traveled by the route.

## Sample Request

http://api.pugetsound.onebusaway.org/api/where/stops-for-route/1_44.xml?key=TEST&version=2

## Sample Response

    <response>
      <version>2</version>
      <code>200</code>
      <text>OK</text>
      <currentTime>1270614730908</currentTime>
      <data class="listWithReferences">
        <references>...</references>
        <entry>
         <routeId>1_44</routeId>
         <stopIds>
            <string>1_10911</string>
            <string>...</string>
         </stopIds>
          <stopGroupings>
            <stopGrouping>
              <type>direction</type>
              <ordered>true</ordered>
              <stopGroups>
                <stopGroup>
                  <name>
                    <type>destination</type>
                    <names>
                      <string>Ballard</string>
                    </names>
                  </name>
                  <stopIds>
                    <string>1_29410</string>
                    <string>...</string>
                  </stopIds>
                  <polylines>
                    <encodedPolyline>...</encodedPolyline>
                    <encodedPolyline>...</encodedPolyline>
                  </polylines>
                </stopGroup>
              </stopGroups>
            </stopGrouping>
          </stopGroupings>
          <polylines>
            <encodedPolyline>...</encodedPolyline>
            <encodedPolyline>...</encodedPolyline>
          </polylines>
        </entry>
      </data>
    </response>

## Request Parameters

* `id` - The route id, encoded directly in the url:
    * `http://api.pugetsound.onebusaway.org/api/where/stops-for-route/[ID GOES HERE].xml`
* includePolylines=true|false = Optional parameter that controls whether polyline elements are included in the response.  Defaults to true.

## Response