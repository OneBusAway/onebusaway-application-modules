[Back to API parent page](../index.html)

# Method: route

Retrieve info for a specific route by id.

## Sample Request

http://api.pugetsound.onebusaway.org/api/where/route/1_100224.xml?key=TEST

## Sample Response

    <response>
        <version>2</version>
        <code>200</code>
        <currentTime>1461441898217</currentTime>
        <text>OK</text>
        <data class="entryWithReferences">
            <references>
                <agencies>
                    <agency>
                        <id>1</id>
                        <name>Metro Transit</name>
                        <url>http://metro.kingcounty.gov</url>
                        <timezone>America/Los_Angeles</timezone>
                        <lang>EN</lang>
                        <phone>206-553-3000</phone>
                        <privateService>false</privateService>
                    </agency>
                </agencies>
            </references>
            <entry class="route">
                <id>1_100224</id>
                <shortName>44</shortName>
                <description>Ballard - Montlake</description>
                <type>3</type>
                <url>http://metro.kingcounty.gov/schedules/044/n0.html</url>
                <agencyId>1</agencyId>
            </entry>
        </data>
    </response>

## Request Parameters

* `id` - the id of the route, encoded directly in the URL:
    * `http://api.pugetsound.onebusaway.org/api/where/route/[ID GOES HERE].xml`

## Response

See details about the various properties of the [`<route/>` element](../elements/route.html).

