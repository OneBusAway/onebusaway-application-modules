[Back to API parent page](../index.html)

# Method: agency-with-coverage

Returns a list of all transit agencies currently supported by OneBusAway along with the center of their coverage area.

## Sample Request

http://api.pugetsound.onebusaway.org/api/where/agencies-with-coverage.xml?key=TEST

## Sample Response

    <response>
      <version>2</version>
      <code>200</code>
      <text>OK</text>
      <currentTime>1270614730908</currentTime>
      <data class="listWithReferences">
        <references>...</references>
        <list>
          <agencyWithCoverage>
            <agencyId>3</agencyId>
            <lat>47.21278384769539</lat>
            <lon>-122.45624875362905</lon>
            <latSpan>0.3559410000000014</latSpan>
            <lonSpan>0.9080050000000028</lonSpan>
          </agencyWithCoverage>
          <agencyWithCoverage>...</agencyWithCoverage>
       </list>
        <limitExceeded>false</limitExceeded>
      </data>
    </response>

## Response

The response has the following fields:

* `agencyId` - an agency id for the agency whose coverage is included.  Should match an [`<agency/>` element](../elements/agency.html) referenced in the `<references/>` section. 
* `lat` and `lon` - indicates the center of the agency's coverage area
* `latSpan` and `lonSpan` - indicate the height (lat) and width (lon) of the coverage bounding box for the agency.