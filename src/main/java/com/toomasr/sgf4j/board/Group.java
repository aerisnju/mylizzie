package com.toomasr.sgf4j.board;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Group {
  public Set<Square> stones = new HashSet<Square>();

  public void addStone(Square sq) {
    stones.add(sq);
  }

  public boolean isEmpty() {
    return stones.size() == 0;
  }

  public boolean contains(Square square) {
    return stones.contains(square);
  }

  public boolean isDead(Square[][] board) {
    for (Iterator<Square> ite = stones.iterator(); ite.hasNext();) {
      Square square = ite.next();
      if (square.x - 1 > -1 && board[square.x - 1][square.y].isEmpty())
        return false;
      if (square.x + 1 < board.length && board[square.x + 1][square.y].isEmpty())
        return false;
      if (square.y + 1 < board[square.x].length && board[square.x][square.y + 1].isEmpty())
        return false;
      if (square.y - 1 > -1 && board[square.x][square.y - 1].isEmpty())
        return false;
    }
    return true;
  }

  public void printGroup() {
    System.out.println("Print group of size " + stones.size());
    Square[][] board = new Square[19][19];

    for (Iterator<Square> ite = stones.iterator(); ite.hasNext();) {
      Square square = ite.next();
      board[square.x][square.y] = square;
    }

    for (int i = 0; i < 19; i++) {
      for (int j = 0; j < 19; j++) {
        if (board[i][j] == null) {
          board[i][j] = new Square(StoneState.EMPTY, i, j);
        }
        System.out.print(board[i][j]);
      }
      System.out.println();
    }
  }

  public boolean isDead(VirtualBoard brd) {
    return isDead(brd.getBoard());
  }

  public String toString() {
    return "Size="+stones.size();
  }
}
