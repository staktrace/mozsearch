package org.mozilla.mozsearch;

import java.nio.file.Paths;

public class JavaAnalyze {
  public static void main(String[] args) {
    if (args.length != 2) {
      System.err.println("Usage: JavaAnalyze <source path> <destination path>");
      System.exit(-1);
    }

    System.out.println("Generating references ...");
    JavaIndexer indexer = new JavaIndexer(Paths.get(args[0]), Paths.get(args[1]));
    indexer.outputIndexes();
  }
}
