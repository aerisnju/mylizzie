package com.toomasr.sgf4j;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toomasr.sgf4j.parser.Game;
import com.toomasr.sgf4j.parser.GameNode;

public class Parser {
  private static final Logger log = LoggerFactory.getLogger(Parser.class);
  private final String originalGame;

  // http://www.red-bean.com/sgf/properties.html
  private static final Set<String> generalProps = new HashSet<>();

  static {
    // Application used to generate the SGF
    generalProps.add("AP");
    // Black's Rating
    generalProps.add("BR");
    // White's Rating
    generalProps.add("WR");
    // KOMI
    generalProps.add("KM");
    // weird alternative KOMI
    generalProps.add("GKM");
    // Black Player Extended information
    generalProps.add("PBX");
    // Black Player name
    generalProps.add("PB");
    // White Player name
    generalProps.add("PW");
    // I think - Black Player name
    generalProps.add("PX");
    // I think - White Player name
    generalProps.add("PY");
    // Charset
    generalProps.add("CA");
    // File format
    generalProps.add("FF");
    // Game type - 1 means Go
    generalProps.add("GM");
    // Size of the board
    generalProps.add("SZ");
    // Annotator
    generalProps.add("AN");
    // Name of the event
    generalProps.add("EV");
    // Name of the event extended
    // Extended info about the event
    generalProps.add("EVX");
    // Rount number
    generalProps.add("RO");
    // Rules
    generalProps.add("RU");
    // Time limit in seconds
    generalProps.add("TM");
    // How overtime is handled
    generalProps.add("OT");
    // Date of the game
    generalProps.add("DT");
    // Extended date
    generalProps.add("DTX");
    // Place of the game
    generalProps.add("PC");
    // Result of the game
    generalProps.add("RE");
    // I think - Result of the game
    generalProps.add("ER");
    // How to show comments
    generalProps.add("ST");
    /*
     * Provides some extra information about the following game.
     * The intend of GC is to provide some background information
     * and/or to summarize the game itself.
     */
    generalProps.add("GC");
    // Any copyright information
    generalProps.add("CP");
    // Provides name of the source
    generalProps.add("SO");
    // Name of the white team
    generalProps.add("WT");
    // Name of the black team
    generalProps.add("BT");
    // name of the user or program who entered the game
    generalProps.add("US");
    // How to print move numbers
    generalProps.add("PM");
    // Some more printing magic
    generalProps.add("FG");
    // Name of the game
    generalProps.add("GN");
    // Black territory or area
    generalProps.add("TB");
    // White territory or area
    generalProps.add("TW");
    // Sets the move number to the given value, i.e. a move
    // specified in this node has exactly this move-number. This
    // can be useful for variations or printing.
    // SGF4J doesn't honour this atm
    generalProps.add("MN");
    // Handicap stones
    generalProps.add("HA");
    // "AB": add black stones AB[point list]
    generalProps.add("AB");
    // "AW": add white stones AW[point list]
    generalProps.add("AW");
    // add empty = remove stones
    generalProps.add("AE");
    // PL tells whose turn it is to play.
    generalProps.add("PL");
    // KGSDE - kgs scoring - marks all prisoner stones
    // http://senseis.xmp.net/?CgobanProblemsAndSolutions
    generalProps.add("KGSDE");
    // KGS - score white
    generalProps.add("KGSSW");
    // KGS - score black
    generalProps.add("KGSSB");
    // Checkmark - ignored in FF4
    // http://www.red-bean.com/sgf/ff1_3/ff3.html and http://www.red-bean.com/sgf/changes.html
    generalProps.add("CH");
    // I think this is White Country
    generalProps.add("WC");
    // "LT": enforces losing on time LT[]
    // http://www.red-bean.com/sgf/ff1_3/ff3.html
    // I don't get it but I'm parsing it
    generalProps.add("LT");
    // I think this is Black Country
    generalProps.add("BC");
    // I think this is just a game ID
    generalProps.add("ID");
    // I have no idea what these properties means
    // but they are in many games of the collections
    // I've downloaded from the interwebs
    generalProps.add("OH");
    generalProps.add("LC");
    generalProps.add("RD"); // maybe release date?
    generalProps.add("TL"); // something to do with time
    generalProps.add("GK"); // something to do with the game

    // These are also available for nodes!

    // time left for white
    generalProps.add("WL");
    // time left for black
    generalProps.add("BL");

    // Multigo specific properties
    generalProps.add("MULTIGOGM");
    generalProps.add("MULTIGOBM");
    // hotspot - no idea :)
    generalProps.add("HO");
    // some go program info probably
    generalProps.add("GOGGPFF");
    generalProps.add("GOGGPAP");
    // Zen specific
    generalProps.add("ZT");
  }

  private static final Set<String> nodeProps = new HashSet<>();

  static {
    // Move for Black
    nodeProps.add("B");
    // Move for White
    nodeProps.add("W");
    // marks given points with circle
    nodeProps.add("CR");
    // marks given points with cross
    nodeProps.add("MA");
    // marks given points with square
    nodeProps.add("SQ");
    // selected points
    nodeProps.add("SL");
    // labels on points
    nodeProps.add("LB");
    // marks given points with triangle
    nodeProps.add("TR");
    // Number of white stones to play in this byo-yomi period
    nodeProps.add("OW");
    // Number of black stones to play in this byo-yomi period
    nodeProps.add("OB");
    // time left for white
    nodeProps.add("WL");
    // time left for black
    nodeProps.add("BL");
    // Comment
    nodeProps.add("C");
    /*
     * Provides a name for the node. For more info have a look at
     * the C-property.
     */
    nodeProps.add("N");
    /*
     * List of points - http://www.red-bean.com/sgf/proplist_ff.html
     * Label the given points with uppercase letters. Not used in FF 3 and FF 4!
     *
     * Replaced by LB which defines the letters also:
     * Example: L[fg][es][jk] -> LB[fg:A][es:B][jk:C]
     */
    nodeProps.add("L");

    // don't quite get it what it means
    // but lets parse this out
    nodeProps.add("WV");
    // dimmed stones - see http://www.red-bean.com/sgf/DD_VW.html
    nodeProps.add("VW");
    // Tesuji - don't know what to do with it though
    nodeProps.add("TE");
  }

  private Stack<GameNode> treeStack = new Stack<>();

  public Parser(String game) {
    originalGame = game;
  }

  public Game parse() {
    Game game = new Game(originalGame);

    // the root node
    GameNode parentNode = null;
    // replace token delimiters

    int moveNo = 1;

    for (int i = 0; i < originalGame.length(); i++) {
      char chr = originalGame.charAt(i);
      if (';' == chr && (i == 0 || originalGame.charAt(i - 1) != '\\')) {
        String nodeContents = consumeUntil(originalGame, i);
        i = i + nodeContents.length();

        GameNode node = parseToken(nodeContents, parentNode, game);
        if (node.isMove()) {
          node.setMoveNo(moveNo++);
        }

        if (parentNode == null) {
          parentNode = node;
          game.setRootNode(parentNode);
        }
        else if (!node.isEmpty()) {
          parentNode.addChild(node);
          parentNode = node;
        }
      }
      else if ('(' == chr && parentNode != null) {
        treeStack.push(parentNode);
      }
      else if (')' == chr) {
        if (treeStack.size() > 0) {
          parentNode = treeStack.pop();
          moveNo = parentNode.getMoveNo() + 1;
        }
      }
      else {
      }
    }

    return game;
  }

  private String consumeUntil(String gameStr, int i) {
    StringBuffer rtrn = new StringBuffer();
    boolean insideComment = false;
    boolean insideValue = false;
    for (int j = i + 1; j < gameStr.length(); j++) {
      char chr = gameStr.charAt(j);
      if (insideComment) {
        if (']' == chr && gameStr.charAt(j - 1) != '\\') {
          insideComment = false;
        }
        rtrn.append(chr);
      }
      else {
        if ('C' == chr && '[' == gameStr.charAt(j + 1)) {
          insideComment = true;
          rtrn.append(chr);
        }
        else if ('[' == chr) {
          insideValue = true;
          rtrn.append(chr);
        }
        else if (']' == chr) {
          insideValue = false;
          rtrn.append(chr);
        }
        // while inside the value lets consume everything -
        // even chars that otherwise would have special meaning
        // like ;()
        else if (insideValue) {
          rtrn.append(chr);
        }
        else if ('\n' == chr) {
          // skip newlines
        }
        else if (';' != chr && ')' != chr && '(' != chr) {
          rtrn.append(chr);
        }
        else {
          break;
        }
      }
    }
    return rtrn.toString().trim();
  }

  private GameNode parseToken(String token, final GameNode parentNode, Game game) {
    GameNode rtrnNode = new GameNode(parentNode);
    // replace delimiters
    token = Parser.prepareToken("'" + token + "'");

    // lets find all the properties
    Pattern p = Pattern.compile("([a-zA-Z]{1,})((\\[[^\\]]*\\]){1,})");
    Matcher m = p.matcher(token);
    while (m.find()) {
      String group = m.group();
      if (group.length() == 0)
        continue;

      String key = m.group(1);
      String value = m.group(2);
      if (value.startsWith("[")) {
        value = value.substring(1, value.length() - 1);
      }

      value = Parser.normaliseToken(value);

      // these properties require some cleanup
      if ("AB".equals(key) || "AW".equals(key)) {
        // these come in as a list of coordinates while the first [ is cut off
        // and also the last ], easy to split by ][
        String[] list = value.split("\\]\\[");
        // if the parent node is null then these are
        // game properties, if not null then the node properties
        if (parentNode == null) {
          game.addProperty(key, String.join(",", list));
        }
        else {
          rtrnNode.addProperty(key, String.join(",", list));
        }
      }
      else if ("C".equals(key) || "N".equals(key)) {
        // nodes and the game can have a comment or name
        // if parent is null it is a game property
        if (parentNode == null) {
          game.addProperty(key, value);
        }
        else {
          rtrnNode.addProperty(key, value);
        }
      }
      else if (generalProps.contains(key)) {
        game.addProperty(key, value);
      }
      else if (nodeProps.contains(key)) {
        rtrnNode.addProperty(key, cleanValue(value));
      }
      else if ("L".equals(key)) {
        log.debug("Not handling " + key + " = " + value);
      }
      else {
         log.error("Not able to parse property '" + m.group(1) + "'=" + m.group(2) + ". Found it from " + m.group(0));
//        throw new SgfParseException("Ignoring property '" + m.group(1) + "'=" + m.group(2) + " Found it from '" + m.group(0) + "'");
      }
    }

    return rtrnNode;
  }

  private String cleanValue(String value) {
    String cleaned = value.replaceAll("\\\\;", ";");
    return cleaned;
  }

  private static String prepareToken(String token) {
    token = token.replaceAll("\\\\\\[", "@@@@@");
    token = token.replaceAll("\\\\\\]", "#####");
    return token;
  }

  public static String normaliseToken(String token) {
    token = token.replaceAll("@@@@@", "\\\\\\[");
    token = token.replaceAll("#####", "\\\\\\]");
    return token;
  }
}
