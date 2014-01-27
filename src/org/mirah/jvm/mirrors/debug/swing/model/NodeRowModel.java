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

import mirah.lang.ast.Fixnum;
import mirah.lang.ast.Named;
import mirah.lang.ast.SimpleString;
import org.netbeans.swing.outline.RowModel;

/**
 *
 * @author ribrdb
 */
public class NodeRowModel implements RowModel{

    @Override
    public int getColumnCount() {
        return 1;
    }

    @Override
    public Object getValueFor(Object o, int i) {
        if (o instanceof Named) {
            return ((Named)o).name().identifier();
        } else if (o instanceof Fixnum) {
            return "" + ((Fixnum)o).value();
        } else if (o instanceof mirah.lang.ast.Float) {
            return "" + ((mirah.lang.ast.Float)o).value();
        } else if (o instanceof SimpleString) {
            return "\"" + ((SimpleString)o).value() + "\"";
        } else {
            return "";
        }
    }

    @Override
    public Class getColumnClass(int i) {
        return String.class;
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
        return "Name";
    }
    
}
