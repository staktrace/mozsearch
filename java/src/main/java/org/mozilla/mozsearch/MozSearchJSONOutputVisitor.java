package org.mozilla.mozsearch;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.json.JSONObject;

public class MozSearchJSONOutputVisitor extends VoidVisitorAdapter<String> {
  private Path mOutputPath;

  public MozSearchJSONOutputVisitor(final Path output) {
    mOutputPath = output;
    if (Files.exists(output)) {
      try {
        Files.delete(output);
      } catch (IOException exception) {
        System.err.println(exception);
      }
    }
  }

  private static String getScope(final String fullName, final SimpleName name) {
    return fullName.substring(0, fullName.length() - name.toString().length());
  }

  private static String getContext(final Node n) {
    try {
      Optional<Node> parent = n.getParentNode();
      while (parent.isPresent()) {
        if (parent.get() instanceof MethodDeclaration) {
          MethodDeclaration d = (MethodDeclaration) parent.get();
          final ResolvedMethodDeclaration decl = d.resolve();
          return decl.getQualifiedName();
        } else if (parent.get() instanceof ConstructorDeclaration) {
          final ConstructorDeclaration d = (ConstructorDeclaration) parent.get();
          final ResolvedReferenceTypeDeclaration decl = d.resolve().declaringType();
          return decl.getQualifiedName() + "." + d.getName();
        } else if (parent.get() instanceof ClassOrInterfaceDeclaration) {
          final ClassOrInterfaceDeclaration d = (ClassOrInterfaceDeclaration) parent.get();
          final ResolvedReferenceTypeDeclaration decl = d.resolve();
          return decl.getQualifiedName();
        }
        parent = parent.get().getParentNode();
      }
    } catch (Exception e) {
      // not resolved
    }
    return "";
  }

  private void outputJSON(final JSONObject obj) {
    try {
      if (!Files.exists(mOutputPath.getParent())) {
        Files.createDirectories(mOutputPath.getParent());
      }
      PrintWriter printWriter =
          new PrintWriter(new BufferedWriter(new FileWriter(mOutputPath.toFile(), true)));
      printWriter.println(obj);
      printWriter.close();
    } catch (IOException exception) {
      System.err.println(exception);
    }
  }

  private void outputSource(
      final ClassOrInterfaceDeclaration node, final SimpleName name, final String scope) {
    final String fullName = scope + name.getIdentifier();

    MozSearchJSONObject obj = new MozSearchJSONObject();
    obj.addSourceLine(name).addSource(node, name, scope);
    obj.addSymbol(fullName);

    outputJSON(obj);
  }

  private void outputSource(
      final ClassOrInterfaceType node, final SimpleName name, final String scope) {
    final String fullName = scope + name.getIdentifier();

    MozSearchJSONObject obj = new MozSearchJSONObject();
    obj.addSourceLine(name).addSource(node, name, scope);
    obj.addSymbol(fullName);

    outputJSON(obj);
  }

  private void outputSource(
      final ConstructorDeclaration node, final SimpleName name, final String scope) {
    final String fullName = scope + name.getIdentifier();

    MozSearchJSONObject obj = new MozSearchJSONObject();
    obj.addSourceLine(name).addSource(node, name, scope);
    obj.addSymbol(fullName);

    outputJSON(obj);
  }

  private void outputSource(
      final MethodDeclaration node, final SimpleName name, final String scope) {
    final String fullName = scope + name.getIdentifier();

    MozSearchJSONObject obj = new MozSearchJSONObject();
    obj.addSourceLine(name).addSource(node, name, scope);
    obj.addSymbol(fullName);

    outputJSON(obj);
  }

  private void outputSource(
      final VariableDeclarator node, final SimpleName name, final String scope) {
    final String fullName = scope + name.getIdentifier();

    MozSearchJSONObject obj = new MozSearchJSONObject();
    obj.addSourceLine(name).addSource(node, name, scope);
    obj.addSymbol(fullName);

    outputJSON(obj);
  }

  private void outputSource(
      final ObjectCreationExpr node, final SimpleName name, final String scope) {
    final String fullName = scope + name.getIdentifier();

    MozSearchJSONObject obj = new MozSearchJSONObject();
    obj.addSourceLine(name).addSource(node, name, scope);
    obj.addSymbol(fullName);

    outputJSON(obj);
  }

  private void outputSource(final MethodCallExpr node, final SimpleName name, final String scope) {
    final String fullName = scope + name.getIdentifier();

    MozSearchJSONObject obj = new MozSearchJSONObject();
    obj.addSourceLine(name).addSource(node, name, scope);
    obj.addSymbol(fullName);

    outputJSON(obj);
  }

  private void outputSource(final FieldAccessExpr node, final SimpleName name, final String scope) {
    final String fullName = scope + name.getIdentifier();

    MozSearchJSONObject obj = new MozSearchJSONObject();
    obj.addSourceLine(name).addSource(node, name, scope);
    obj.addSymbol(fullName);

    outputJSON(obj);
  }

  private void outputSource(final NameExpr node, final SimpleName name, final String scope) {
    final String fullName = scope + name.getIdentifier();

    MozSearchJSONObject obj = new MozSearchJSONObject();
    obj.addSourceLine(name).addSource(node, name, scope);
    obj.addSymbol(fullName);

    outputJSON(obj);
  }

  private void outputTarget(
      final ClassOrInterfaceDeclaration node,
      final SimpleName name,
      final String scope,
      final String context) {
    final String fullName = scope + name.getIdentifier();

    MozSearchJSONObject obj = new MozSearchJSONObject();
    obj.addTargetLine(name).addTarget(node, name, scope, context);
    obj.addSymbol(fullName);

    outputJSON(obj);
  }

  private void outputTarget(
      final ClassOrInterfaceType node,
      final SimpleName name,
      final String scope,
      final String context) {
    final String fullName = scope + name.getIdentifier();

    MozSearchJSONObject obj = new MozSearchJSONObject();
    obj.addTargetLine(name).addTarget(node, name, scope, context);
    obj.addSymbol(fullName);

    outputJSON(obj);
  }

  private void outputTarget(
      final ConstructorDeclaration node,
      final SimpleName name,
      final String scope,
      final String context) {
    final String fullName = scope + name.getIdentifier();

    MozSearchJSONObject obj = new MozSearchJSONObject();
    obj.addTargetLine(name).addTarget(node, name, scope, context);
    obj.addSymbol(fullName);

    outputJSON(obj);
  }

  private void outputTarget(
      final VariableDeclarator node,
      final SimpleName name,
      final String scope,
      final String context) {
    final String fullName = scope + name.getIdentifier();

    MozSearchJSONObject obj = new MozSearchJSONObject();
    obj.addTargetLine(name).addTarget(node, name, scope, context);
    obj.addSymbol(fullName);

    outputJSON(obj);
  }

  private void outputTarget(
      final MethodDeclaration node,
      final SimpleName name,
      final String scope,
      final String context) {
    final String fullName = scope + name.getIdentifier();

    MozSearchJSONObject obj = new MozSearchJSONObject();
    obj.addTargetLine(name).addTarget(node, name, scope, context);
    obj.addSymbol(fullName);

    outputJSON(obj);
  }

  private void outputTarget(
      final ObjectCreationExpr node,
      final SimpleName name,
      final String scope,
      final String context) {
    final String fullName = scope + name.getIdentifier();

    MozSearchJSONObject obj = new MozSearchJSONObject();
    obj.addTargetLine(name).addTarget(node, name, scope, context);
    obj.addSymbol(fullName);

    outputJSON(obj);
  }

  private void outputTarget(
      final MethodCallExpr node, final SimpleName name, final String scope, final String context) {
    final String fullName = scope + name.getIdentifier();

    final MozSearchJSONObject obj = new MozSearchJSONObject();
    obj.addTargetLine(name).addTarget(node, name, scope, context);
    obj.addSymbol(fullName);

    outputJSON(obj);
  }

  private void outputTarget(
      final FieldAccessExpr node, final SimpleName name, final String scope, final String context) {
    final String fullName = scope + name.getIdentifier();

    final MozSearchJSONObject obj = new MozSearchJSONObject();
    obj.addTargetLine(name).addTarget(node, name, scope, context);
    obj.addSymbol(fullName);

    outputJSON(obj);
  }

  private void outputTarget(
      final NameExpr node, final SimpleName name, final String scope, final String context) {
    final String fullName = scope + name.getIdentifier();

    final MozSearchJSONObject obj = new MozSearchJSONObject();
    obj.addTargetLine(name).addTarget(node, name, scope, context);
    obj.addSymbol(fullName);

    outputJSON(obj);
  }

  /* This is class defines like "class ABC extends DEF" */
  @Override
  public void visit(ClassOrInterfaceDeclaration node, String a) {
    String scope = "";

    try {
      final ResolvedReferenceTypeDeclaration decl = node.resolve();
      scope = getScope(decl.getQualifiedName(), node.getName());
    } catch (Exception e) {
    }

    // TODO: set context (package name is better?)

    outputSource(node, node.getName(), scope);
    outputTarget(node, node.getName(), scope, "");

    for (ClassOrInterfaceType classType : node.getExtendedTypes()) {
      // TODO: must be resolve
      outputSource(classType, classType.getName(), "");
      outputTarget(classType, classType.getName(), "", "");
    }
    for (ClassOrInterfaceType classType : node.getImplementedTypes()) {
      // TODO: must be resolve
      outputSource(classType, classType.getName(), "");
      outputTarget(classType, classType.getName(), "", "");
    }
    super.visit(node, a);
  }

  /* This is member defines / local variable like "int x" */
  @Override
  public void visit(VariableDeclarator node, String a) {
    String scope = "";

    try {
      final ResolvedValueDeclaration decl = node.resolve();
      if (decl.isField()) {
        final ResolvedTypeDeclaration typeDecl = decl.asField().declaringType();
        scope = typeDecl.getQualifiedName() + ".";
      }
    } catch (Exception e) {
      // not resolved
    }

    final String context = getContext(node);

    outputSource(node, node.getName(), scope);
    outputTarget(node, node.getName(), scope, context);

    // TODO: output type

    super.visit(node, a);
  }

  /* This is constructor define "class ABC { ABC() }" */
  @Override
  public void visit(ConstructorDeclaration node, String a) {
    String scope = "";

    try {
      final ResolvedReferenceTypeDeclaration decl = node.resolve().declaringType();
      scope = decl.getQualifiedName() + ".";
    } catch (Exception e) {
      // not resolved
    }

    final String context = getContext(node);

    outputSource(node, node.getName(), scope);
    outputTarget(node, node.getName(), scope, context);

    // TODO: output parameter

    super.visit(node, a);
  }

  /* This is method define like "int ABC();" */
  @Override
  public void visit(MethodDeclaration node, String a) {
    String scope = "";

    try {
      final ResolvedMethodDeclaration decl = node.resolve();
      scope = getScope(decl.getQualifiedName(), node.getName());
    } catch (Exception e) {
      // not resolved
    }

    final String context = getContext(node);

    outputSource(node, node.getName(), scope);
    outputTarget(node, node.getName(), scope, context);

    // TODO: output parameters

    super.visit(node, a);
  }

  /* This is method call like "x = ABC();" */
  @Override
  public void visit(MethodCallExpr node, String a) {
    String scope = "";

    try {
      final ResolvedMethodDeclaration decl = node.resolve();
      scope = getScope(decl.getQualifiedName(), node.getName());
    } catch (Exception e) {
      // not resolved.
    }

    final String context = getContext(node);

    outputSource(node, node.getName(), scope);
    outputTarget(node, node.getName(), scope, context);

    super.visit(node, a);
  }

  /* This is generic access of variable like "abc = 1" */
  @Override
  public void visit(NameExpr node, String a) {
    String scope = "";

    try {
      final ResolvedValueDeclaration decl = node.resolve();
      if (decl.isField()) {
        final ResolvedTypeDeclaration typeDecl = decl.asField().declaringType();
        scope = typeDecl.getQualifiedName() + ".";
      }
    } catch (Exception e) {
      // not resolved
    }

    final String context = getContext(node);

    outputSource(node, node.getName(), scope);
    outputTarget(node, node.getName(), scope, context);

    super.visit(node, a);
  }

  /* This is constructor call of variable like "ABC abc = new ABC()" */
  @Override
  public void visit(ObjectCreationExpr node, String a) {
    String scope = "";

    try {
      final ResolvedConstructorDeclaration decl = node.resolve();
      scope = getScope(decl.getQualifiedName(), node.getType().getName());
    } catch (Exception e) {
      // not resolved
    }

    final String context = getContext(node);

    outputSource(node, node.getType().getName(), scope);
    outputTarget(node, node.getType().getName(), scope, context);

    super.visit(node, a);
  }

  /* This is member access like "abc.x = 1" */
  @Override
  public void visit(FieldAccessExpr node, String a) {
    String scope = "";

    try {
      final ResolvedFieldDeclaration decl = node.resolve().asField();
      final ResolvedTypeDeclaration typeDecl = decl.declaringType();
      scope = typeDecl.getQualifiedName() + ".";
    } catch (Exception e) {
      // not resolved
    }

    final String context = getContext(node);

    outputSource(node, node.getName(), scope);
    outputTarget(node, node.getName(), scope, context);

    super.visit(node, a);
  }
}
