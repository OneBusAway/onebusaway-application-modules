[Back to API parent page](../index.html)

# The &lt;route/&gt; Element

The `<route/>` element models agencies in OneBusAway.  Routes are directly mapped from entries in the [GTFS routes.txt](http://code.google.com/transit/spec/transit_feed_specification.html#routes_txt___Field_Definitions) file from the GTFS feeds that power an API instance.

## Example

    <route>
      <id>1_48</id>
      <shortName>48</shortName>
      <longName>U Dist/Greenwood</longName> 
      <description>U Dist/Greenwood</description>
      <type>3</type>
      <url>http://metro.kingcounty.gov/tops/bus/schedules/s048_0_.html</url>
      <color>00FFFF</color>
      <textColor>FF0000</textColor>
      <agencyId>1</agencyId>
    </route>

## Details

The fields of the route element closely match the fields defined for routes in the [GTFS spec](http://code.google.com/transit/spec/transit_feed_specification.html#routes_txt___Field_Definitions).

A few important details:

* The only fields that are absolutely required are `id`, `type` and `agencyId`.
* Agencies are not required to specify both a shortName and longName, thought they must specify at least one.  Some will specify one but not the other.  Others will include both.  Confounding matters even more, some agencies don't specify a longName but do specify a description that's effectively a longName.  The result is that care must be taken when constructing a route name by using the information that you're actually given.