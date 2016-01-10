[Back to API parent page](../index.html)

# Method: agency

Retrieve info for a specific transit agency identified by id

## Sample Request

http://api.pugetsound.onebusaway.org/api/where/agency/1.xml?key=TEST

## Sample Response

    <response>
      <version>2</version>
      <code>200</code>
      <text>OK</text>
      <currentTime>1270614730908</currentTime>
      <data class="entryWithReferences">
        <references/>
        <entry class="agency">
          <id>1</id>
          <name>Metro Transit</name>
          <url>America/Los_Angeles</url>
          <timezone>America/Los_Angeles</timezone>
          <lang>en</lang>
          <phone>206-553-3000</phone>
          <disclaimer>Transit scheduling, geographic, and real-time data provided by permission of King County</disclaimer>
        </entry>
      </data>
    </response>

## Request Parameters

* id - the id of the agency, encoded directly in the URL:
    * `http://api.pugetsound.onebusaway.org/api/where/agency/[ID GOES HERE].xml`

### Response

For more details on the fields returned for an agency, see the documentation for the [`<agency/>` element](../elements/agency.html).
