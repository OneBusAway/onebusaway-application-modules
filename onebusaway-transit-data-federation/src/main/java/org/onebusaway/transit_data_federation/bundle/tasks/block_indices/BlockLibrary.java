package org.onebusaway.transit_data_federation.bundle.tasks.block_indices;

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
