package org.mozilla.mozsearch;

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
import org.json.JSONObject;

public class MozSearchJSONObject extends JSONObject {
  public MozSearchJSONObject() {
    super();
  }

  public MozSearchJSONObject addSourceLine(final SimpleName name) {
    put(
            "loc",
            name.getBegin().get().line
                + ":"
                + (name.getBegin().get().column - 1)
                + "-"
                + (name.getBegin().get().column - 1 + name.getIdentifier().length()))
        .put("source", 1);
    return this;
  }

  public MozSearchJSONObject addTargetLine(final SimpleName name) {
    put("loc", name.getBegin().get().line + ":" + (name.getBegin().get().column - 1))
        .put("target", 1);
    return this;
  }

  public MozSearchJSONObject addSymbol(final String symbolName) {
    put("sym", symbolName.replace('.', '#'));
    return this;
  }

  public JSONObject addSource(
      final ClassOrInterfaceDeclaration node, final SimpleName name, final String scope) {
    if (node.isInterface()) {
      return put("syntax", "def,type").put("pretty", "interface " + scope + name.getIdentifier());
    }
    return put("syntax", "def,type").put("pretty", "class " + scope + name.getIdentifier());
  }

  public JSONObject addSource(
      final ClassOrInterfaceType node, final SimpleName name, final String scope) {
    return put("syntax", "type,use").put("pretty", "class " + scope + name.getIdentifier());
  }

  public JSONObject addSource(
      final ConstructorDeclaration node, final SimpleName name, final String scope) {
    return put("syntax", "def,function")
        .put("pretty", "constructor " + scope + name.getIdentifier());
  }

  public JSONObject addSource(
      final MethodDeclaration node, final SimpleName name, final String scope) {
    return put("syntax", "def,function").put("pretty", "method " + scope + name.getIdentifier());
  }

  public JSONObject addSource(
      final VariableDeclarator node, final SimpleName name, final String scope) {
    if (scope.length() > 0) {
      return put("syntax", "def,variable").put("pretty", "member " + scope + name.getIdentifier());
    }
    return put("syntax", "use,variable")
        .put("pretty", "variable " + scope + name.getIdentifier())
        .put("no_crossref", 1);
  }

  public JSONObject addSource(
      final MethodCallExpr node, final SimpleName name, final String scope) {
    return put("syntax", "use,function").put("pretty", "method " + scope + name.getIdentifier());
  }

  public JSONObject addSource(
      final ObjectCreationExpr node, final SimpleName name, final String scope) {
    return put("syntax", "use,function")
        .put("pretty", "constructor " + scope + name.getIdentifier());
  }

  public JSONObject addSource(
      final FieldAccessExpr node, final SimpleName name, final String scope) {
    return put("syntax", "use").put("pretty", "member " + scope + name.getIdentifier());
  }

  public JSONObject addSource(final NameExpr node, final SimpleName name, final String scope) {
    if (scope.length() > 0) {
      return put("syntax", "use,variable").put("pretty", "member " + scope + name.getIdentifier());
    }
    return put("syntax", "use,variable")
        .put("pretty", "variable " + scope + name.getIdentifier())
        .put("no_crossref", 1);
  }

  public JSONObject addTarget(
      final ClassOrInterfaceDeclaration n,
      final SimpleName name,
      final String scope,
      final String context) {
    return put("kind", "def").put("pretty", scope + name.getIdentifier());
  }

  public JSONObject addTarget(
      final ClassOrInterfaceType n,
      final SimpleName name,
      final String scope,
      final String context) {
    return put("kind", "use").put("pretty", scope + name.getIdentifier()).put("context", context);
  }

  public JSONObject addTarget(
      final ConstructorDeclaration n,
      final SimpleName name,
      final String scope,
      final String context) {
    return put("kind", "def").put("pretty", scope + name.getIdentifier()).put("context", context);
  }

  public JSONObject addTarget(
      final MethodDeclaration node,
      final SimpleName name,
      final String scope,
      final String context) {
    return put("kind", "def").put("pretty", scope + name.getIdentifier()).put("context", context);
  }

  public JSONObject addTarget(
      final VariableDeclarator node,
      final SimpleName name,
      final String scope,
      final String context) {
    return put("kind", "use").put("pretty", scope + name.getIdentifier()).put("context", context);
  }

  public JSONObject addTarget(
      final MethodCallExpr node, final SimpleName name, final String scope, final String context) {
    return put("kind", "use").put("pretty", scope + name.getIdentifier()).put("context", context);
  }

  public JSONObject addTarget(
      final ObjectCreationExpr node,
      final SimpleName name,
      final String scope,
      final String context) {
    return put("kind", "use").put("pretty", scope + name.getIdentifier()).put("context", context);
  }

  public JSONObject addTarget(
      final FieldAccessExpr node, final SimpleName name, final String scope, final String context) {
    return put("kind", "use").put("pretty", scope + name.getIdentifier()).put("context", context);
  }

  public JSONObject addTarget(
      final NameExpr node, final SimpleName name, final String scope, final String context) {
    return put("kind", "use").put("pretty", scope + name.getIdentifier()).put("context", context);
  }
}
