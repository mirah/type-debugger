/*
 * Copyright (c) 2014 The Mirah project authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.mirah.jvm.mirrors.debug.prettyprint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.lang.model.SourceVersion;
import mirah.lang.ast.Annotation;
import mirah.lang.ast.AnnotationList;
import mirah.lang.ast.Arguments;
import mirah.lang.ast.Array;
import mirah.lang.ast.Block;
import mirah.lang.ast.Boolean;
import mirah.lang.ast.Break;
import mirah.lang.ast.Call;
import mirah.lang.ast.CallSite;
import mirah.lang.ast.Cast;
import mirah.lang.ast.CharLiteral;
import mirah.lang.ast.ClassAppendSelf;
import mirah.lang.ast.ClassDefinition;
import mirah.lang.ast.ClosureDefinition;
import mirah.lang.ast.Colon2;
import mirah.lang.ast.Colon3;
import mirah.lang.ast.Constant;
import mirah.lang.ast.ConstructorDefinition;
import mirah.lang.ast.EmptyArray;
import mirah.lang.ast.Ensure;
import mirah.lang.ast.FieldAccess;
import mirah.lang.ast.FieldAssign;
import mirah.lang.ast.Fixnum;
import mirah.lang.ast.Float;
import mirah.lang.ast.FunctionalCall;
import mirah.lang.ast.Hash;
import mirah.lang.ast.HashEntry;
import mirah.lang.ast.If;
import mirah.lang.ast.ImplicitNil;
import mirah.lang.ast.ImplicitSelf;
import mirah.lang.ast.Import;
import mirah.lang.ast.InterfaceDeclaration;
import mirah.lang.ast.LocalAccess;
import mirah.lang.ast.LocalAssignment;
import mirah.lang.ast.Loop;
import mirah.lang.ast.MethodDefinition;
import mirah.lang.ast.Next;
import mirah.lang.ast.Node;
import mirah.lang.ast.NodeList;
import mirah.lang.ast.Noop;
import mirah.lang.ast.Not;
import mirah.lang.ast.Null;
import mirah.lang.ast.OptionalArgument;
import mirah.lang.ast.Package;
import mirah.lang.ast.Raise;
import mirah.lang.ast.Redo;
import mirah.lang.ast.Regex;
import mirah.lang.ast.RequiredArgument;
import mirah.lang.ast.Rescue;
import mirah.lang.ast.RescueClause;
import mirah.lang.ast.Return;
import mirah.lang.ast.Script;
import mirah.lang.ast.Self;
import mirah.lang.ast.SimpleNodeVisitor;
import mirah.lang.ast.SimpleString;
import mirah.lang.ast.StaticMethodDefinition;
import mirah.lang.ast.StringConcat;
import mirah.lang.ast.StringEval;
import mirah.lang.ast.Super;
import mirah.lang.ast.Symbol;
import mirah.lang.ast.TypeName;
import mirah.lang.ast.TypeNameList;
import mirah.lang.ast.TypeRef;
import mirah.lang.ast.TypeRefImpl;
import mirah.lang.ast.VCall;
import mirah.lang.ast.ZSuper;

public class MirahSourceGenerator extends SimpleNodeVisitor {

    private final List<Token> tokens = new ArrayList<>();
    protected final TokenPrinter out = new TokenPrinter(tokens);
    protected static final Token NL = new NewlineToken();
    protected static final Token NBR = new SoftNewlineToken();
    protected static final Token LIST = new ListStartToken();
    protected static final Token LI = new ListSeparatorToken();
    protected static final Token LIST_END = new ListEndToken();
    protected static final Token INDENT = new IndentToken();
    protected static final Token DEDENT = new DedentToken();
    protected static final Token SLP = new SoftLParenToken();
    protected static final Token SRP = new SoftRParenToken();
    protected static final Token ST = new TagToken("</span>");
    protected static final Token END = kw("end");

    @Override
    public String toString() {
        return out.toString();
    }

    protected static Token s(String style, String value) {
        return new StyledToken(style, value);
    }

    protected static Token kw(String value) {
        return s("kw", value);
    }

    protected Token span(String style) {
        return new TagToken("<span class=" + style + ">");
    }

    protected Object add(final String identifier) {
        if (identifier != null) {
            tokens.add(new EscapedToken(identifier));
        }
        return null;
    }

    protected Object add(String format, Object... args) {
        tokens.add(new EscapedToken(String.format(format, args)));
        return null;
    }

    protected Object add(Token t) {
        tokens.add(t);
        return null;
    }

    protected Object addAll(Object... args) {
        for (Object arg : args) {
            if (arg instanceof Token) {
                add((Token) arg);
            } else if (arg instanceof String) {
                add(new EscapedToken((String) arg));
            } else {
                format((Node) arg);
            }
        }
        return null;
    }

    @Override
    public Object visitTypeRefImpl(TypeRefImpl tri, Object o) {
        return addAll(tri.name(), tri.isArray() ? "[]" : null);
    }

    @Override
    public Object visitVCall(VCall vcall, Object o) {
        final String identifier = vcall.identifier();
        return add(identifier);
    }

    @Override
    public Object visitZSuper(ZSuper zsuper, Object o) {
        return add(kw("super"));
    }

    private void formatAll(TypeNameList items, Token separator) {
        Iterator it = items.iterator();
        while (it.hasNext()) {
            TypeName n = (TypeName) it.next();
            if (it.hasNext()) {
                addAll(", ", separator);
            }
            format(n.typeref());
        }
    }

    private void formatAll(Iterable items) {
        formatAll(items, new StringToken(", "));
    }

    private void formatAll(Iterable items, Token separator) {
        Iterator it = items.iterator();
        while (it.hasNext()) {
            Node n = (Node) it.next();
            n.accept(this, null);
            if (it.hasNext()) {
                add(separator);
                if (!(separator instanceof NewlineToken)) {
                    add(LI);
                }
            }
        }
    }

    private Object format(Node n) {
        if (n != null) {
            n.accept(this, null);
        }
        return null;
    }

    @Override
    public Object visitFunctionalCall(FunctionalCall fc, Object o) {
        final String name = fc.name().identifier();
        if (isOperator(name) && fc.parameters_size() == 1 && fc.block() == null) {
            return addAll(name.substring(0, 1), fc.parameters(0));
        }
        addAll(name, "(", LIST);
        formatAll(fc.parameters());
        addAll(")", LIST_END);
        return format(fc.block());
    }

    @Override
    public Object visitCast(Cast cast, Object o) {
        TypeRef type = cast.type().typeref();
        String name = type.name();
        if (type.isArray()) {
            return addAll(span("cast"), name, "[]", NBR, ".cast", ST, "(", LIST, cast.value(), LIST_END, ")");
        } else {
            return addAll(s("cast", name), "(", LIST, cast.value(), LIST_END, ")", ST);
        }
    }

    @Override
    public Object visitCall(Call call, Object o) {
        final String name = call.name().identifier();
        if (call.parameters_size() == 1
                && call.block() == null
                && isOperator(name)) {
            return visitOp(call);
        }
        if (name.equals("[]") && call.block() == null) {
            addAll(call.target(), "[", LIST);
            formatAll(call.parameters());
            return addAll("]", LIST_END);
        } else if (name.equals("[]=") && call.block() == null) {
            addAll(call.target(), "[", LIST);
            int valueIndex = call.parameters_size() - 1;
            for (int i = 0; i < valueIndex; ++i) {
                if (i > 0) {
                    addAll(", ", LI);
                }
                format(call.parameters(i));
            }
            return addAll("] = ", LIST_END, NBR, call.parameters(valueIndex));
        } else {
            addAll(call.target(), NBR, ".", name, "(", LIST);
            formatAll(call.parameters());
            return addAll(")", LIST_END, call.block());
        }
    }

    private Object visitOp(Call call) {
        String name = call.name().identifier();
        handleOpArg(call.target(), name);
        addAll(" ", kw(name), " ", NBR);
        handleOpArg(call.parameters(0), name);
        return null;
    }

    @Override
    public Object visitColon2(Colon2 colon2, Object o) {
        return addAll(colon2.target(), NBR, "::", colon2.name().identifier());
    }

    @Override
    public Object visitSuper(Super s, Object o) {
        addAll(kw("super"), "(", LIST);
        formatAll(s.parameters());
        return addAll(LIST_END, ")");
    }

    @Override
    public Object visitIf(If i, Object o) {
        if (i.body_size() == 0) {
            add(kw("unless "));
            formatCondition(i.condition());
            return addAll(INDENT, i.elseBody(), DEDENT, END);
        }
        add(kw("if "));
        formatCondition(i.condition());
        addAll(INDENT, i.body(), DEDENT);
        if (i.elseBody_size() > 0) {
            if (i.elseBody_size() == 1 && i.elseBody(0) instanceof If) {
                return addAll(kw("els"), i.elseBody());
            } else {
                return addAll(kw("else"), INDENT, i.elseBody(), DEDENT, END);
            }
        }
        return add(END);
    }

    @Override
    public Object visitLoop(Loop loop, Object o) {
        final Token kind = kw(loop.negative() ? "until " : "while ");
        if (loop.skipFirstCheck()) {
            addAll(kw("begin"), INDENT);
        } else {
            add(kind);
            formatCondition(loop.condition());
            add(INDENT);
        }
        if (loop.init_size() > 0) {
            addAll(kw("init do"), INDENT, loop.init(), DEDENT, END, NL);
        }
        if (loop.pre_size() > 0) {
            addAll(kw("pre do"), INDENT, loop.pre(), DEDENT, END, NL);
        }
        if (loop.post_size() > 0) {
            addAll(kw("post do"), INDENT, loop.post(), DEDENT, END, NL);
        }
        format(loop.body());
        addAll(DEDENT, END);
        if (loop.skipFirstCheck()) {
            addAll(" ", kind);
            formatCondition(loop.condition());
        }
        return null;
    }

    @Override
    public Object visitNot(Not not, Object o) {
        return addAll(kw("!"), not.value());
    }

    @Override
    public Object visitReturn(Return r, Object o) {
        return addAll(kw("return "), r.value());
    }

    @Override
    public Object visitBreak(Break b, Object o) {
        return add(kw("break"));
    }

    @Override
    public Object visitNext(Next next, Object o) {
        return add(kw("next"));
    }

    @Override
    public Object visitRedo(Redo redo, Object o) {
        return add(kw("redo"));
    }

    @Override
    public Object visitImport(Import i, Object o) {
        add(kw("import "));
        String fullname = i.fullName().identifier();
        String shortname = i.simpleName().identifier();
        if ("*".equals(shortname)) {
            return addAll(fullname, ".", shortname);
        } else if (fullname.endsWith("." + shortname)) {
            return add(fullname);
        } else if (shortname.startsWith(".")) {
            if (".*".equals(shortname)) {
                return addAll("static ", fullname, ".*");
            } else {
                return addAll("static ", fullname);
            }
        } else {
            return addAll(fullname, kw(" as "), shortname);
        }
    }

    @Override
    public Object visitPackage(Package pckg, Object o) {
        if (pckg.body() == null) {
            return addAll(kw("package "), pckg.name().identifier(), NL);
        }
        return super.visitPackage(pckg, o);
    }

    @Override
    public Object visitFixnum(Fixnum fixnum, Object o) {
        return add(s("num", "" + fixnum.value()));
    }

    @Override
    public Object visitFloat(Float f, Object o) {
        return add(s("num", "" + f.value()));
    }

    @Override
    public Object visitCharLiteral(CharLiteral cl, Object o) {
        if (Character.isUnicodeIdentifierPart(cl.value())) {
            return add(s("num", String.format("?%c", cl.value())));
        } else {
            return add(s("num", String.format("?\\U%8x", cl.value())));
        }
    }

    @Override
    public Object visitNodeList(NodeList nl, Object o) {
        formatAll(nl, NL);
        return null;
    }

    @Override
    public Object visitSymbol(Symbol symbol, Object o) {
        return add(s("sym", ":" + symbol.value()));
    }

    @Override
    public Object visitBoolean(Boolean bln, Object o) {
        return add(s("const", "" + bln.value()));
    }

    @Override
    public Object visitNull(Null n, Object o) {
        return add(s("const", "nil"));
    }

    @Override
    public Object visitImplicitNil(ImplicitNil in, Object o) {
        return add(s("const", "nil"));
    }

    @Override
    public Object visitSelf(Self self, Object o) {
        return add(s("const", "self"));
    }

    @Override
    public Object visitImplicitSelf(ImplicitSelf is, Object o) {
        return add(s("const", "self"));
    }

    @Override
    public Object visitNoop(Noop noop, Object o) {
        return null;
    }

    @Override
    public Object visitScript(Script script, Object o) {
        add(new TagToken("<style>pre{font:12pt Monaco,Monospace}.err{color:#ffffff;background-color:#990000}.var{color: #318495}.kw{color:#0000FF;font-weight:bold}.num{color:#0000CD}.sym{color:#C5060B;font-weight:bold}.const{color:#585CF6;font-weight:bold}.str{color:#036A07}.source{color:#26B31A}.fname{color:#0000A2;font-weight:bold}.cname{text-decoration:underline}.type{color:#70727E;font-style:normal}.cast{color:#3C4C72;font-weight:bold}.scname{font-style:italic}.param{font-style:italic}</style><pre>"));
        return visitNodeList(script.body(), o);
    }

    @Override
    public Object visitSimpleString(SimpleString ss, Object o) {
        return add(s("str", String.format("'%s'", ss.value().replace("\\", "\\\\").replace("'", "\\'"))));
    }

    @Override
    public Object visitStringEval(StringEval node, Object arg) {
        return addAll(span("source"), "#{", LIST, node.value(), LI, "}", ST, LIST_END);
    }

    @Override
    public Object visitStringConcat(StringConcat node, Object arg) {
        addAll(span("str"), "\"");
        for (Object n : node.strings()) {
            if (n instanceof SimpleString) {
                add(DStringEscape((SimpleString) n));
            } else {
                format((Node) n);
            }
        }
        return addAll("\"", ST);
    }

    @Override
    public Object visitRegex(Regex node, Object arg) {
        addAll(span("str"), "/");
        for (Object n : node.strings()) {
            if (n instanceof SimpleString) {
                add(RegexpEscape((SimpleString) n));
            } else {
                format((Node) n);
            }
        }
        add("/");
        if (node.options() != null) {
            add(node.options().identifier());
        }
        return add(ST);
    }

    @Override
    public Object visitRaise(Raise raise, Object o) {
        add(kw("raise "));
        formatAll(raise.args());
        return null;
    }

    @Override
    public Object visitFieldAccess(FieldAccess fa, Object o) {
        return addAll(span("var"), "@", fa.isStatic() ? "@" : "", fa.name().identifier(), ST);
    }

    @Override
    public Object visitFieldAssign(FieldAssign node, Object arg) {
        return addAll(
                node.annotations(),
                span("var"),
                "@", node.isStatic() ? "@" : null, node.name().identifier(), ST,
                kw(" = "), NBR, node.value());
    }

    @Override
    public Object visitConstant(Constant cnstnt, Object o) {
        String name = cnstnt.identifier();
        return add(s("const", name));
    }

    @Override
    public Object visitColon3(Colon3 colon3, Object o) {
        return add("::" + colon3.identifier());
    }

    @Override
    public Object visitLocalAccess(LocalAccess la, Object o) {
        return add(la.name().identifier());
    }

    @Override
    public Object visitLocalAssignment(LocalAssignment node, Object arg) {
        return addAll(
                node.name().identifier(), kw(" = "), NBR, node.value());
    }

    @Override
    public Object defaultNode(Node node, Object arg) {
        return addAll(span("err"), "<", node.getClass().getSimpleName(), ">", ST);
    }

    @Override
    public Object visitClassDefinition(ClassDefinition node, Object arg) {
        String name = node.name() != null ? node.name().identifier() : "<anonymous>";
        addAll(NL, node.annotations(), kw("class "), s("cname", name), " ");
        if (node.superclass() != null) {
            addAll(kw("< "), s("scname", node.superclass().typeref().name()), " ");
        }
        if (node.interfaces_size() > 0) {
            addAll(LIST, kw("implements "), span("scname"));
            formatAll(node.interfaces(), LI);
            addAll(ST, LIST_END);
        }
        addAll(INDENT, node.body(), DEDENT, END, NL);

        return null;
    }

    @Override
    public Object visitClosureDefinition(ClosureDefinition node, Object arg) {
        return visitClassDefinition(node, arg);
    }

    @Override
    public Object visitInterfaceDeclaration(InterfaceDeclaration node, Object arg) {
        addAll(NL, node.annotations(), kw("interface "), s("cname", node.name().identifier()));
        if (node.interfaces_size() > 0) {
            addAll(kw(" < "), span("scname"));
            formatAll(node.interfaces(), NBR);
        }
        return addAll(ST, INDENT, node.body(), DEDENT, END, NL);
    }

    @Override
    public Object visitHash(Hash node, Object arg) {
        addAll("{", LIST);
        formatAll(node);
        return addAll("}", LIST_END);
    }

    @Override
    public Object visitAnnotation(Annotation node, Object arg) {
        addAll(span("const"), "$", node.type(), ST);
        if (node.values_size() > 0) {
            addAll("[", LIST);
            formatAll(node.values());
            addAll("]", LIST_END);
        }
        return add(NL);
    }

    @Override
    public Object visitAnnotationList(AnnotationList node, Object arg) {
        for (Object anno : node) {
            format((Node) anno);
        }
        return null;
    }

    @Override
    public Object visitHashEntry(HashEntry node, Object arg) {
        if (node.key() instanceof SimpleString
                && SourceVersion.isIdentifier(((SimpleString) node.key()).value())) {
            addAll(span("sym"), ((SimpleString) node.key()).value(), ":", ST, NBR);
        } else {
            addAll(node.key(), " => ", NBR);
        }
        return format(node.value());
    }

    @Override
    public Object visitConstructorDefinition(ConstructorDefinition node, Object arg) {
        return visitMethodDefinition(node, arg);
    }

    @Override
    public Object visitStaticMethodDefinition(StaticMethodDefinition node, Object arg) {
        return visitMethodDefinition(node, arg);
    }

    @Override
    public Object visitMethodDefinition(MethodDefinition node, Object arg) {
        addAll(node.annotations(), kw("def "), span("fname"));
        if (node instanceof StaticMethodDefinition) {
            add("self.");
        }
        addAll(node.name().identifier(), ST, "(", LIST, node.arguments(), LI, ")", LIST_END);
        if (node.type() != null) {
            addAll(span("type"), ":", node.type().typeref(), ST);
        }
        return addAll(INDENT, node.body(), DEDENT, END, NL);
    }

    @Override
    public Object visitOptionalArgument(OptionalArgument node, Object arg) {
        addAll(span("param"), node.name().identifier());
        if (node.type() != null) {
            addAll(span("type"), ":", node.type().typeref(), ST);
        }
        return addAll("=", NBR, node.value(), ST);
    }

    @Override
    public Object visitRequiredArgument(RequiredArgument node, Object arg) {
        add(s("param", node.name().identifier()));
        if (node.type() != null) {
            addAll(span("type"), ":", node.type().typeref(), ST);
        }
        return null;
    }

    @Override
    public Object visitArguments(Arguments node, Object arg) {
        List<Node> nodes = collectArguments(node);
        formatAll(nodes);
        return null;
    }

    private List<Node> collectArguments(Arguments node) {
        List<Node> nodes = new ArrayList<>();
        if (node == null) {
            return nodes;
        }
        for (int i = 0; i < node.required_size(); ++i) {
            nodes.add(node.required(i));
        }
        for (int i = 0; i < node.optional_size(); ++i) {
            nodes.add(node.optional(i));
        }
        if (node.rest() != null) {
            nodes.add(node.rest());
        }
        for (int i = 0; i < node.required2_size(); ++i) {
            nodes.add(node.required2(i));
        }
        if (node.block() != null) {
            nodes.add(node.block());
        }
        return nodes;
    }

    @Override
    public Object visitBlock(Block node, Object arg) {
        add(kw(" do"));
        List<Node> args = collectArguments(node.arguments());
        if (args.size() > 0) {
            addAll(" ", NBR, "|", LIST, node.arguments(), LI, "|", LIST_END);
        }
        return addAll(INDENT, node.body(), DEDENT, END);
    }

    @Override
    public Object visitArray(Array node, Object arg) {
        addAll("[", LIST);
        formatAll(node.values());
        return addAll(LI, "]", LIST_END);
    }

    @Override
    public Object visitEmptyArray(EmptyArray node, Object arg) {
        return addAll(node.type().typeref(), "[", LIST, node.size(), "]", LIST_END);
    }

    @Override
    public Object visitRescue(Rescue node, Object arg) {
        boolean insideEnsure = node.parent() instanceof NodeList && node.parent().parent() instanceof Ensure;
        if (!insideEnsure) {
            addAll(kw("begin"), INDENT);
        }
        format(node.body());
        for (Object clause : node.clauses()) {
            addAll(DEDENT, clause);
        }
        if (node.elseClause_size() > 0) {
            addAll(DEDENT, kw("else"), INDENT, node.elseClause());
        }
        if (!insideEnsure) {
            addAll(DEDENT, END);
        }
        return null;
    }

    @Override
    public Object visitEnsure(Ensure node, Object arg) {
        return addAll(
                kw("begin"), INDENT, node.body(), DEDENT,
                kw("ensure"), INDENT, node.ensureClause(), DEDENT,
                END);
    }

    @Override
    public Object visitRescueClause(RescueClause node, Object arg) {
        add(kw("rescue"));
        for (int i = 0; i < node.types_size(); ++i) {
            TypeName t = node.types(i);
            if (i > 0) {
                addAll(", ", NBR, t.typeref());
            } else {
                addAll(" ", NBR, t.typeref());
            }
        }
        if (node.name() != null) {
            addAll(" => ", node.name().identifier());
        }
        return addAll(INDENT, node.body());
    }

    @Override
    public Object visitClassAppendSelf(ClassAppendSelf node, Object arg) {
        return addAll(kw("class << self"), INDENT, node.body(), DEDENT, END);
    }

    private final Set<String> operators = new HashSet<>(Arrays.asList(
            "<=>", "==", "===", "!=", "=~", "!~", "<=", "<", ">", ">=",
            "^", "|", "&", "<<<", "<<", ">>", "+", "-", "*", "/", "%",
            "!", "~", "+@", "-@", "**"));

    private boolean isOperator(String name) {
        return operators.contains(name);
    }

    private final String[][] precedence = {
        {"||"},
        {"&&"},
        {"<=>", "==", "===", "!=", "=~", "!~"},
        {"<=", "<", ">", ">="},
        {"^", "|"},
        {"&"},
        {"<<<", "<<", ">>"},
        {"+", "-"},
        {"*", "/", "%"},
        {"!", "~", "+@", "-@"},
        {"**"}
    };

    private int operatorPrecedence(String op) {
        for (int i = 0; i < precedence.length; ++i) {
            for (int j = 0; j < precedence[i].length; ++j) {
                if (precedence[i][j].equals(op)) {
                    return i;
                }
            }
        }
        throw new IllegalArgumentException("Bad operator '" + op + "'");
    }

    private int compareOperators(String op1, String op2) {
        return operatorPrecedence(op1) - operatorPrecedence(op2);
    }

    private void handleOpArg(Node arg, String op) {
        if (arg instanceof CallSite) {
            String name = ((CallSite) arg).name().identifier();
            if (isOperator(name) && compareOperators(name, op) < 0) {
                addAll("(", LIST, arg, ")", LIST_END);
                return;
            }
        }
        format(arg);
    }

    public String DStringEscape(SimpleString node) {
        String value = node.value();
        if (!value.matches("[\\\"#\r\n]")) {
            return value;
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("#", "\\#")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    public String RegexpEscape(SimpleString node) {
        String value = node.value();
        if (!value.matches("[\\/#\r\n]")) {
            return value;
        }
        return value.replace("\\", "\\\\")
                .replace("/", "\\/")
                .replace("#", "\\#")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private void formatCondition(Node n) {
        add(SLP);
        formatSubCondition(n, "||");
        add(SRP);
    }

    private void formatSubCondition(Node n, String op) {
        while (n instanceof NodeList && ((NodeList) n).size() == 1) {
            n = ((NodeList) n).get(0);
        }
        if (n instanceof If) {
            if (formatAnd((If) n)) {
                return;
            }
        } else if (n instanceof NodeList) {
            if (formatOr((NodeList) n, op)) {
                return;
            }
        }
        format(n);
    }

    private boolean formatAnd(If n) {
        if (n.body_size() != 1) {
            return false;
        }
        formatSubCondition(n.condition(), "&&");
        addAll(kw(" && "), LI);
        formatSubCondition(n.body(0), "&&");
        return true;
    }

    private boolean formatOr(NodeList nl, String op) {
        if (nl.size() == 2 && nl.get(0) instanceof LocalAssignment) {
            LocalAssignment assignment = (LocalAssignment) nl.get(0);
            if (assignment.name().identifier().startsWith("$or$")) {
                if ("&&".equals(op)) {
                    addAll("(", LIST);
                }
                formatSubCondition(assignment.value(), "||");
                If n = (If) nl.get(1);
                addAll(kw(" || "), LI);
                formatSubCondition(n.elseBody(), "||");
                if ("&&".equals(op)) {
                    addAll(")", LIST_END);
                }
                return true;
            }
        }
        return false;
    }
}
