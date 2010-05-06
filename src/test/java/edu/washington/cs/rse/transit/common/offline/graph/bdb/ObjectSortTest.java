package edu.washington.cs.rse.transit.common.offline.graph.bdb;

import edu.emory.mathcs.backport.java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ObjectSortTest {

  @Test
  public void testMergeSort() {

    List<String> values = new ArrayList<String>();
    for (int i = 0; i < 100; i++)
      values.add(Integer.toString(i));
    Collections.shuffle(values);

    List<String> a = new ArrayList<String>(values);
    List<String> b = new ArrayList<String>(values);

    Collections.sort(a);
    ObjectSort.mergeSort(b);

    Assert.assertEquals(a, b);
  }
}
