package com.toomasr.sgf4j.parser;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toomasr.sgf4j.Sgf;

public class Game {
  private static final Logger log = LoggerFactory.getLogger(Game.class);

  private Map<String, String> properties = new HashMap<String, String>();
  private GameNode rootNode;
  private int noMoves = 0;
  private int noNodes = 0;

  // great for debugging
  private String originalSgf = null;

  public Game() {
  }

  public Game(String originalSgf) {
    this.originalSgf = originalSgf;
  }

  public void addProperty(String key, String value) {
    /* Actually properties can be set multiple times and it seems based on
     * other software that the expectation is that everything is appended rather
     * than the last definition wins.
     */
    if (properties.get(key) != null) {
      String current = properties.get(key);
      properties.put(key, current+","+value);
    }
    else {
      properties.put(key, value);
    }
  }

  public String getProperty(String key) {
    return properties.get(key);
  }

  public String getProperty(String key, String defaultValue) {
    if (properties.get(key) == null) {
      return defaultValue;
    }
    else {
      return properties.get(key);
    }
  }

  public Map<String, String> getProperties() {
    return new HashMap<String, String>(this.properties);
  }

  public String toString() {
    return properties.toString();
  }

  public void setRootNode(GameNode rootNode) {
    this.rootNode = rootNode;
  }

  public GameNode getRootNode() {
    return rootNode;
  }

  public int getNoMoves() {
    return noMoves;
  }

  public void setNoMoves(int noMoves) {
    this.noMoves = noMoves;
  }

  public void postProcess() {
    // make sure we have a empty first node
    if (getRootNode().isMove()) {
      GameNode oldRoot = getRootNode();
      GameNode newRoot = new GameNode(null);

      newRoot.addChild(oldRoot);
      setRootNode(newRoot);
    }

    // count the moves & nodes
    GameNode node = getRootNode();
    do {
      if (node.isMove()) {
        noMoves++;
      }
      noNodes++;
    }
    while (((node = node.getNextNode()) != null));

    // number all the moves
    numberTheMoves(getRootNode(), 1, 0);

    // calculate the visual depth
    VisualDepthHelper helper = new VisualDepthHelper();
    helper.calculateVisualDepth(getLastMove(), 1);
  }

  private void numberTheMoves(GameNode startNode, int moveNo, int nodeNo) {
    GameNode node = startNode;
    int nextMoveNo = moveNo;
    int nextNodeNo = nodeNo;

    if (node.isMove()) {
      startNode.setMoveNo(moveNo);
      nextMoveNo++;
    }

    startNode.setNodeNo(nodeNo);
    nextNodeNo++;

    if (node.getNextNode() != null) {
      numberTheMoves(node.getNextNode(), nextMoveNo, nextNodeNo);
    }

    if (node.hasChildren()) {
      for (Iterator<GameNode> ite = node.getChildren().iterator(); ite.hasNext();) {
        GameNode childNode = ite.next();
        numberTheMoves(childNode, nextMoveNo, nextNodeNo);
      }
    }
  }

  public int getNoNodes() {
    return noNodes;
  }

  public GameNode getFirstMove() {
    GameNode node = getRootNode();

    do {
      if (node.isMove())
        return node;
    }
    while ((node = node.getNextNode()) != null);

    return null;
  }

  public GameNode getLastMove() {
    GameNode node = getRootNode();
    GameNode rtrn = null;
    do {
      if (node.isMove()) {
        rtrn = node;
      }
    }
    while ((node = node.getNextNode()) != null);
    return rtrn;
  }

  public void saveToFile(Path path) {
    Sgf.writeToFile(this, path);
  }

  public boolean isSameGame(Game otherGame) {
    if (this.equals(otherGame))
      return true;

    // all root level properties have to match
    Map<String, String> reReadProps = otherGame.getProperties();
    if (properties.size() != reReadProps.size()) {
      log.debug("Properties mismatch {} {}", properties.size(), otherGame.getProperties().size());
      return false;
    }

    for (Iterator<Map.Entry<String, String>> ite = properties.entrySet().iterator(); ite.hasNext();) {
      Map.Entry<String, String> entry = ite.next();
      if (!entry.getValue().equals(reReadProps.get(entry.getKey()))) {
        log.debug("Property mismatch {}={} {}", entry.getKey(), entry.getValue(), reReadProps.get(entry.getKey()));
        return false;
      }
    }

    // same number of nodes?
    if (this.getNoNodes() != otherGame.getNoNodes()) {
      log.debug("Games have different no of nodes {} {}", this.getNoNodes(), otherGame.getNoNodes());
      return false;
    }

    // same number of moves?
    if (this.getNoMoves() != otherGame.getNoMoves()) {
      log.debug("Games have different no of moves {} {}", this.getNoMoves(), otherGame.getNoMoves());
      return false;
    }

    // alrighty, lets check alllllll the moves
    boolean allSame = compareAllNodes(this, this.getRootNode(), otherGame, otherGame.getRootNode());
    if (!allSame) {
      return false;
    }

    return true;
  }

  private boolean compareAllNodes(Game game, GameNode node, Game otherGame, GameNode otherNode) {
    if (!node.equals(otherNode)) {
      return false;
    }

    GameNode nextNode = node.getNextNode();
    GameNode nextOtherNode = otherNode.getNextNode();
    if (nextNode != null) {
      compareAllNodes(game, nextNode, otherGame, nextOtherNode);
    }
    // if nextNode is null lets make sure the other one is too
    else if (nextNode != nextOtherNode) {
      return false;
    }

    Set<GameNode> children = node.getChildren();
    Set<GameNode> otherChildren = otherNode.getChildren();

    if (!children.equals(otherChildren)) {
      return false;
    }

    Iterator<GameNode> ite = children.iterator();
    Iterator<GameNode> otherIte = otherChildren.iterator();
    for (; ite.hasNext();) {
      GameNode childNode = ite.next();
      GameNode otherChildNode = otherIte.next();
      if (!compareAllNodes(game, childNode, otherGame, otherChildNode)) {
        return false;
      }
    }

    return true;
  }

  public String getOriginalSgf() {
    return originalSgf;
  }

  public void setOriginalSgf(String originalSgf) {
    this.originalSgf = originalSgf;
  }
}
