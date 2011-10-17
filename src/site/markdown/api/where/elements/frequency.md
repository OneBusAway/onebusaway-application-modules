[Back to API parent page](../index.html)

# The &lt;frequency/&gt; Element

The `<frequency/>` element captures information about a trip that uses frequency-based scheduling.  Frequency-based scheduling is where a trip doesn't have specifically scheduled stop times, but instead just a headway specifying the frequency of service (ex. service every 10 minutes).  The `<frequency/>` element can be a sub-element of a number of other elements:

* [arrivalAndDeparture](arrival-and-departure.html)
* [tripStatus](trip-status.html)

## Example

    <frequency>
      <startTime>1289579400000</startTime>
      <endTime>1289602799000</endTime>
      <headway>600</headway>
    </frequency>

## Details

We include three fields:

* `startTime` - the start time (unix timestamp) when the frequency block starts
* `endTime` - the end time (unix timestamp) when the frequency block ends
* `headway` - the frequency of service, in seconds