[Back to API parent page](../index.html)

# The &lt;agency/&gt; Element

The `<agency/>` element models agencies in OneBusAway.  Agencies are directly mapped from entries in the [GTFS agency.txt](http://code.google.com/transit/spec/transit_feed_specification.html#agency_txt___Field_Definitions) file from the GTFS feeds that power an API instance.

## Example

    <agency>
      <id>1</id>
      <name>Metro Transit</name>
      <url>America/Los_Angeles</url>
      <timezone>America/Los_Angeles</timezone>
      <lang>en</lang>
      <phone>206-553-3000</phone>
      <disclaimer>Transit scheduling, geographic, and real-time data provided by permission of King County</disclaimer>
    </agency>

## Details

The fields of the agency element closely match the fields defined for agencies in the [GTFS spec](http://code.google.com/transit/spec/transit_feed_specification.html#agency_txt___Field_Definitions).

A few important details:

* The only fields that are required are `id`, `name`, `url`, and `timezone`.
* The disclaimer field is an additional field that includes any legal disclaimer that transit agencies would like displayed to users when using the agency's data in an application.