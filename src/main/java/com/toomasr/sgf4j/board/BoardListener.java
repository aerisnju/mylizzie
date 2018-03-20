package com.toomasr.sgf4j.board;

import com.toomasr.sgf4j.parser.GameNode;

public interface BoardListener {
  void placeStone(int x, int y, StoneState color);
  void removeStone(int x, int y);
  void playMove(GameNode node, GameNode prevMove);
  void undoMove(GameNode currentMove, GameNode prevMove);
  void initInitialPosition();
}
