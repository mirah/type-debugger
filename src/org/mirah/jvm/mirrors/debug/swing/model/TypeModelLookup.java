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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import mirah.lang.ast.Node;
import org.mirah.typer.TypeFuture;
import org.mirah.typer.Typer;

interface TypeModel<T> {
    boolean isLeaf(T value);
    VariableModel getChild(T parent, int index);
    int getChildCount(T parent);
}

public interface TypeModelLookup {
    <T> TypeModel<T> lookup(Class<T> klass);
}

class ClassModel<T> implements TypeModel<T> {
    private final Class<T> type;
    private final List<Field> fields;
    private final TypeModel<? super T> parentType;
    protected final TypeModelLookup lookup;

    public TypeModel<? super T> getParentType() {
        return parentType;
    }

    public ClassModel(Class<T> type, TypeModel<? super T> parentType, TypeModelLookup lookup) {
        this.type = type;
        this.parentType = parentType;
        this.lookup = lookup;
        Field[] allFields = type.getDeclaredFields();
        fields = new ArrayList<>(allFields.length);
        for (Field f : allFields) {
            if (!Modifier.isStatic(f.getModifiers())) {
                f.setAccessible(true);
                fields.add(f);
            }
        }
        Collections.sort(fields, new Comparator<Field>() {
            @Override
            public int compare(Field o1, Field o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
    }

    @Override
    public String toString() {
        return type.toString();
    }
    
    @Override
    public boolean isLeaf(T value) {
        if (type.isPrimitive() || value == null) {
            return true;
        }
        return (value instanceof String || value instanceof Number || value instanceof Boolean);
    }
    
    @Override
    public VariableModel getChild(T parent, int index) {
        if (index >= fields.size()) {
            VariableModel result = parentType.getChild(parent, index - fields.size());
            result.setIndex(index);
            return result;
        }
        Field f = fields.get(index);
        Object value = null;
        try {
            value = f.get(parent);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(ClassModel.class.getName()).log(Level.SEVERE, null, ex);
        }
        Class klass = value == null ? f.getType() : value.getClass();
        return new VariableModel(f.getName(), lookup.lookup(klass), value, index);
    }

    @Override
    public int getChildCount(T parent) {
        return fields.size() +
                (parentType == null ? 0 : parentType.getChildCount(parent));
    }
}

class NodeTypeModel<T extends Node> extends ClassModel<T> {
    private Typer typer;

    public NodeTypeModel(Class<T> type, TypeModel<? super T> parentType, TypeModelLookup lookup, Typer typer) {
        super(type, parentType, lookup);
        this.typer = typer;
    }

    @Override
    public int getChildCount(T parent) {
        return 1 + super.getChildCount(parent);
    }

    @Override
    public VariableModel getChild(T parent, int index) {
        if (index == 0) {
            TypeFuture future = typer == null ? null : typer.getInferredType(parent);
            TypeModel type;
            if (future == null) {
                type = lookup.lookup(TypeFuture.class);
            } else {
                type = lookup.lookup(future.getClass());
            }
            return new VariableModel("(type)", type, future, 0);
        }
        VariableModel result = super.getChild(parent, index - 1);
        result.setIndex(index);
        return result;
    }
    
}

class CollectionModel<T extends Collection> implements TypeModel<T> {
    private final Class klass;
    private final TypeModelLookup lookup;

    public CollectionModel(Class<T> klass, TypeModelLookup lookup) {
        this.klass = klass;
        this.lookup = lookup;
    }
    
    @Override
    public String toString() {
        return klass.toString();
    }
    
    @Override
    public boolean isLeaf(Collection value) {
        return value == null;
    }

    @Override
    public VariableModel getChild(Collection parent, int index) {
        if (parent == null) {
            return null;
        }
        Object child;
        if (parent instanceof List) {
            child = ((List)parent).get(index);
        } else {
            Iterator it = parent.iterator();
            for (int i = 0; i < index; ++i) {
                it.next();
            }
            child = it.next();
        }
        TypeModel type;
        if (child == null) {
            type = lookup.lookup(Object.class);
        } else {
            type = lookup.lookup(child.getClass());
        }
        return new VariableModel("[" + index + "]", type, child, index);
    }

    @Override
    public int getChildCount(Collection parent) {
        if (parent == null) {
            return 0;
        }
        return parent.size();
    }

}

class RootTypeModel implements TypeModel<List<VariableModel>> {
    @Override
    public boolean isLeaf(List<VariableModel> value) {
        return false;
    }

    @Override
    public VariableModel getChild(List<VariableModel> parent, int index) {
        //return ((List<VariableModel>)parent.getValue()).get(index);
        return parent.get(index);
    }

    @Override
    public int getChildCount(List<VariableModel> parent) {
        //return ((List<VariableModel>)parent.getValue()).size();
        return parent.size();
    }
}

class ArrayModel<T> implements TypeModel<T> {
    private final Class type;
    private final TypeModelLookup lookup;

    public ArrayModel(Class type, TypeModelLookup lookup) {
        this.type = type;
        this.lookup = lookup;
    }
    
    @Override
    public boolean isLeaf(Object value) {
        return value == null;
    }

    @Override
    public VariableModel getChild(Object parent, int index) {
        Object value = Array.get(parent, index);
        Class klass = value == null ? type.getComponentType() : value.getClass();
        return new VariableModel("" + index, lookup.lookup(klass), parent, index);
    }

    @Override
    public int getChildCount(Object parent) {
        return Array.getLength(parent);
    }
    
}