package com.toomasr.sgf4j.board;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.toomasr.sgf4j.parser.GameNode;
import com.toomasr.sgf4j.parser.Util;

public class VirtualBoard {
  private int size = 19;
  private Square[][] vBoard = new Square[size][size];
  private List<BoardListener> boardListeners = new ArrayList<>();
  private Map<GameNode, Set<Group>> moveToRemovedGroups = new HashMap<>();

  public VirtualBoard() {
    initEmptyBoard();
  }

  private void initEmptyBoard() {
    for (int i = 0; i < vBoard.length; i++) {
      for (int j = 0; j < vBoard.length; j++) {
        vBoard[i][j] = new Square(StoneState.EMPTY, i, j);
      }
    }
  }

  public void makeMove(GameNode move, GameNode prevMove) {
    // only if the move is not a pass
    if (move.getMoveString() != null && !move.isPass() && !move.isPlacementMove()) {
      int x = move.getCoords()[0];
      int y = move.getCoords()[1];
      this.vBoard[x][y] = new Square(move.getColorAsEnum(), x, y);
      Set<Group> removedGroups = removeDeadGroupsForOppColor(move.getColorAsEnum());
      moveToRemovedGroups.put(move, removedGroups);
      // place the stone on the board
      placeStone(move);
    }

    // play the move fully out with all the bells and whistles
    playMove(move, prevMove);
  }

  public void placeStone(StoneState color, int x, int y) {
    this.vBoard[x][y] = new Square(color, x, y);
    for (Iterator<BoardListener> ite = boardListeners.iterator(); ite.hasNext();) {
      BoardListener boardListener = ite.next();
      boardListener.placeStone(x, y, color);
    }
  }

  public void playMove(GameNode move, GameNode prevMove) {
    for (Iterator<BoardListener> ite = boardListeners.iterator(); ite.hasNext();) {
      BoardListener boardListener = ite.next();
      boardListener.playMove(move, prevMove);
    }
  }

  public void placeStone(Square sq) {
    placeStone(sq.getColor(), sq.x, sq.y);
  }

  /**
   * Place a stone on the board. No dead group
   * handling or nothing. This is good to put
   * stones on the board for any starting position.
   *
   * @param gameNode the node to place
   */
  public void placeStone(GameNode gameNode) {
    placeStone(gameNode.getColorAsEnum(), gameNode.getCoords()[0], gameNode.getCoords()[1]);
  }

  public void placeWhiteStone(int i, int j) {
    placeStone(new Square(StoneState.WHITE, i, j));
  }

  public void placeBlackStone(int i, int j) {
    placeStone(new Square(StoneState.BLACK, i, j));
  }

  public Set<Group> removeDeadGroupsForOppColor(StoneState color) {
    return removeDeadGroups(oppColor(color));
  }

  public Set<Group> removeDeadGroups(StoneState color) {
    Set<Group> groups = findDistinctGroups(color);
    Set<Group> rtrn = new HashSet<>();
    for (Iterator<Group> ite = groups.iterator(); ite.hasNext();) {
      Group group = ite.next();
      if (group.isDead(vBoard)) {
        removeStones(group);
        rtrn.add(group);
      }
    }
    return rtrn;
  }

  public void removeStone(int x, int y) {
    vBoard[x][y] = new Square(x, y);
    for (Iterator<BoardListener> ite = boardListeners.iterator(); ite.hasNext();) {
      BoardListener boardListener = ite.next();
      boardListener.removeStone(x, y);
    }
  }

  public void removeStones(Group group) {
    for (Iterator<Square> ite = group.stones.iterator(); ite.hasNext();) {
      Square square = ite.next();
      removeStone(square.x, square.y);
    }
  }

  protected Set<Group> findDistinctGroups(StoneState color) {
    Set<Square> alreadyChecked = new HashSet<>();

    Set<Group> groups = new HashSet<>();
    Group activeGroup = new Group();
    for (int i = 0; i < vBoard.length; i++) {
      for (int j = 0; j < vBoard[i].length; j++) {
        // we found a group, lets expand this
        if (vBoard[i][j].isOfColor(color) && !alreadyChecked.contains(vBoard[i][j])) {
          populateGroup(i, j, color, activeGroup);
          alreadyChecked.addAll(activeGroup.stones);

          groups.add(activeGroup);
          activeGroup = new Group();
        }
        // alreadyChecked.add(new Square());
      }
    }
    return groups;
  }

  /*
   * Starts from a node and then finds all the connected stones with this group.
   * Basically populates by starting from a single node.
   */
  private void populateGroup(int i, int j, StoneState color, Group activeGroup) {
    if (vBoard[i][j].isOfColor(color) && !activeGroup.contains(vBoard[i][j])) {
      activeGroup.addStone(vBoard[i][j]);
      if (i - 1 > -1)
        populateGroup(i - 1, j, color, activeGroup);
      if (i + 1 < 19)
        populateGroup(i + 1, j, color, activeGroup);
      if (j - 1 > -1)
        populateGroup(i, j - 1, color, activeGroup);
      if (j + 1 < 19)
        populateGroup(i, j + 1, color, activeGroup);
    }
    else {
      return;
    }
  }

  public StoneState oppColor(StoneState color) {
    if (color.equals(StoneState.EMPTY))
      throw new RuntimeException("Wrong argument for oppColor");
    if (color.equals(StoneState.WHITE))
      return StoneState.BLACK;
    else
      return StoneState.WHITE;
  }

  public void printBoard() {
    for (int i = 0; i < vBoard.length; i++) {
      for (int j = 0; j < vBoard[i].length; j++) {
        System.out.print(vBoard[i][j]);
      }
      System.out.println();
    }
  }

  public Square getCoord(int x, int y) {
    return vBoard[x][y];
  }

  public static VirtualBoard setUpFromStringBoard(String board) {
    VirtualBoard rtrn = new VirtualBoard();
    String[] lines = board.split("\\n");
    for (int i = 0; i < lines.length; i++) {
      for (int j = 0; j < lines[i].length(); j++) {
        Square sq = new Square(lines[i].charAt(j), i, j);
        rtrn.placeStone(sq);
      }
    }
    return rtrn;
  }

  public void fastForwardTo(GameNode fwdTo) {
    // the fwdTo could be an element in one of the child nodes
    // it is really difficult to find if we start from the rootNode
    // so lets start from the node itself, go backwards until we
    // find the root node and later on play all the moves until that point
    List<GameNode> movesToPlay = new ArrayList<>();
    GameNode node = fwdTo;
    do {
      //if (node.isMove()) {
        movesToPlay.add(node);
      //}
    }
    while ((node = node.getParentNode()) != null);

    initEmptyBoard();

    for (Iterator<BoardListener> ite = boardListeners.iterator(); ite.hasNext();) {
      BoardListener boardListener = ite.next();
      boardListener.initInitialPosition();
    }

    GameNode prevMove = null;
    // now lets re-play the moves
    for (int i = movesToPlay.size() - 1; i > -1; i--) {
      node = movesToPlay.get(i);
      makeMove(node, prevMove);

      prevMove = node;
    }
  }

  public Square[][] getBoard() {
    return vBoard;
  }

  public void addBoardListener(BoardListener listener) {
    this.boardListeners.add(listener);
  }

  public void undoMove(GameNode moveNode, GameNode prevMove) {
    if (!moveNode.isPass() && !moveNode.isPlacementMove()) {
      String currMoveStr = moveNode.getMoveString();
      int[] moveCoords = Util.alphaToCoords(currMoveStr);
      removeStone(moveCoords[0], moveCoords[1]);
    }

    // if the move that we are taking back happened to remove
    // stones on the board and now the move is undone we need
    // to put those stones back
    Set<Group> removedGroups = moveToRemovedGroups.get(moveNode);
    if (removedGroups != null) {
      for (Iterator<Group> ite = removedGroups.iterator(); ite.hasNext();) {
        Group group = ite.next();
        for (Iterator<Square> ite2 = group.stones.iterator(); ite2.hasNext();) {
          Square square = ite2.next();
          placeStone(square);
        }
      }
    }

    for (Iterator<BoardListener> ite = boardListeners.iterator(); ite.hasNext();) {
      BoardListener boardListener = ite.next();
      boardListener.undoMove(moveNode, prevMove);
    }
  }
}
