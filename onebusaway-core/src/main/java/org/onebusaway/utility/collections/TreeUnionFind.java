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
package org.onebusaway.utility.collections;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Simple tree-based union find data structure.
 * 
 * @author bdferris
 */
public class TreeUnionFind<T> {

  private Map<T, SentryImpl> _elementToSentry = new HashMap<T, SentryImpl>();

  public interface Sentry {

  }

  public boolean contains(T target) {
    return _elementToSentry.containsKey(target);
  }

  public Sentry find(T target) {
    SentryImpl s = _elementToSentry.get(target);

    if (s == null) {
      s = new SentryImpl(target);
      _elementToSentry.put(target, s);
    }

    return compress(s);
  }

  @SuppressWarnings("unchecked")
  public Set<T> members(Sentry sentry) {
    Set<T> m = new HashSet<T>();
    addElements((SentryImpl) sentry, m);
    return m;
  }

  public Sentry union(T a, T b) {
    return unionWithSentries(find(a), find(b));
  }

  public Sentry unionWithSentry(Sentry sentry, T value) {
    return unionWithSentries(sentry, find(value));
  }

  @SuppressWarnings("unchecked")
  public Sentry unionWithSentries(Sentry a, Sentry b) {

    SentryImpl sa = (SentryImpl) a;
    SentryImpl sb = (SentryImpl) b;

    if (sa == sb)
      return sa;

    if (Math.random() < 0.5) {
      sa.root = sb;
      sb.children.add(sa);
      return sb;
    } else {
      sb.root = sa;
      sa.children.add(sb);
      return sa;
    }
  }

  public Collection<Sentry> getSets() {

    Set<Sentry> retro = new HashSet<Sentry>();

    for (SentryImpl sentry : _elementToSentry.values()) {
      SentryImpl root = compress(sentry);
      retro.add(root);
    }

    return retro;
  }

  public Iterable<Set<T>> getSetMembers() {
    return new Iterable<Set<T>>() {
      public Iterator<Set<T>> iterator() {
        return new SetIterator();
      }
    };
  }

  public int size() {
    return _elementToSentry.size();
  }

  public boolean isEmpty() {
    return _elementToSentry.isEmpty();
  }

  public Set<T> getElements() {
    return Collections.unmodifiableSet(_elementToSentry.keySet());
  }

  public Iterator<T> iterator() {
    return getElements().iterator();
  }

  public boolean isSameSet(T a, T b) {
    if (a.equals(b))
      return true;
    if (!(contains(a) && contains(b)))
      return false;
    Sentry sa = find(a);
    Sentry sb = find(b);
    return sa != null && sb != null && sa.equals(sb);
  }

  /*******************************************************************************************************************
   * Private Methods
   ******************************************************************************************************************/

  private SentryImpl compress(SentryImpl sentry) {
    if (sentry.root == sentry)
      return sentry;
    SentryImpl root = compress(sentry.root);
    sentry.root = root;
    return root;
  }

  private void addElements(SentryImpl sentry, Set<T> elements) {
    elements.addAll(sentry.elements);
    for (SentryImpl s : sentry.children)
      addElements(s, elements);
  }

  /*******************************************************************************************************************
   * Sentry Impl
   ******************************************************************************************************************/

  private class SentryImpl implements Sentry {

    public SentryImpl root;

    public Set<T> elements = new HashSet<T>();

    public Set<SentryImpl> children = new HashSet<SentryImpl>();

    public SentryImpl(T target) {
      elements.add(target);
      root = this;
    }
  }

  private class SetIterator implements Iterator<Set<T>> {

    private Iterator<Sentry> _sentries;

    public SetIterator() {
      _sentries = getSets().iterator();
    }

    public boolean hasNext() {
      return _sentries.hasNext();
    }

    public Set<T> next() {
      return members(_sentries.next());
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

}
