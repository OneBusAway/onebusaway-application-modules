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
package org.onebusaway.transit_data_federation.impl.blocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.onebusaway.transit_data_federation.services.transit_graph.BlockEntry;

public class BlockLibrary {

  /****
   * Private Methods
   ****/

  public static boolean isFrequencyBased(BlockEntry blockEntry) {
    return blockEntry.getConfigurations().get(0).getFrequencies() != null;
  }

  public static <T> List<List<T>> createStrictlyOrderedGroups(
      List<T> elements, Comparator<T> looseOrder, Comparator<T> strictOrder) {

    Collections.sort(elements, looseOrder);

    List<List<T>> lists = new ArrayList<List<T>>();

    for (T element : elements) {

      List<T> list = BlockLibrary.getBestList(lists, element, strictOrder);

      list.add(element);
    }

    return lists;
  }

  public static <T> List<T> getBestList(List<List<T>> lists, T element,
      Comparator<T> strictComparator) {

    for (List<T> list : lists) {

      if (list.isEmpty())
        return list;

      T prev = list.get(list.size() - 1);

      int c = strictComparator.compare(prev, element);

      if (c <= 0)
        return list;
    }

    List<T> list = new ArrayList<T>();
    lists.add(list);
    return list;
  }
}
