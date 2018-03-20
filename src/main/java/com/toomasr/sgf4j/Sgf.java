package com.toomasr.sgf4j;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;

import com.toomasr.sgf4j.parser.Game;
import com.toomasr.sgf4j.parser.GameNode;

public class Sgf {
  private Parser parser;
  private Game game;

  private Sgf(String sgf) {
    parser = new Parser(sgf);
    game = parser.parse();

    game.postProcess();
  }

  public static Game createFromPath(Path path, String charSet) {
    try {
      String gameAsString = new String(Files.readAllBytes(path), charSet);
      return createFromString(gameAsString);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static Game createFromPath(Path path) {
    try {
      String gameAsString = new String(Files.readAllBytes(path), "UTF-8");
      return createFromString(gameAsString);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static Game createFromString(String gameAsString) {
    Sgf rtrn = new Sgf(gameAsString);
    return rtrn.getGame();
  }

  public static Game createFromInputStream(InputStream in) {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8").newDecoder()))) {
      StringBuilder out = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        out.append(line);
      }
      Sgf rtrn = new Sgf(out.toString());
      return rtrn.getGame();
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  public static void writeToFile(Game game, Path destination) {
    try (
        OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(destination.toFile()), Charset.forName("UTF-8").newEncoder())) {

      osw.write("(");

      // lets write all the root node properties
      Map<String, String> props = game.getProperties();
      if (props.size() > 0) {
        osw.write(";");
      }

      for (Iterator<Map.Entry<String, String>> ite = props.entrySet().iterator(); ite.hasNext();) {
        Map.Entry<String, String> entry = ite.next();
        osw.write(entry.getKey() + "[" + entry.getValue() + "]");
      }
      GameNode node = game.getRootNode();
      do {
        osw.write(";");
        for (Iterator<Map.Entry<String, String>> ite = node.getProperties().entrySet().iterator(); ite.hasNext();) {
          Map.Entry<String, String> entry = ite.next();
          osw.write(entry.getKey() + "[" + entry.getValue() + "]");
        }
        osw.write("\n");
        // System.out.println(node);
      }
      while ((node = node.getNextNode()) != null);
      osw.write(")");
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static File writeToFile(String sgf) {
    BufferedOutputStream bos = null;
    try {
      File tmpFile = File.createTempFile("sgf4j-test-", ".sgf");
      bos = new BufferedOutputStream(new FileOutputStream(tmpFile));
      bos.write(sgf.getBytes());
      return tmpFile;
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
    finally {
      if (bos != null) {
        try {
          bos.close();
        }
        catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  private Game getGame() {
    return game;
  }
}
