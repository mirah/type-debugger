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
 
package org.mirah.jvm.mirrors.debug.swing.model;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javax.swing.tree.TreePath;
import mirah.lang.ast.Fixnum;
import mirah.lang.ast.Float;
import mirah.lang.ast.Named;
import mirah.lang.ast.Node;
import mirah.lang.ast.Position;
import mirah.lang.ast.Script;
import mirah.lang.ast.SimpleString;
import org.mirah.jvm.mirrors.debug.prettyprint.MirahSourceGenerator;

public class NodeModel {

    public NodeModel(Node node) {
        this.node = node;
    }

    public Node getNode() {
        return node;
    }

    public String getFilename() {
        Position p = node.position();
        if (p != null) {
            File f = new File(p.source().name());
            return f.getName();
        }
        return "";
    }

    public String getSourceCode() {
        if (node.position() != null) {
            return node.position().source().contents();
        }
        return "";
    }
    
    public String getGeneratedCode() {
        MirahSourceGenerator generator = new MirahSourceGenerator();
        node.accept(generator, null);
        return generator.toString();
    }
    
    public int getStartChar() {
        if (node.position() != null) {
            return node.position().startChar();
        }
        return 0;
    }
    
    public int getEndChar() {
        if (node.position() != null) {
            return node.position().endChar();
        }
        return 0;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.node);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NodeModel other = (NodeModel) obj;
        return Objects.equals(this.node, other.node);
    }
    
    @Override
    public String toString() {
        return node.getClass().getSimpleName();
    }
    
    public String getName() {
        if (node instanceof Named) {
            return ((Named)node).name().identifier();
        } else if (node instanceof Fixnum) {
            return Long.toString(((Fixnum)node).value());
        } else if (node instanceof Float) {
            return Double.toString(((Float)node).value());
        } else if (node instanceof SimpleString) {
            return "\"" + ((SimpleString)node).value() + "\"";
        }
        return "";
    }
    
    public TreePath getPath() {
        if (path == null) {
            path = createPath(node);
        }
        return path;
    }

    public static TreePath createPath(Node n) {
        List<Node> elems = new LinkedList();
        while (n != null) {
            elems.add(n);
            n = n.parent();
        }
        Collections.reverse(elems);
        return new TreePath(elems.toArray());
    }
    private final Node node;
    private TreePath path;

    int getLineNumber() {
        if (node.position() != null) {
            return node.position().startLine();
        }
        return 0;        
    }
}
