package org.onebusaway.geospatial.grid;

import junit.framework.TestCase;

import java.util.List;

public class BoundaryFactoryTest extends TestCase {

  private static final EDirection DU = EDirection.UP;

  private static final EDirection DL = EDirection.LEFT;

  private static final EDirection DR = EDirection.RIGHT;

  private static final EDirection DD = EDirection.DOWN;

  public void testBoundaryFactory() {
    Grid<String> grid = new MapGrid<String>();
    grid.set(0, 0, "a");
    BoundaryFactory bf = new BoundaryFactory();
    List<Boundary> boundaries = bf.getBoundaries(grid);

    assertEquals(1, boundaries.size());

    Boundary boundary = boundaries.get(0);

    BoundaryPath expected = pathFactory().add(0, 0, DU).add(0, 0, DR).add(0, 0,
        DD).add(0, 0, DL).path();
    BoundaryPath actual = boundary.getOuterBoundary();

    assertTrue(arePathsEquivalent(expected, actual));

    assertTrue(boundary.getInnerBoundaries().isEmpty());
  }

  public void testBoundaryFactoryWithHole() {
    Grid<String> grid = new MapGrid<String>();

    for (int x = 0; x < 6; x++) {
      for (int y = 0; y < 6; y++) {
        if (2 <= x && x < 4 && 2 <= y && y < 4)
          continue;
        grid.set(x, y, "a");
      }
    }

    BoundaryFactory bf = new BoundaryFactory();
    List<Boundary> boundaries = bf.getBoundaries(grid);

    assertEquals(1, boundaries.size());

    Boundary boundary = boundaries.get(0);

    BoundaryPathFactory factory = pathFactory();
    for (int i = 0; i < 6; i++)
      factory.add(0, i, DL);
    for (int i = 0; i < 6; i++)
      factory.add(i, 5, DU);
    for (int i = 5; i >= 0; i--)
      factory.add(5, i, DR);
    for (int i = 5; i >= 0; i--)
      factory.add(i, 0, DD);
    BoundaryPath expected = factory.path();

    BoundaryPath actual = boundary.getOuterBoundary();

    assertTrue(arePathsEquivalent(expected, actual));

    assertEquals(1, boundary.getInnerBoundaries().size());

    BoundaryPath innerExpected = pathFactory().add(2, 1, DU).add(3, 1, DU).add(
        4, 2, DL).add(4, 3, DL).add(3, 4, DD).add(2, 4, DD).add(1, 3, DR).add(
        1, 2, DR).path();
    BoundaryPath innerActual = boundary.getInnerBoundaries().get(0);

    assertTrue(arePathsEquivalent(innerExpected, innerActual));

  }

  public void testBoundaryFactoryTwoClusters() {
    Grid<String> grid = new MapGrid<String>();

    grid.set(0, 0, "a");
    grid.set(0, 1, "a");

    grid.set(0, 3, "b");

    BoundaryFactory bf = new BoundaryFactory();
    List<Boundary> boundaries = bf.getBoundaries(grid);

    assertEquals(2, boundaries.size());

    Boundary ba = boundaries.get(0);
    Boundary bb = boundaries.get(1);

    if (ba.getOuterBoundary().size() < bb.getOuterBoundary().size()) {
      Boundary bt = ba;
      ba = bb;
      bb = bt;
    }

    BoundaryPath expectedPathA = pathFactory().add(0, 0, DR).add(0, 0, DD).add(
        0, 0, DL).add(0, 1, DL).add(0, 1, DU).add(0, 1, DR).path();
    BoundaryPath actualPathA = ba.getOuterBoundary();
    assertTrue(arePathsEquivalent(expectedPathA, actualPathA));

    BoundaryPath expectedPathB = pathFactory().add(0, 3, DL).add(0, 3, DU).add(
        0, 3, DR).add(0, 3, DD).path();
    BoundaryPath actualPathB = bb.getOuterBoundary();
    assertTrue(arePathsEquivalent(expectedPathB, actualPathB));
  }

  public void testClusterWithTwoHoles() {
    Grid<String> grid = new MapGrid<String>();

    grid.set(0, 0, "a");
    grid.set(0, 1, "a");
    grid.set(0, 2, "a");
    grid.set(1, 0, "a");
    grid.set(1, 2, "a");
    grid.set(2, 0, "a");
    grid.set(2, 1, "a");
    grid.set(2, 2, "a");
    grid.set(3, 0, "a");
    grid.set(3, 2, "a");
    grid.set(4, 0, "a");
    grid.set(4, 1, "a");
    grid.set(4, 2, "a");

    BoundaryFactory bf = new BoundaryFactory();
    List<Boundary> boundaries = bf.getBoundaries(grid);
    assertEquals(1, boundaries.size());

    Boundary boundary = boundaries.get(0);

    BoundaryPathFactory factory = pathFactory();
    for (int i = 0; i < 3; i++)
      factory.add(0, i, DL);
    for (int i = 0; i < 5; i++)
      factory.add(i, 2, DU);
    for (int i = 2; i >= 0; i--)
      factory.add(4, i, DR);
    for (int i = 4; i >= 0; i--)
      factory.add(i, 0, DD);

    BoundaryPath expectedOuterPath = factory.path();
    BoundaryPath actualOuterPath = boundary.getOuterBoundary();
    assertTrue(arePathsEquivalent(expectedOuterPath, actualOuterPath));

    List<BoundaryPath> innerBoundaries = boundary.getInnerBoundaries();
    assertEquals(2, innerBoundaries.size());

    BoundaryPath pathA = pathFactory().add(1, 0, DU).add(2, 1, DL).add(1, 2, DD).add(
        0, 1, DR).path();
    BoundaryPath pathB = pathFactory().add(3, 0, DU).add(4, 1, DL).add(3, 2, DD).add(
        2, 1, DR).path();

    BoundaryPath actualPathA = innerBoundaries.get(0);
    BoundaryPath actualPathB = innerBoundaries.get(1);

    if (!arePathsEquivalent(pathA, actualPathA)) {
      BoundaryPath temp = actualPathA;
      actualPathA = actualPathB;
      actualPathB = temp;
    }

    assertTrue(arePathsEquivalent(pathA, actualPathA));
    assertTrue(arePathsEquivalent(pathB, actualPathB));
  }

  public void testPruneAllButCorners() {

    Grid<String> grid = new MapGrid<String>();

    for (int x = 0; x < 6; x++) {
      for (int y = 0; y < 6; y++) {
        if (2 <= x && x < 4 && 2 <= y && y < 4)
          continue;
        grid.set(x, y, "a");
      }
    }

    BoundaryFactory factory = new BoundaryFactory();
    factory.setPruneAllButCorners(true);

    List<Boundary> boundaries = factory.getBoundaries(grid);

    assertEquals(1, boundaries.size());

    Boundary boundary = boundaries.get(0);

    BoundaryPath expectedOuter = pathFactory().add(0, 0, DL).add(0, 5, DU).add(
        5, 5, DR).add(5, 0, DD).path();
    BoundaryPath actualOuter = boundary.getOuterBoundary();
    assertTrue(arePathsEquivalent(expectedOuter, actualOuter));

  }

  /*****************************************************************************
   * Private Methods
   ****************************************************************************/

  private boolean arePathsEquivalent(BoundaryPath a, BoundaryPath b) {

    if (a.size() != b.size())
      return false;

    GridIndex firstIndex = a.getIndex(0);
    EDirection firstDirection = a.getDirection(0);

    for (int offset = 0; offset < b.size(); offset++) {

      if (firstIndex.equals(b.getIndex(offset))
          && firstDirection.equals(b.getDirection(offset))) {

        for (int ia = 0; ia < a.size(); ia++) {
          int ib = (ia + offset) % b.size();
          if (!a.getIndex(ia).equals(b.getIndex(ib)))
            return false;
          if (!a.getDirection(ia).equals(b.getDirection(ib)))
            return false;
        }

        return true;
      }
    }

    return false;
  }

  private BoundaryPathFactory pathFactory() {
    return new BoundaryPathFactory();
  }

  private class BoundaryPathFactory {
    private BoundaryPath _path = new BoundaryPath();

    public BoundaryPathFactory add(int x, int y, EDirection direction) {
      _path.addEdge(new GridIndex(x, y), direction);
      return this;
    }

    public BoundaryPath path() {
      return _path;
    }
  }
}
