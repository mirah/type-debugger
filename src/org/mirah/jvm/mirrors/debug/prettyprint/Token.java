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

public interface Token {
    void accept(TokenVisitor l);
}

interface TokenVisitor {
    void visitString(StringToken t);
    void visitNewline(NewlineToken t);
    void visitSoftNewline(SoftNewlineToken t);
    void visitIndent(IndentToken t);
    void visitDedent(DedentToken t);
    void visitListStart(ListStartToken t);
    void visitListSeparator(ListSeparatorToken t);
    void visitListEnd(ListEndToken t);
    void visitSoftLParen(SoftLParenToken t);
    void visitSoftRParen(SoftRParenToken t);
}

class StringToken implements Token {  
    private final String value;

    public StringToken(String value) {
        this.value = value;
    }
    
    public StringToken(String format, Object... args) {
        this.value = String.format(format, args);
    }

    public String getValue() {
        return value;
    }
    public void accept(TokenVisitor l) {
        l.visitString(this);
    }

    int length() {
        return value.length();
    }
}

class NewlineToken implements Token {

    @Override
    public void accept(TokenVisitor l) {
        l.visitNewline(this);
    }
}

class SoftNewlineToken implements Token {

    @Override
    public void accept(TokenVisitor l) {
        l.visitSoftNewline(this);
    }
    
}

class IndentToken implements Token {

    @Override
    public void accept(TokenVisitor l) {
        l.visitIndent(this);
    }
    
}

class DedentToken implements Token {

    @Override
    public void accept(TokenVisitor l) {
        l.visitDedent(this);
    }
    
}

class ListStartToken implements Token {

    @Override
    public void accept(TokenVisitor l) {
        l.visitListStart(this);
    }
    
}

class ListEndToken implements Token {

    @Override
    public void accept(TokenVisitor l) {
        l.visitListEnd(this);
    }
    
}

class ListSeparatorToken implements Token {

    @Override
    public void accept(TokenVisitor l) {
        l.visitListSeparator(this);
    }
    
}

class SoftLParenToken implements Token {
    @Override
    public void accept(TokenVisitor l) {
        l.visitSoftLParen(this);
    }
}

class SoftRParenToken implements Token {
    @Override
    public void accept(TokenVisitor l) {
        l.visitSoftRParen(this);
    }
}

class StyledToken extends StringToken {
    private int length;

    public StyledToken(String style, String value) {
        super("<span class='" + style + "'>" + escape(value) + "</span>");
        this.length = value.length();
    }

    @Override
    int length() {
        return length;
    }
    
    static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}

class EscapedToken extends StringToken {
    private int length;
    public EscapedToken(String value) {
        super(StyledToken.escape(value));
        length = value.length();
    }
    @Override
    int length() { return length; }
}

class TagToken extends StringToken {
    public TagToken(String value) {
        super(value);
    }
    int length() { return 0; }
}