package com.toomasr.sgf4j.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Util {
  //@formatter:off
  public static final String[] alphabet = new String[] { "A", "B", "C", "D"
      , "E", "F", "G", "H", "J"
      , "K", "L", "M", "N", "O"
      ,"P", "Q", "R", "S", "T" };

  public static final Map<String, Integer> alphaToCoord = new HashMap<String, Integer>(){{
    put("a", 0);
    put("b", 1);
    put("c", 2);
    put("d", 3);
    put("e", 4);
    put("f", 5);
    put("g", 6);
    put("h", 7);
    put("i", 8);
    put("j", 9);
    put("k", 10);
    put("l", 11);
    put("m", 12);
    put("n", 13);
    put("o", 14);
    put("p", 15);
    put("q", 16);
    put("r", 17);
    put("s", 18);
    put("t", 19);
  }};

  public static final Map<Integer, String> coordToAlpha = new HashMap<Integer, String>(){{
    put(0, "a");
    put(1, "b");
    put(2, "c");
    put(3, "d");
    put(4, "e");
    put(5, "f");
    put(6, "g");
    put(7, "h");
    put(8, "i");
    put(9, "j");
    put(10, "k");
    put(11, "l");
    put(12, "m");
    put(13, "n");
    put(14, "o");
    put(15, "p");
    put(16, "q");
    put(17, "r");
    put(18, "s");
    put(19, "t");
  }};
  //@formatter:on
  private Util() {
  }

  public static void printNodeTree(GameNode rootNode) {
    if (rootNode.hasChildren()) {
      Set<GameNode> children = rootNode.getChildren();
      for (Iterator<GameNode> ite = children.iterator(); ite.hasNext();) {
        GameNode node = ite.next();
        printNodeTree(node);
      }
    }
  }

  public static int[] alphaToCoords(String input) {
    if (input == null || input.length() < 2) {
      throw new RuntimeException("Coordinate cannot be less than 2 characters. Input '" + input + "'");
    }
    return new int[] { alphaToCoord.get(input.charAt(0) + ""), alphaToCoord.get(input.charAt(1) + "") };
  }

  public static Map<String, String> extractLabels(String str) {
    HashMap<String, String> rtrn = new HashMap<String, String>();
    // the LB property comes like 'fb:A][gb:C][jd:B
    if (str == null)
      return rtrn;
    String[] labels = str.split("\\]\\[");
    for (int i = 0; i < labels.length; i++) {
      String[] label = labels[i].split(":");
      rtrn.put(label[0], label[1]);
    }
    return rtrn;
  }

  public static String[] coordSequencesToSingle(String addBlack) {
    List<String> rtrn = new ArrayList<>();
    String[] blackStones = addBlack.split(",");
    for (int i = 0; i < blackStones.length; i++) {
      if (blackStones[i].contains(":")) {
        String[] seq = blackStones[i].split(":");
        if (seq[0].charAt(0) == seq[1].charAt(0)) {
          for (int j = Util.alphaToCoord.get(seq[0].charAt(1)+""); j <= Util.alphaToCoord.get(seq[1].charAt(1)+""); j++) {
            rtrn.add(seq[0].charAt(0) + coordToAlpha.get(j));
          }
        }
        else {
          for (int j = Util.alphaToCoord.get(seq[0].charAt(0)+""); j <= Util.alphaToCoord.get(seq[1].charAt(0)+""); j++) {
            rtrn.add(coordToAlpha.get(j)+seq[0].charAt(1));
          }
        }
      }
      else {
        rtrn.add(blackStones[i]);
      }
    }
    return rtrn.toArray(new String[] {});
  }
}
