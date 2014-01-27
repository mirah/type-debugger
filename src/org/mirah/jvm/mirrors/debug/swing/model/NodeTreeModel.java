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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import mirah.lang.ast.Fixnum;
import mirah.lang.ast.Node;
import mirah.lang.ast.NodeScanner;
import mirah.lang.ast.Null;
import mirah.lang.ast.SimpleString;

class NodeTreeModel implements TreeModel {
    private Node root;
    private final Map<Object, List<Node>> childCache = new HashMap<>();

    public NodeTreeModel(Node root) {
        this.root = root;
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public Object getChild(Object parent, int index) {
        return getList(parent).get(index);
    }

    @Override
    public int getChildCount(Object parent) {
        return getList(parent).size();
    }

    @Override
    public boolean isLeaf(Object node) {
        return node instanceof SimpleString || node instanceof Fixnum
                || node instanceof mirah.lang.ast.Float || node instanceof Null;
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return getList(parent).indexOf(child);
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
    }
    
    private synchronized List<Node> getList(final Object parent) {
        if (childCache.containsKey(parent)) {
            return childCache.get(parent);
        } else {
            final List<Node> children = new ArrayList<>();
            NodeScanner scanner = new NodeScanner() {
                @Override
                public boolean enterDefault(Node node, Object arg) {
                    if (node == parent) {
                        return true;
                    } else if (node != null) {
                        children.add(node);
                    }
                    return false;
                }
            };
            scanner.scan((Node)parent);
            childCache.put(parent, children);
            return children;
        }
    }
}
