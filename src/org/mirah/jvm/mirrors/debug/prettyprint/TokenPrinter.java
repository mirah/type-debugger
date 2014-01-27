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
import java.util.List;

public class TokenPrinter implements TokenVisitor {

    private static final String NL = String.format("%n");
    private StringBuilder buf = new StringBuilder();
    private int lineLength = 0;
    private int limit = 80;
    private int indentSpace = 2;
    private int tokenIndex = 0;
    private int currentIndent = 0;
    private int continuationSpace = 0;
    private SavedIndent savedIndent = new SavedIndent();
    private SavedPosition repairPoint = null;
    private ListState list = new NoList();
    private final List<Token> tokens;

    public TokenPrinter(List<Token> tokens) {
        this.tokens = tokens;
    }
    
    public String toString() {
        if (tokenIndex != tokens.size()) {
            printTokens();
        }
        return buf.toString();
    }

    private void printTokens() {
        while (tokenIndex < tokens.size()) {
            tokens.get(tokenIndex).accept(this);
            ++tokenIndex;
        }
    }
    
    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getIndentSpace() {
        return indentSpace;
    }

    public void setIndentSpace(int indentSpace) {
        this.indentSpace = indentSpace;
    }

    public void visitString(StringToken t) {
        maybeIndent();
        if (lineLength + t.length() > limit && tryRepair()) {
            return;
        }
        buf.append(t.getValue());
        lineLength += t.length();
    }

    private void maybeIndent() {
        if (lineLength == 0) {
            int indent = getIndent();
            if (indent > 0) {
                buf.append(String.format("%" + indent + "s", ""));
                lineLength += indent;
            }
        }
    }

    @Override
    public void visitNewline(NewlineToken t) {
        endLine();
    }
    
    private boolean endLine() {
        if (!allowNewline()) {
            return false;
        }
        continuationSpace = savedIndent.continuation;
        startLine();
        return true;
    }

    private void startLine() {
        buf.append(NL);
        lineLength = 0;
        repairPoint = null;
    }

    @Override
    public void visitSoftNewline(SoftNewlineToken t) {
        repairPoint = new SoftBreak();
    }

    @Override
    public void visitListSeparator(ListSeparatorToken t) {
        repairPoint = new ListItemBreak();
    }    
    
    @Override
    public void visitIndent(IndentToken t) {
        savedIndent = new SavedIndent();
        if (endLine()) {
            currentIndent += indentSpace;
        }
    }

    private void indent(int amount) {
        savedIndent = new SavedIndent();
        currentIndent += amount;
    }

    @Override
    public void visitDedent(DedentToken t) {
        if (endLine()) {
            dedent();
        }
    }

    private void dedent() {
        savedIndent.restore();
    }

    @Override
    public void visitListStart(ListStartToken t) {
        final SingleLineList list = new SingleLineList();
        this.list = list;
        this.repairPoint = list;
    }

    @Override
    public void visitListEnd(ListEndToken t) {
        list.endList();
    }

    @Override
    public void visitSoftLParen(SoftLParenToken t) {
        SoftParenList list = new SoftParenList();
        this.list = list;
        this.repairPoint = list;
    }

    @Override
    public void visitSoftRParen(SoftRParenToken t) {
        list.endList();
    }
    

    private int getIndent() {
        return currentIndent + continuationSpace;
    }

    private boolean tryRepair() {
        if (repairPoint == null) {
            return false;
        }
        SavedPosition best = repairPoint;
        for (SavedPosition current = repairPoint; current != null; current = current.previous) {
            if (current.priority() > best.priority()) {
                best = current;
            } else if (current.priority() == best.priority()) {
                if (best.linePos > limit && current.linePos < best.linePos) {
                    best = current;
                }
            }
        }
        return best.repair();
    }

    private boolean allowNewline() {
        double newlinePriority = Math.scalb(1.5, -list.getDepth());

        SavedPosition best = null;
        for (SavedPosition current = repairPoint; current != null; current = current.previous) {
            if (current.priority() <= newlinePriority) {
                continue;
            }
            if (best == null || current.priority() > best.priority()) {
                best = current;
            }
        }
        if (best == null) {
            return true;
        } else {
            best.repair();
            return false;
        }
    }
    
    class SavedIndent {
        protected final SavedIndent previous = savedIndent;
        private final int indent = currentIndent;
        private final int continuation = continuationSpace;
        
        void restore() {
            currentIndent = indent;
            continuationSpace = continuation;
            savedIndent = previous;
        }
    }
    
    interface ListState {

        void endList();

        ListState getPreviousList();
        
        int getDepth();
    }

    abstract class SavedPosition {

        private final int bufferPos = buf.length();
        protected final int linePos = lineLength;
        protected final int savedToken = tokenIndex;
        protected final SavedPosition previous = repairPoint;
        private final SavedIndent indentState = savedIndent;
        private final int indent = currentIndent;
        private final int savedContinuation = continuationSpace;
        protected final ListState savedList = list;

        protected void restore() {
            tokenIndex = savedToken;
            buf.setLength(bufferPos);
            lineLength = linePos;
            repairPoint = previous;
            currentIndent = indent;
            continuationSpace = savedContinuation;
            list = savedList;
            savedIndent = indentState;
        }
        abstract boolean repair();

        abstract double priority();
    }

    class SoftBreak extends SavedPosition {
        private double priority = Math.scalb(1.0, -savedList.getDepth());
        @Override
        boolean repair() {
            restore();
            startLine();
            continuationSpace = savedIndent.continuation + indentSpace * 2;
            return true;
        } 
        double priority() {
            return priority;
        }
    }

    class ListItemBreak extends SavedPosition {
         private double priority = Math.scalb(2.0, -savedList.getDepth());

        @Override
        boolean repair() {
            restore();
            startLine();
            continuationSpace = savedIndent.continuation;
            return true;
        }

        @Override
        double priority() {
            return priority;
        }
       
    }
    
    class NoList implements ListState {

        @Override
        public void endList() {
            throw new IllegalStateException();
        }

        @Override
        public ListState getPreviousList() {
            return this;
        }

        @Override
        public int getDepth() {
            return 0;
        }
    }

    class SingleLineList extends SavedPosition implements ListState {
        private double priority = Math.scalb(3.0, -getDepth());

        @Override
        public void endList() {
            list = savedList;
        }

        @Override
        public ListState getPreviousList() {
            return savedList;
        }

        @Override
        boolean repair() {
            restore();
            startLine();
            indent(indentSpace * 2);
            list = new WrappedList();
            return true;
        }

        @Override
        public int getDepth() {
            return 1 + getPreviousList().getDepth();
        }

        @Override
        double priority() {
            return priority;
        }
    }
    
    class WrappedList implements ListState {

        private ListState previous = list;

        @Override
        public void endList() {
            dedent();
            list = previous;
        }

        @Override
        public ListState getPreviousList() {
            return previous;
        }

        @Override
        public int getDepth() {
            return 1 + getPreviousList().getDepth();
        }
    }
    
    class SoftParenList extends SingleLineList {
        private boolean visible = false;
        @Override
        protected void restore() {
            super.restore();
            visible = true;
            maybeIndent();
            buf.append("(");
        }

        @Override
        boolean repair() {
            if (super.repair()) {
                list = this;
                return true;
            }
            return false;
        }

        @Override
        public void endList() {
            if (visible) {
                maybeIndent();
                buf.append(")");
                lineLength += 1;
                dedent();
            }
            super.endList();
        }
        
    }
}
