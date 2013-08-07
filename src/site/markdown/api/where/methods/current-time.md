[Back to API parent page](../index.html)

# Method: current-time

Retrieve the current system time

## Sample Request

http://api.pugetsound.onebusaway.org/api/where/current-time.xml?key=TEST

## Sample Response

    <response>
      <version>2</version>
      <code>200</code>
      <text>OK</text>
      <currentTime>1270614730908</currentTime>
      <data class="time">
        <references/>
        <time>
          <time>1270614730908</time>
          <readableTime>2010-04-06T21:32:10-07:00</readableTime>
        </time>
      </data>
    </response>

## Response

* `time` - current system time as milliseconds since the Unix epoch
* `readableTime` - current system time in [ISO 8601](http://en.wikipedia.org/wiki/ISO_8601) format