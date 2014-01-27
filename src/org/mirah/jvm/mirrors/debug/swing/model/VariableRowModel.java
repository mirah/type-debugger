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
import java.util.Collection;
import org.netbeans.swing.outline.RowModel;

public class VariableRowModel implements RowModel {

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueFor(Object o, int i) {
        VariableModel v = (VariableModel)o;
        if (i == 0) {
            return v.getType();
        } else if (v.getType().isLeaf(v.getValue())) {
            return "" + v.getValue();
        } else if (v.getValue() instanceof Collection) {
            return "size: " + ((Collection)v.getValue()).size();
        } else if (v.getType() instanceof ArrayModel) {
            return "length: " + Array.getLength(v.getValue());
        } else {
            return "#" + System.identityHashCode(v.getValue());
        }
    }

    @Override
    public Class getColumnClass(int i) {
        if (i == 0) {
            return TypeModel.class;
        } else {
            return String.class;
        }
    }

    @Override
    public boolean isCellEditable(Object o, int i) {
        return false;
    }

    @Override
    public void setValueFor(Object o, int i, Object o1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getColumnName(int i) {
        if (i == 0) {
            return "Class";
        } else {
            return "Value";
        }
    }
    
}
