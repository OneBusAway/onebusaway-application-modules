/**
 * Copyright (C) 2010 OpenPlans
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
package org.onebusaway.presentation.services.cachecontrol;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.BinaryConnectionFactory;
import net.spy.memcached.MemcachedClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.TimerTask;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public abstract class CacheService<K, V> {

  private static final int DEFAULT_CACHE_TIMEOUT = 30;

  private static final int STATUS_INTERVAL_MINUTES = 1;
  protected static Logger _log = LoggerFactory.getLogger(CacheService.class);
  protected Cache<K, V> _cache;

  private ScheduledFuture<CacheService<K, V>.StatusThread> _statusTask = null;
  @Autowired
  private ThreadPoolTaskScheduler _taskScheduler;

  String addr = "sessions-memcache:11211";
  MemcachedClient memcache;
  protected boolean useMemcached = false;

  protected abstract void refreshCache();

  // proxy to the actual hashing algorithm
  public abstract K hash(Object... factors);

  protected boolean _disabled = false;

  public void setUseMemcached(boolean useIt) {
    this.useMemcached = useIt;
  }
  
  public synchronized void setDisabled(boolean disable) {
    this._disabled = disable;
  }

  public Cache<K, V> getCache() {
    return getCache(DEFAULT_CACHE_TIMEOUT, "GENERIC");
  }

  public Cache<K, V> getCache(int timeout, String type) {
    if (_cache == null) {
      _log.info("creating initial " + type + " cache with timeout " + timeout
          + "...");
      _cache = CacheBuilder.newBuilder().expireAfterWrite(timeout,
          TimeUnit.SECONDS).build();
      _log.info("done");
    }
    if (memcache==null && useMemcached)
    {
      try {
        // TODO this appears to leak connections if addr does not exist
        memcache = new MemcachedClient(
            new BinaryConnectionFactory(),
            AddrUtil.getAddresses(addr));
      } 
      catch (Exception e) {
      }
    }
    if (_disabled)
      _cache.invalidateAll();
    return _cache;
  }

  @SuppressWarnings("unchecked")
  public V retrieve(K key) {
    if (_disabled)
      return null;
    if (useMemcached) {
      try {
        return (V) memcache.get(key.toString());
      } catch (Exception e) {
        toggleCache(false);
      }
    }
    return (getCache() != null ? getCache().getIfPresent(key) : null);
  }

  public void store(K key, V value) {
    store(key, value, DEFAULT_CACHE_TIMEOUT);
  }

  public void store(K key, V value, int timeout) {
    if (_disabled)
      return;
    if (useMemcached) {
      try {
        memcache.set(key.toString(), timeout, value);
        return;
      } catch (Exception e) {
        toggleCache(false);
      }
    }
    getCache().put(key, value);
  }

  public boolean containsKey(K key) {
    if (_disabled)
      return false;
    Cache<K, V> cache = getCache();
    if (useMemcached) {
      try {
        return memcache.get(key.toString()) != null;
      } catch (Exception e) {
        toggleCache(false);
      }
    }
    if (!cache.asMap().containsKey(key)){
      // only attempt to switch to memcached if there is a miss in local cache
      // to minimize memcached connection attempts, saving time per local cache usage
      if (memcache != null && !memcache.getAvailableServers().isEmpty()){
        toggleCache(true);
      }
      return false;
    }
    return true;
  }

  public boolean hashContainsKey(Object... factors) {
    return containsKey(hash(factors));
  }

  public void hashStore(V value, Object... factors) {
    getCache().put(hash(factors), value);
  }

  @SuppressWarnings("unchecked")
  @PostConstruct
  private void startStatusTask() {
    if (_statusTask == null) {
      if (!_disabled) {
        _statusTask = (ScheduledFuture<StatusThread>) _taskScheduler.scheduleWithFixedDelay(new StatusThread(),
                STATUS_INTERVAL_MINUTES * 60 * 1000);

      } else {
        this.logStatus();
      }
    }
  }

  public void logStatus() {
    _log.info(getCache().stats().toString() + "; disabled=" + _disabled
        + "; useMemcached=" + useMemcached
        + "; Local Size=" + _cache.size()
        + "; Memcached Size=" + (memcache==null?"[null]":memcache.getStats("sizes")));
  }

  private class StatusThread extends TimerTask {
    @Override
    public void run() {
      logStatus();
    }
  }
  
  private void toggleCache(boolean useMemcached){
    this.useMemcached = useMemcached;
    _log.info("Caching with " + (useMemcached?"Memcached":"Local Cache"));
  }
}