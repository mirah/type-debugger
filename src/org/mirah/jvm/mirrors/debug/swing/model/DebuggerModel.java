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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import mirah.lang.ast.Break;
import mirah.lang.ast.Node;
import org.mirah.jvm.mirrors.debug.Breakpoint;
import org.mirah.jvm.mirrors.debug.DebugController;
import org.mirah.jvm.mirrors.debug.StackEntry;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.OutlineModel;

public class DebuggerModel {
    private List<FrameModel> stack;
    private FrameModel currentFrame;
    private List<NodeModel> asts = new ArrayList<>();
    private NodeModel selectedNode;

    private OutlineModel ast;
    private final VariableTreeModel vars = new VariableTreeModel();
    private final OutlineModel varsOutline =
            DefaultOutlineModel.createOutlineModel(vars, new VariableRowModel());

    private DefaultListModel<Breakpoint> breakpoints = new DefaultListModel<>();
    private DebugController debugger;

    public DefaultListModel<Breakpoint> getBreakpoints() {
        return breakpoints;
    }
    
    public OutlineModel getVars() {
        return varsOutline;
    }

    public OutlineModel getAst() {
        return ast;
    }
    
    private synchronized void setAst(OutlineModel ast) {
        OutlineModel oldAst = this.ast;
        this.ast = ast;
        propertyChangeSupport.firePropertyChange("ast", oldAst, ast);
    }

    public List<FrameModel> getStack() {
        return stack;
    }

    private synchronized void setStack(List<FrameModel> stack) {
        List<FrameModel> oldStack = this.stack;
        this.stack = stack;
        propertyChangeSupport.firePropertyChange("stack", oldStack, stack);
    }
    
    public FrameModel getCurrentFrame() {
        return currentFrame;
    }
    
    public synchronized void setCurrentFrame(FrameModel frame) {
        if (Objects.equals(currentFrame, frame) || frame == null) {
            return;
        }
        FrameModel oldFrame = this.currentFrame;
        currentFrame = frame;
        propertyChangeSupport.firePropertyChange(
                "currentFrame", oldFrame, frame);
        setSelectedNode(frame.getNode());
    }
    
    public void refreshVars() {
        FrameModel frame = getCurrentFrame();
        vars.setTyper(frame.getTyper());
        vars.clear();
        for (Map.Entry<String, Object> item : frame.getVars().entrySet()) {
            vars.addVariable(item.getKey(), item.getValue());
        }
        NodeModel node = getSelectedNode();
        if (node != null && !node.equals(frame.getNode())) {
            vars.addVariable("selection", node.getNode());
        }
        int index = 1;
        if (debugger != null) {
            for (Object future : debugger.watches().keySet()) {
                vars.addVariable("watch " + index, future);
                index += 1;
            }
        }
        vars.refresh();
    }

    private boolean running;

    public static final String PROP_RUNNING = "running";

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        boolean oldRunning = this.running;
        this.running = running;
        propertyChangeSupport.firePropertyChange(PROP_RUNNING, oldRunning, running);
        propertyChangeSupport.firePropertyChange("stopped", oldRunning, running);
    }

    public boolean isStopped() {
        return !running;
    }
    
    public synchronized void stopped(StackEntry state) {
        List<FrameModel> frames = new LinkedList<>();
        FrameModel f = new FrameModel(state);
        while (f != null) {
            frames.add(f);
            f = f.getParent();
        }
        setStack(frames);
        setCurrentFrame(frames.get(0));
        setRunning(false);
    }
    
    public List<NodeModel> getAsts() {
        return asts;
    }

    public NodeModel getSelectedScript() {
        TreePath path = getSelectedNodePath();
        if (path == null) {
            return null;
        }
        return new NodeModel((Node) path.getPathComponent(0));
    }
    
    public void setSelectedScript(NodeModel script) {
        setSelectedNode(script);
    }
    
    public synchronized NodeModel getSelectedNode() {
        return selectedNode;
    }

    public synchronized void setSelectedNode(NodeModel node) {
        NodeModel oldNode = this.selectedNode;
        if (Objects.equals(node, oldNode) || node == null) {
            return;
        }
        TreePath oldPath =
                propertyChangeSupport.hasListeners("selectedNodePath") ?
                getSelectedNodePath() : null;
        NodeModel oldScript = getSelectedScript();
        TreeModel oldModel = ast;
        selectedNode = node;
        NodeModel newScript = getSelectedScript();
        if (!Objects.equals(newScript, oldScript)) {
            setAst(DefaultOutlineModel.createOutlineModel(
                    new NodeTreeModel(newScript.getNode()), new NodeRowModel()));
        }
        refreshVars();
        if (!asts.contains(newScript)) {
            asts.add(newScript);
            propertyChangeSupport.firePropertyChange(
                "asts", asts, asts);
        }
        propertyChangeSupport.firePropertyChange(
                "selectedNode", oldNode, node);
        propertyChangeSupport.firePropertyChange(
                "selectedNodePath", oldPath, getSelectedNodePath());
        propertyChangeSupport.firePropertyChange(
                "selectedScript", oldScript, newScript);
    }
    
    public TreePath getSelectedNodePath() {
        if (selectedNode == null) {
            return null;
        }
        return NodeModel.createPath(selectedNode.getNode());
    }
    
    public void setSelectedNodePath(TreePath p) {
        Node n = (Node)p.getLastPathComponent();
        if (n != null) {
            setSelectedNode(new NodeModel(n));
        }
    }
    
    private transient final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public synchronized void addPropertyChangeListener(String property, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(property, listener);
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public synchronized void removePropertyChangeListener(String property, PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(property, listener);
    }

    public void setDebugger(DebugController debugger) {
        this.debugger = debugger;
    }
}
