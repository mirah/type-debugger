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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import mirah.lang.ast.Node;
import org.mirah.typer.Typer;

class VariableTreeModel implements TreeModel, TypeModelLookup {
    private Typer typer;

    public void setTyper(Typer typer) {
        this.typer = typer;
    }

    public void clear() {
        vars.clear();
    }
    
    public void addVariable(String name, Object value) {
        vars.add(new VariableModel(
                name, lookup(value.getClass()), value, vars.size()));
    }
    
    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public Object getChild(Object parent, int index) {
        return getType(parent).getChild(((VariableModel)parent).getValue(), index);
    }

    @Override
    public int getChildCount(Object parent) {
        return getType(parent).getChildCount(((VariableModel)parent).getValue());
    }

    @Override
    public boolean isLeaf(Object node) {
         return getType(node).isLeaf(((VariableModel)node).getValue());
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return ((VariableModel)child).getIndex();
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
        listeners.add(l);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        listeners.remove(l);
    }

    private TypeModel getType(Object parent) {
        return ((VariableModel)parent).getType();
    }
    
    void refresh() {
        for (TreeModelListener l : listeners) {
            l.treeStructureChanged(
                    new TreeModelEvent(this, new Object[] { root }));
        }
    }

    @Override
    public <T> TypeModel<T> lookup(Class<T> klass) {
        if (typeCache.containsKey(klass)) {
            return typeCache.get(klass);
        }
        TypeModel<T> model;
        if (klass.isArray()) {
            model = new ArrayModel<>(klass, this);
        } else {
            try {
                klass.asSubclass(Collection.class);
                model = new CollectionModel(klass, this);
            } catch (ClassCastException ex) {
                model = lookupByClass(klass);
                try {
                    klass.asSubclass(Node.class);
                    model = new NodeTypeModel(klass, ((ClassModel)model).getParentType(), this, typer);
                } catch (ClassCastException ex2) {
                }
            }
        }
        typeCache.put(klass, model);
        return model;
    }
    
    private <T> ClassModel<T> lookupByClass(Class<T> klass) {
        if (klass == null) {
            return null;
        }
        if (classCache.containsKey(klass)) {
            return classCache.get(klass);
        }
        ClassModel model = new ClassModel(
                klass, lookupByClass(klass.getSuperclass()), this);
        classCache.put(klass, model);
        return model;           
    }

    final List<VariableModel> vars = new LinkedList<>();
    final VariableModel root =
            new VariableModel("root", new RootTypeModel(), vars, 0);
    final List<TreeModelListener> listeners = new LinkedList<>();
    final Map<Class, ClassModel> classCache = new HashMap<>();
    final Map<Class, TypeModel> typeCache = new HashMap<>();
}
