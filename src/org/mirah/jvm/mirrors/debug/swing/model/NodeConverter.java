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

import java.util.HashMap;
import java.util.Map;
import mirah.lang.ast.Node;
import org.jdesktop.beansbinding.Converter;

public class NodeConverter extends Converter {

    @Override
    public Object convertForward(Object value) {
        System.out.println(value);
        NodeModel node = (NodeModel)value;
        Map<String, Object> map = new HashMap<>();
        map.put("column0", node.getNode());
        map.put("column1", node.getName());
        return map;
    }

    @Override
    public Object convertReverse(Object value) {
        System.out.println(value);
        return new NodeModel((Node) ((Map)value).get("column0"));
    }
    
}
