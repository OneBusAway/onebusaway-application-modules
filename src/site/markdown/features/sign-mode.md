# OneBusAway Sign Mode

OneBusAway provides a mode optimized for showing real-time arrival information on large public screens, allowing
quick and cheap information displays.

<a href="http://www.flickr.com/photos/atomictaco/6202909370/sizes/m/in/pool-624040@N24/"><img src="Sign.jpeg" /></a>

Photo by Atomic Taco.

To use sign-mode, use the [standard web interface](web.html) to browse to your favorite stop.  The URL for the page
should have form like:

http://your.host-name.com/where/standard/stop.action?id=X_Y

To enable sign mode, simply replace the `standard` part of the URL with `sign`:

http://your.host-name.com/where/sign/stop.action?id=X_Y

There are a number of parameters you can add to the URL to tweak the behavior of the sign:

* `title=...` - By default, the sign uses the current stop name as a title.  This option allows you to override the title.
* `showTitle=false` - Alternatively, you can hide the title completely.
* `route=X_Y` - Specify a route id to indicate that only this route should be show.  Can be repeated.
* `minutesBefore=N` - Excludes vehicles that departed more than N minutes ago. 
* `minutesAfter=N` - Exclude vehicles arriving more than N minutes from now.
* `refresh=N` - Automatically refresh the page every N seconds.