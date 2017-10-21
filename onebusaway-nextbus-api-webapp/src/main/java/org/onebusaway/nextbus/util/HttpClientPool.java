/**
 * Copyright (C) 2017 Cambridge Systematics
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
package org.onebusaway.nextbus.util;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientPool {
	private static Logger _log = LoggerFactory.getLogger(HttpClientPool.class);

	// The thread-safe client.
	private CloseableHttpClient threadSafeClient;
	// The pool monitor.
	private IdleConnectionMonitorThread monitor;

	@PostConstruct
	public void setup() {
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		// Increase max total connection to 200
		cm.setMaxTotal(200);
		// Increase default max connection per route to 200
		cm.setDefaultMaxPerRoute(200);
		// Build the client.
		threadSafeClient = HttpClients.custom().setConnectionManager(cm)
				.build();
		// Start up an eviction thread.
		monitor = new IdleConnectionMonitorThread(cm);
		// Don't stop quitting.
		monitor.setDaemon(true);
		monitor.start();
	}

	public CloseableHttpClient getClient() {
		return threadSafeClient;
	}

	// Watches for stale connections and evicts them.
	private class IdleConnectionMonitorThread extends Thread {
		// The manager to watch.
		private final PoolingHttpClientConnectionManager cm;
		// Use a BlockingQueue to stop everything.
		private final BlockingQueue<Stop> stopSignal = new ArrayBlockingQueue<Stop>(1);

		// Pushed up the queue.
		private class Stop {
			// The return queue.
			private final BlockingQueue<Stop> stop = new ArrayBlockingQueue<Stop>(1);

			// Called by the process that is being told to stop.
			public void stopped() {
				// Push me back up the queue to indicate we are now stopped.
				stop.add(this);
			}

			// Called by the process requesting the stop.
			public void waitForStopped() throws InterruptedException {
				// Wait until the callee acknowledges that it has stopped.
				stop.take();
			}

		}

		IdleConnectionMonitorThread(PoolingHttpClientConnectionManager cm) {
			super();
			this.cm = cm;
		}

		@Override
		public void run() {
			try {
				// Holds the stop request that stopped the process.
				Stop stopRequest;
				// Every 5 seconds.
				while ((stopRequest = stopSignal.poll(5, TimeUnit.SECONDS)) == null) {
					// Close expired connections
					cm.closeExpiredConnections();
					// Optionally, close connections that have been idle too
					// long.
					cm.closeIdleConnections(60, TimeUnit.SECONDS);
					// Look at pool stats.
					_log.trace("Stats: {}", cm.getTotalStats());
				}
				// Acknowledge the stop request.
				stopRequest.stopped();
			} catch (InterruptedException ex) {
				// terminate
			}
		}

		@PreDestroy
		public void shutdown() throws InterruptedException, IOException {
			_log.trace("Shutting down client pool");
			// Signal the stop to the thread.
			Stop stop = new Stop();
			stopSignal.add(stop);
			// Wait for the stop to complete.
			stop.waitForStopped();
			// Close the pool - Added
			threadSafeClient.close();
			// Close the connection manager.
			cm.close();
			_log.trace("Client pool shut down");
		}

	}

	public void shutdown() throws InterruptedException, IOException {
		// Shutdown the monitor.
		monitor.shutdown();
	}

}