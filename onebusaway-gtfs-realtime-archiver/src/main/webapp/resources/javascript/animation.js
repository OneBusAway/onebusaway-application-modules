/*
 * Copyright (C) 2015 Cambridge Systematics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// D3-style object to create and control an animation of the AVL
// data for a particular vehicle.
// map : map or group where animation will be added.
// clock: DOM object where current time should be updated.
// icon: Leaflet icon which will be animated.
function avlAnimation(map, icon, clock) {
	
	var startTime, endTime, rate, currentIndex, elapsedTime,
		lastTime, lineDone, paused, durations, sprite, positions;
	
	var ready = false;
	
	// create icon for animation and initialize values
	// positions is an array of position values: { lat, lon, timestamp }
	function animation(data) {
	
		// remove old sprite.
		if (sprite)
			map.removeLayer(sprite);
		
		positions = data
		
		ready = true;
		startTime = positions[0].timestamp;
		endTime = positions[positions.length-1].timestamp;
		rate = 1;
	
		currentIndex = 0; // this means we're going to 1
	
		elapsedTime = positions[0].timestamp,
			lastTime = 0,
			lineDone = 0;
	
		paused = true;
	
		durations = []
		for (var i = 0; i < positions.length - 1; i++)
			durations.push(positions[i+1].timestamp - positions[i].timestamp);
		
		sprite = L.marker(positions[0], {icon: icon}).addTo(map);
		clock.textContent = parseTime(elapsedTime);
	}
	
	function tick() {
		var now = Date.now(),
			delta = now - lastTime;
		
		lastTime = now;
		
		elapsedTime += delta * rate;
		
		lineDone += delta * rate;
		
		if (lineDone > durations[currentIndex]) {
			// advance index and icon
			currentIndex += 1
			lineDone = 0;
			
			if (currentIndex == positions.length - 1)
				return;
			
			sprite.setLatLng(positions[currentIndex])
			sprite.update()
			elapsedTime = positions[currentIndex].timestamp
		}
		else {
			var pos = interpolatePosition(positions[currentIndex], positions[currentIndex+1], durations[currentIndex], lineDone)
			sprite.setLatLng(pos)
			sprite.update()
			
		}
		clock.textContent = parseTime(elapsedTime);
		
		if (!paused)
			requestAnimationFrame(tick)
	}
	
	animation.ready = function() {
		return ready;
	}
	
	animation.start = function() { 
		lastTime = Date.now();
		paused = false;
		tick();
	}
	
	animation.pause = function() {
		paused = true;
	}
	
	animation.paused = function() {
		return paused;
	}
	
	animation.rate = function(_) {
		if(_)
			rate = _;
		else
			return rate;
	}
	
	// skip to next AVL
	animation.next = function() {
		updateToIndex(currentIndex+1);
	}
	
	// previous AVL
	animation.prev = function() {
		// In most cases, we don't actually want to go *back* an index, just
		// restart this one. Exception: if we are less than 500ms (in realtime)
		// into this avl.
		
		var delta = elapsedTime - positions[currentIndex].timestamp;
		if (delta/rate < 500)
			updateToIndex(currentIndex-1);
		else
			updateToIndex(currentIndex);
	}
		
	function updateToIndex(i) {
		if (i > positions.length - 1)
			i = positions.length - 1;
		if (i < 0)
			i = 0;
		
		currentIndex = i; //+= 1;
		lineDone = 0;
		var avl = positions[currentIndex];
		elapsedTime = avl.timestamp;
		
		// update GUI if tick won't.
		if (paused) {
			sprite.setLatLng(avl);
			sprite.update();
			clock.textContent = parseTime(elapsedTime);
		}
	}
	
	function parseTime(x) {
		return new Date(x).toTimeString().slice(0, 8);
	}
	
	// taken from leafletMovingMarker.js
	var interpolatePosition = function(p1, p2, duration, t) {
	    var k = t/duration;
	    k = (k>0) ? k : 0;
	    k = (k>1) ? 1 : k;
	    return L.latLng(p1.lat + k*(p2.lat-p1.lat), p1.lon + k*(p2.lon-p1.lon));
	};


	return animation;
}
				
	