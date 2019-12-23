[Back to API parent page](../index.html)

# Method: stops-for-route

Retrieve the set of stops serving a particular route, including groups by direction of travel.  The `stops-for-route` method first and foremost provides a method for retrieving the set of stops that serve a particular route.  In addition to the full set of stops, we provide various "stop groupings" that are used to group the stops into useful collections.  Currently, the main grouping provided organizes the set of stops by direction of travel  for the route.  Finally, this method also returns a set of polylines that can be used to draw the path traveled by the route.

## Sample Request

http://api.pugetsound.onebusaway.org/api/where/stops-for-route/1_100224.xml?key=TEST

## Sample Response

    <response>
        <version>2</version>
        <code>200</code>
        <currentTime>1461443625722</currentTime>
        <text>OK</text>
        <data class="entryWithReferences">
             <references></references>
             <entry class="stopsForRoute">
                  <routeId>1_100224</routeId>
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
                                    <id>0</id>
                                    <name>
                                        <type>destination</type>
                                        <names>
                                            <string>BALLARD WALLINGFORD</string>
                                        </names>
                                    </name>
                                    <stopIds>
                                        <string>1_25240</string> 
                                        <string>...</string> 
                                    </stopIds>
                                    <polylines>...</polylines>
                               </stopGroup>
                           </stopGroups>
                      </stopGrouping>
                  </stopGroupings>
                  <polylines>...</polylines>
             </entry>
        </data>
    </response>

## Request Parameters

* `id` - The route id, encoded directly in the URL:
    * `http://api.pugetsound.onebusaway.org/api/where/stops-for-route/[ID GOES HERE].xml`
* `includePolylines=true|false` = Optional parameter that controls whether polyline elements are included in the response.  Defaults to true.
* `time=YYYY-MM-DD|epoch` = specify the service date explicitly.  Defaults to today.

## Response
