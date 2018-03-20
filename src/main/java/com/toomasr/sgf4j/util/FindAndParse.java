package com.toomasr.sgf4j.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import com.toomasr.sgf4j.Sgf;

public class FindAndParse {
  public static void main(String[] args) throws IOException {
    Path path = Paths.get(args[0]);
    System.out.println(path.toAbsolutePath());

    Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file,
          BasicFileAttributes attr) {
        if (attr.isRegularFile() && file.getFileName().toString().toLowerCase().endsWith("sgf")) {

          try {
            Sgf.createFromPath(file);

          }
          catch (Exception e) {
            System.out.format("Parsing %s\n", file);
            e.printStackTrace();
          }
        }
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        System.out.format("Visiting: %s\n", dir);
        return super.preVisitDirectory(dir, attrs);
      }

    });
  }
}
