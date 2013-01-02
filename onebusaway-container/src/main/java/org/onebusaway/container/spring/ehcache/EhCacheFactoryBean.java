/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.container.spring.ehcache;

import java.io.IOException;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.config.TerracottaConfiguration;
import net.sf.ehcache.constructs.blocking.BlockingCache;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;
import net.sf.ehcache.constructs.blocking.SelfPopulatingCache;
import net.sf.ehcache.constructs.blocking.UpdatingCacheEntryFactory;
import net.sf.ehcache.constructs.blocking.UpdatingSelfPopulatingCache;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * A Spring {@link FactoryBean} for programmatically creating an EhCache
 * {@link Cache} with standard configuration options, but also progrmattic
 * Terracotta configuration.
 * 
 * A special note about Terracotta config. While you can create all your
 * Terracotta-enabled caches programatically using this FactoryBean, you need to
 * have at least one Terracotta-enabled cache created the old fashioned way
 * (through an {@code ehcache.xml} resource config) so that the
 * {@link CacheManager} will properly enable Terracotta support.
 * 
 * @author bdferris
 * 
 */
public class EhCacheFactoryBean implements FactoryBean<Ehcache>, BeanNameAware,
    InitializingBean {

  protected final Log logger = LogFactory.getLog(getClass());

  private CacheManager cacheManager;

  private String cacheName;

  private int maxElementsInMemory = 10000;

  private int maxElementsOnDisk = 10000000;

  private MemoryStoreEvictionPolicy memoryStoreEvictionPolicy = MemoryStoreEvictionPolicy.LRU;

  private boolean overflowToDisk = true;

  private boolean eternal = false;

  private int timeToLive = 60 * 5;

  private int timeToIdle = 120;

  private boolean diskPersistent = false;

  private int diskExpiryThreadIntervalSeconds = 60 * 5;

  private boolean blocking = false;

  private boolean terracottaClustered = false;

  private CacheEntryFactory cacheEntryFactory;

  private String beanName;

  private Ehcache cache;

  /**
   * Set a CacheManager from which to retrieve a named Cache instance. By
   * default, <code>CacheManager.getInstance()</code> will be called.
   * <p>
   * Note that in particular for persistent caches, it is advisable to properly
   * handle the shutdown of the CacheManager: Set up a separate
   * EhCacheManagerFactoryBean and pass a reference to this bean property.
   * <p>
   * A separate EhCacheManagerFactoryBean is also necessary for loading EHCache
   * configuration from a non-default config location.
   * 
   * @see EhCacheManagerFactoryBean
   * @see net.sf.ehcache.CacheManager#getInstance
   */
  public void setCacheManager(CacheManager cacheManager) {
    this.cacheManager = cacheManager;
  }

  /**
   * Set a name for which to retrieve or create a cache instance. Default is the
   * bean name of this EhCacheFactoryBean.
   */
  public void setCacheName(String cacheName) {
    this.cacheName = cacheName;
  }

  /**
   * Specify the maximum number of cached objects in memory. Default is 10000
   * elements.
   */
  public void setMaxElementsInMemory(int maxElementsInMemory) {
    this.maxElementsInMemory = maxElementsInMemory;
  }

  /**
   * Specify the maximum number of cached objects on disk. Default is 10000000
   * elements.
   */
  public void setMaxElementsOnDisk(int maxElementsOnDisk) {
    this.maxElementsOnDisk = maxElementsOnDisk;
  }

  /**
   * Set the memory style eviction policy for this cache. Supported values are
   * "LRU", "LFU" and "FIFO", according to the constants defined in EHCache's
   * MemoryStoreEvictionPolicy class. Default is "LRU".
   */
  public void setMemoryStoreEvictionPolicy(
      MemoryStoreEvictionPolicy memoryStoreEvictionPolicy) {
    Assert.notNull(memoryStoreEvictionPolicy,
        "memoryStoreEvictionPolicy must not be null");
    this.memoryStoreEvictionPolicy = memoryStoreEvictionPolicy;
  }

  /**
   * Set whether elements can overflow to disk when the in-memory cache has
   * reached the maximum size limit. Default is "true".
   */
  public void setOverflowToDisk(boolean overflowToDisk) {
    this.overflowToDisk = overflowToDisk;
  }

  /**
   * Set whether elements are considered as eternal. If "true", timeouts are
   * ignored and the element is never expired. Default is "false".
   */
  public void setEternal(boolean eternal) {
    this.eternal = eternal;
  }

  /**
   * Set t he time in seconds to live for an element before it expires, i.e. the
   * maximum time between creation time and when an element expires. It is only
   * used if the element is not eternal. Default is 300 seconds.
   */
  public void setTimeToLive(int timeToLive) {
    this.timeToLive = timeToLive;
  }

  /**
   * Set the time in seconds to idle for an element before it expires, that is,
   * the maximum amount of time between accesses before an element expires. This
   * is only used if the element is not eternal. Default is 120 seconds.
   */
  public void setTimeToIdle(int timeToIdle) {
    this.timeToIdle = timeToIdle;
  }

  /**
   * Set whether the disk store persists between restarts of the Virtual
   * Machine. The default is "false".
   */
  public void setDiskPersistent(boolean diskPersistent) {
    this.diskPersistent = diskPersistent;
  }

  /**
   * Set the number of seconds between runs of the disk expiry thread. The
   * default is 300 seconds.
   */
  public void setDiskExpiryThreadIntervalSeconds(
      int diskExpiryThreadIntervalSeconds) {
    this.diskExpiryThreadIntervalSeconds = diskExpiryThreadIntervalSeconds;
  }

  /**
   * Set whether to use a blocking cache that lets read attempts block until the
   * requested element is created.
   * <p>
   * If you intend to build a self-populating blocking cache, consider
   * specifying a {@link #setCacheEntryFactory CacheEntryFactory}.
   * 
   * @see net.sf.ehcache.constructs.blocking.BlockingCache
   * @see #setCacheEntryFactory
   */
  public void setBlocking(boolean blocking) {
    this.blocking = blocking;
  }

  public void setTerracottaClustered(boolean terracottaClustered) {
    this.terracottaClustered = terracottaClustered;
  }

  /**
   * Set an EHCache {@link net.sf.ehcache.constructs.blocking.CacheEntryFactory}
   * to use for a self-populating cache. If such a factory is specified, the
   * cache will be decorated with EHCache's
   * {@link net.sf.ehcache.constructs.blocking.SelfPopulatingCache}.
   * <p>
   * The specified factory can be of type
   * {@link net.sf.ehcache.constructs.blocking.UpdatingCacheEntryFactory}, which
   * will lead to the use of an
   * {@link net.sf.ehcache.constructs.blocking.UpdatingSelfPopulatingCache}.
   * <p>
   * Note: Any such self-populating cache is automatically a blocking cache.
   * 
   * @see net.sf.ehcache.constructs.blocking.SelfPopulatingCache
   * @see net.sf.ehcache.constructs.blocking.UpdatingSelfPopulatingCache
   * @see net.sf.ehcache.constructs.blocking.UpdatingCacheEntryFactory
   */
  public void setCacheEntryFactory(CacheEntryFactory cacheEntryFactory) {
    this.cacheEntryFactory = cacheEntryFactory;
  }

  public void setBeanName(String name) {
    this.beanName = name;
  }

  public void afterPropertiesSet() throws CacheException, IOException {
    // If no CacheManager given, fetch the default.
    if (this.cacheManager == null) {
      if (logger.isDebugEnabled()) {
        logger.debug("Using default EHCache CacheManager for cache region '"
            + this.cacheName + "'");
      }
      this.cacheManager = CacheManager.getInstance();
    }

    // If no cache name given, use bean name as cache name.
    if (this.cacheName == null) {
      this.cacheName = this.beanName;
    }

    // Fetch cache region: If none with the given name exists,
    // create one on the fly.
    Ehcache rawCache = null;
    if (this.cacheManager.cacheExists(this.cacheName)) {
      if (logger.isDebugEnabled()) {
        logger.debug("Using existing EHCache cache region '" + this.cacheName
            + "'");
      }
      rawCache = this.cacheManager.getEhcache(this.cacheName);
    } else {
      if (logger.isDebugEnabled()) {
        logger.debug("Creating new EHCache cache region '" + this.cacheName
            + "'");
      }
      rawCache = createCache();
      this.cacheManager.addCache(rawCache);
    }

    // Decorate cache if necessary.
    Ehcache decoratedCache = decorateCache(rawCache);
    if (decoratedCache != rawCache) {
      this.cacheManager.replaceCacheWithDecoratedCache(rawCache, decoratedCache);
    }
    this.cache = decoratedCache;
  }

  /**
   * Create a raw Cache object based on the configuration of this FactoryBean.
   */
  private Cache createCache() {

    CacheConfiguration config = new CacheConfiguration(this.cacheName,
        this.maxElementsInMemory);
    config.setMemoryStoreEvictionPolicyFromObject(this.memoryStoreEvictionPolicy);

    config.setEternal(this.eternal);
    config.setTimeToLiveSeconds(this.timeToLive);
    config.setTimeToIdleSeconds(this.timeToIdle);
    
    PersistenceConfiguration pc = new PersistenceConfiguration();
    if(this.diskPersistent) 	
    	pc.strategy(PersistenceConfiguration.Strategy.LOCALRESTARTABLE);
    else if(this.overflowToDisk) 
    	pc.strategy(PersistenceConfiguration.Strategy.LOCALTEMPSWAP);
    else
    	pc.strategy(PersistenceConfiguration.Strategy.NONE);

    config.setDiskExpiryThreadIntervalSeconds(this.diskExpiryThreadIntervalSeconds);
    config.setMaxElementsOnDisk(this.maxElementsOnDisk);

    if (this.terracottaClustered) {
      TerracottaConfiguration tcConfig = new TerracottaConfiguration();
      tcConfig.setClustered(true);
      config.terracotta(tcConfig);
    }

    return new Cache(config);
  }

  /**
   * Decorate the given Cache, if necessary.
   * 
   * @param cache the raw Cache object, based on the configuration of this
   *          FactoryBean
   * @return the (potentially decorated) cache object to be registered with the
   *         CacheManager
   */
  protected Ehcache decorateCache(Ehcache cache) {
    if (this.cacheEntryFactory != null) {
      if (this.cacheEntryFactory instanceof UpdatingCacheEntryFactory) {
        return new UpdatingSelfPopulatingCache(cache,
            (UpdatingCacheEntryFactory) this.cacheEntryFactory);
      } else {
        return new SelfPopulatingCache(cache, this.cacheEntryFactory);
      }
    }
    if (this.blocking) {
      return new BlockingCache(cache);
    }
    return cache;
  }

  public Ehcache getObject() {
    return this.cache;
  }

  public Class<?> getObjectType() {
    return (this.cache != null ? this.cache.getClass() : Ehcache.class);
  }

  public boolean isSingleton() {
    return true;
  }

}
