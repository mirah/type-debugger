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

import java.util.LinkedHashMap;
import java.util.Map;
import org.mirah.jvm.mirrors.debug.StackEntry;
import org.mirah.typer.Typer;

public class FrameModel {
    public FrameModel(StackEntry frame) {
        this.frame = frame;
    }
    public NodeModel getNode() {
        return new NodeModel(frame.node());
    }
    public Typer getTyper() {
        return (Typer)frame.context().get(Typer.class);
    }
    public Map<String, Object> getVars() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("node", frame.node());
        if (frame.result() != null) {
            result.put("ret", frame.result());
        }
        if (frame.watch() != null) {
            result.put("future", frame.watch().future());
            result.put("oldType", frame.watch().currentValue());
            result.put("newType", frame.watch().newValue());
        }
        return result;
    }
    public String toString() {
        NodeModel n = getNode();
        return n + " (" + n.getFilename() + ":" + n.getLineNumber() + ")";
    }
    public FrameModel getParent() {
        if (frame.parent() == null) {
            return null;
        }
        return new FrameModel(frame.parent());
    }
    private final StackEntry frame;
}
