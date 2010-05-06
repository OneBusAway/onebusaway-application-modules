package org.onebusaway.gtfs_diff.impl;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs_diff.model.EntityMatch;
import org.onebusaway.gtfs_diff.model.EntityPropertyMismatch;
import org.onebusaway.gtfs_diff.model.GtfsDifferences;
import org.onebusaway.gtfs_diff.model.Match;
import org.onebusaway.gtfs_diff.model.MatchCollection;

import edu.washington.cs.rse.collections.CollectionsLibrary;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractDifferenceServiceImpl {

  protected GtfsDifferences _results;

  protected GtfsRelationalDao _gtfsDao;

  @Autowired
  public void setGtfsDao(GtfsRelationalDao gtfsDao) {
    _gtfsDao = gtfsDao;
  }

  @Autowired
  public void setResults(GtfsDifferences results) {
    _results = results;
  }

  protected <K, V> Map<K, V> mapAndTranlateIds(List<V> values, String property,
      Class<K> propertyType, String modelPrefix) {
    Map<K, V> valuesByProperty = CollectionsLibrary.mapToValue(values,
        property, propertyType);
    return translateIds(valuesByProperty, modelPrefix);
  }

  protected <K, V> Map<K, V> translateIds(Map<K, V> elementsById,
      String modelPrefix) {

    Map<K, V> translated = new HashMap<K, V>();

    for (Map.Entry<K, V> entry : elementsById.entrySet()) {

      K key = _results.translateId(entry.getKey(), modelPrefix);
      // Did the key change?
      if (!key.equals(entry.getKey()))
        translated.put(key, entry.getValue());
    }

    return translated;
  }

  @SuppressWarnings("unchecked")
  protected <T> List<EntityMatch<T>> getEntityMatches(MatchCollection results,
      Class<T> type) {

    List<EntityMatch<T>> entityMatches = new ArrayList<EntityMatch<T>>();

    for (Match match : results.getMatches()) {
      if (match instanceof EntityMatch) {
        EntityMatch<?> entityMatch = (EntityMatch<?>) match;
        Class<? extends Object> typeA = entityMatch.getEntityA().getClass();
        Class<? extends Object> typeB = entityMatch.getEntityB().getClass();
        if (type.isAssignableFrom(typeA) && type.isAssignableFrom(typeB))
          entityMatches.add((EntityMatch<T>) entityMatch);
      }
    }

    return entityMatches;
  }

  protected <T, X extends MatchCollection> List<EntityMatch<T>> getEntityMatches(
      Iterable<X> results, Class<T> type) {
    List<EntityMatch<T>> entityMatches = new ArrayList<EntityMatch<T>>();
    for (MatchCollection collection : results)
      entityMatches.addAll(getEntityMatches(collection, type));
    return entityMatches;
  }

  protected <T> Set<T> getCommonElements(Set<T> keySetA, Set<T> keySetB) {
    Set<T> common = new HashSet<T>(keySetA);
    common.retainAll(keySetB);
    return common;
  }

  protected void computeEntityPropertyDifferences(Object objectA,
      Object objectB, MatchCollection results, String... propertiesToExclude) {

    if (objectA.getClass() != objectB.getClass())
      throw new IllegalArgumentException("class mismatch: "
          + objectA.getClass() + " vs " + objectB.getClass());

    Set<String> toExclude = new HashSet<String>();
    for (String property : propertiesToExclude)
      toExclude.add(property);

    BeanWrapper wrapperA = new BeanWrapperImpl(objectA);
    BeanWrapper wrapperB = new BeanWrapperImpl(objectB);

    for (PropertyDescriptor descriptor : wrapperA.getPropertyDescriptors()) {

      if (toExclude.contains(descriptor.getName()))
        continue;

      Object valueA = wrapperA.getPropertyValue(descriptor.getName());
      Object valueB = wrapperB.getPropertyValue(descriptor.getName());

      if (!equals(valueA, valueB))
        results.addMismatch(new EntityPropertyMismatch(objectA, objectB,
            descriptor.getName()));
    }
  }

  protected boolean equals(Object a, Object b) {

    if (a == null)
      return b == null;

    if (a instanceof AgencyAndId && b instanceof AgencyAndId) {
      a = _results.translateId(a);
      b = _results.translateId(b);
      return a.equals(b);
    } else if (a instanceof IdentityBean && b instanceof IdentityBean) {
      return equals(((IdentityBean<?>) a).getId(),
          ((IdentityBean<?>) b).getId());
    }

    return a.equals(b);
  }
}
