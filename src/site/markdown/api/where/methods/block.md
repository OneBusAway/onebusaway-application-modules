[Back to API parent page](../index.html)

# Method: block

Get details of a specific block by id

## Sample Request

http://api.pugetsound.onebusaway.org/api/where/block/1_5678585.xml?key=TEST

## Sample Response

    <response>
      <version>2</version>
      <code>200</code>
      <text>OK</text>
      <currentTime>1391465493476</currentTime>
      <data class="entryWithReferences">
        <references />
        <entry class="block">
          <id>MTA NYCT_GH_A4-Sunday_D_GH_21000_BX12-15</id>
          <configurations>
            <blockConfiguration>
              <!-- See documentation for the blockConfiguration element, linked below -->
            </blockConfiguration>
          </configurations>
        </entry>
      </data>
    </response>

## Request Parameters

* id - the id of the block, encoded directly in the url:
    * `http://api.pugetsound.onebusaway.org/api/where/block/[ID GOES HERE].xml`

## Response

See details about the various properties of the [`<blockConfiguration/>` element](../elements/block-configuration.html).
