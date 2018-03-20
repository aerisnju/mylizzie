package com.toomasr.sgf4j.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Computes the visual depth for the game nodes. The visual
 * depth designates as how deep in the tree should the line be
 * shown if a GUI is being used. I should move this helper to
 * sgf4j-gui project but haven't done it yet.
 */
public class VisualDepthHelper {

  private List<List<Integer>> depthMatrix;

  /*
   * We'll start iterating over the nodes from last to first and
   * calculate how "deep" should they be displayed in the tree. The
   * mainline is on depth 0.
   *
   * @param variationDepth depth of the variation for the first level child
   * of the line of play.
   */
  public void calculateVisualDepth(GameNode lastNode, int variationDepth) {
    // if there are no moves, for example we are just
    // looking at a problem then we can skip calculating
    // the visual depth
    if (lastNode == null) {
      return;
    }

    // a XyZ matrix that we'll fill with 1s and 0s based
    // on whether a square is occupied or not
    List<List<Integer>> depthMatrix = new ArrayList<>();

    initializeMainLine(lastNode, depthMatrix);

    GameNode activeNode = lastNode;
    do {
      if (activeNode.hasChildren()) {
        for (Iterator<GameNode> ite = activeNode.getChildren().iterator(); ite.hasNext();) {
          // the do/while iterates over the main line that has depth 0
          // all other branches have to be at least depth 1
          variationDepth = variationDepth + 1;
          calculateVisualDepthFor(ite.next(), depthMatrix, 1, variationDepth);
        }
      }
    }
    while ((activeNode = activeNode.getPrevNode()) != null);
    this.depthMatrix = depthMatrix;
  }

  private void calculateVisualDepthFor(GameNode node, List<List<Integer>> depthMatrix, int minDepth, int variationDepth) {
    int depth = findVisualDepthForNode(node, depthMatrix, minDepth, variationDepth);
    GameNode lastNodeInLine = setVisualDepthForLine(node, depth);

    GameNode activeNode = lastNodeInLine;
    do {
      if (activeNode.hasChildren()) {
        for (Iterator<GameNode> ite = activeNode.getChildren().iterator(); ite.hasNext();) {
          calculateVisualDepthFor(ite.next(), depthMatrix, depth + 1, variationDepth);
        }
      }
      if (activeNode.equals(node)) {
        break;
      }
    }
    while ((activeNode = activeNode.getPrevNode()) != null);
  }

  private void initializeMainLine(GameNode lastNode, List<List<Integer>> depthMatrix) {
    // initialize the first line with 0s
    List<Integer> firstLine = new ArrayList<>();
    for (int i = 0; i <= lastNode.getMoveNo(); i++) {
      firstLine.add(i, 0);
    }
    depthMatrix.add(0, firstLine);

    // initialize the first line actual moves with 1s
    GameNode node = lastNode;
    do {
      if (node.isMove()) {
        firstLine.set(node.getMoveNo(), 1);
        // main line will be at depth 0
        node.setVisualDepth(0);
      }
    }
    while ((node = node.getPrevNode()) != null);
  }

  protected GameNode setVisualDepthForLine(GameNode child, int depth) {
    GameNode node = child;
    GameNode rtrn = child;
    do {
      node.setVisualDepth(depth);
      rtrn = node;
    }
    while ((node = node.getNextNode()) != null);
    return rtrn;
  }

  protected int findVisualDepthForNode(GameNode node, List<List<Integer>> depthMatrix, int minDepth, int variationDepth) {
    int length = findLengthOfLine(node);

    int depthDelta = minDepth;

    do {
      // init the matrix at this depth if not yet done
      if (depthMatrix.size() <= depthDelta) {
        for (int i = depthMatrix.size(); i <= depthDelta; i++) {
          depthMatrix.add(i, new ArrayList<Integer>());
        }
      }

      boolean available = isAvailableForLineOfPlay(node, length, depthMatrix, depthDelta, variationDepth);

      if (available) {
        bookForLineOfPlay(node, length, depthMatrix, depthDelta, variationDepth);
        break;
      }
      depthDelta++;
    }
    while (true);

    return depthDelta;
  }

  /*
   * Iterates over the depthMatrix and marks all the needed cells as booked (the number 1).
   */
  protected void bookForLineOfPlay(GameNode node, int length, List<List<Integer>> depthMatrix, int listIndex, int variationDepth) {
    List<Integer> levelList = depthMatrix.get(listIndex);

    int start = 0;
    if (node.getMoveNo() > 0) {
      start = node.getMoveNo() - 1;
    }

    // book the line of play (horizontal line)
    for (int i = start; i < (node.getMoveNo() + length); i++) {
      levelList.set(i, 1);
    }

    // book the "glue" pieces (vertical lines for the connection lines)
    // I'll start from the deep end and book the lines until I see a booked
    // line. I presume the current variation is branching from that found
    // booked line.
    for (int i = listIndex-1; i > 0; i--) {
      List<Integer> tmpLevelList = depthMatrix.get(i);
      expandListIfNeeded(tmpLevelList, start);
      if (tmpLevelList.get(start) == 1)
        break;
      tmpLevelList.set(start, 1);
    }
  }

  private void expandListIfNeeded(List<Integer> tmpLevelList, int upperBound) {
    if (tmpLevelList.size() > upperBound) {
      return;
    }

    for (int i = tmpLevelList.size(); i <= upperBound; i++) {
      tmpLevelList.add(i, 0);
    }
  }

  /*
   * Iterates over the depthMatrix and checks whether we can put the variation on this particular line.
   *
   * Each variation needs room for the actual moves and also one spot for the glue piece (the line designating
   * the parent move). If the variation is quite deep then it will need a glue code piece for each row.
   *
   * This method will analyse the matrix for the availability for these. If found they can be "booked"
   * with the bookForLineOfPlay(). If not found we can just look a level deeper.
   */
  protected boolean isAvailableForLineOfPlay(GameNode node, int length, List<List<Integer>> depthMatrix, int listIndex, int variationDepth) {
    // if the marker row is not initialized yet then lets fill with 0s
    List<Integer> levelList = depthMatrix.get(listIndex);
    // the node.getMoveNo() can be -1 if there are more than 1 "starting positions" in a SGF file
    if (levelList.size() <= node.getMoveNo() || node.getMoveNo() == -1) {
      for (int i = levelList.size(); i <= node.getMoveNo() + length; i++) {
        levelList.add(i, 0);
      }
    }

    // we'll start the search one move earlier as we also
    // want to show to "glue stone"
    Integer marker = 0;
    if (node.getMoveNo() > 1) {
      marker = node.getMoveNo() - 1;
    }

    // we'll look at the array and make sure nobody has booked anything yet
    for (int i = marker; i < node.getMoveNo() + length; i++) {
      Integer localMarker = levelList.get(i);
      if (localMarker == 1) {
        return false;
      }
    }
    return true;
  }

  /*
   * Helper to print out the matrix for debugging purposes.
   */
  public void printDepthMatrix() {
    for (Iterator<List<Integer>> ite = depthMatrix.iterator(); ite.hasNext();) {
      List<Integer> list = ite.next();
      System.out.println(list);
    }
  }

  /**
   * Returns the length of this game line. This is the length with
   * no branch taken into account except the main line for this branch.
   *
   * @param node
   * @return no of moves in the main line from this node
   */
  protected int findLengthOfLine(final GameNode node) {
    GameNode tmpNode = node;
    int i = 0;
    do {
      i++;
    }
    while ((tmpNode = tmpNode.getNextNode()) != null);
    return i;
  }
}
