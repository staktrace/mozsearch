package org.mozilla.mozsearch;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JavaIndexer {
  private Path mSourceDir;
  private Path mOutputDir;
  private Path[] mRootPaths;

  public JavaIndexer(final Path sourceDir, final Path outputDir) {
    mSourceDir = sourceDir;
    mOutputDir = outputDir;
  }

  public void addRootPaths(final Path[] rootPaths) {
    mRootPaths = rootPaths;
  }

  public void outputIndexes() {
    try {
      indexAllChildren(mSourceDir, mSourceDir, mOutputDir);
    } catch (IOException exception) {
      System.err.println(exception);
    }
  }

  private void indexAllChildren(final Path currentDir, final Path srcDir, final Path outputDir)
      throws IOException {
    if (currentDir.toFile().getName().equals(".git")
        || currentDir.toFile().getName().equals(".hg")) {
      return;
    }
    ArrayList<Path> javaFiles = new ArrayList<Path>();
    List<Path> files = Files.list(currentDir).collect(Collectors.toList());
    for (Path file : files) {
      if (Files.isDirectory(file)) {
        indexAllChildren(file, srcDir, outputDir);
      } else if (file.toString().endsWith(".java")) {
        javaFiles.add(file);
      }
    }
    makeIndexes(javaFiles, srcDir, outputDir);
  }

  private void makeIndexes(final List<Path> files, final Path srcDir, final Path outputDir) {
    if (files.isEmpty()) {
      return;
    }

    final CombinedTypeSolver solver = new CombinedTypeSolver();
    solver.add(new ReflectionTypeSolver());
    // Set Android SDK's JAR using ANDROID_SDK_ROOT
    String sdkroot = System.getenv("ANDROID_SDK_ROOT");
    if (sdkroot != null && sdkroot.length() > 0) {
      try {
        solver.add(new JarTypeSolver(sdkroot + "/platforms/android-28/android.jar"));
      } catch (IOException exception) {
      }
    }

    if (mRootPaths != null) {
      for (Path path : mRootPaths) {
        solver.add(new JavaParserTypeSolver(path.toString()));
      }
    }

    ArrayList<Path> dirs = new ArrayList<Path>();
    for (Path file : files) {
      if (!dirs.contains(file.getParent())) {
        solver.add(new JavaParserTypeSolver(file.getParent().toFile()));
        dirs.add(file.getParent());
      }
    }

    final JavaSymbolSolver symbolSolver = new JavaSymbolSolver(solver);
    StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);

    for (Path file : files) {
      Path output =
          Paths.get(
              outputDir.toString(), file.toString().substring(srcDir.toString().length() + 1));
      try {
        makeIndex(file, output);
      } catch (Exception exception) {
        System.err.println(exception);
        try {
          Files.delete(output);
        } catch (IOException ioexception) {
        }
      }
    }

    System.gc();
  }

  private void makeIndex(final Path file, final Path outputPath) throws IOException {
    if (!file.toString().endsWith(".java")) {
      return;
    }
    System.out.println("Processing " + file.toString() + " ");

    final CompilationUnit unit = StaticJavaParser.parse(file);

    MozSearchJSONOutputVisitor visitor = new MozSearchJSONOutputVisitor(outputPath);
    unit.accept(visitor, null);
  }
}
