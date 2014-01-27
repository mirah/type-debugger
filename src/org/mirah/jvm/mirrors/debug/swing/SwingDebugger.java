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
 
package org.mirah.jvm.mirrors.debug.swing;

import java.util.concurrent.Executor;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.mirah.jvm.mirrors.debug.DebugController;
import org.mirah.jvm.mirrors.debug.DebugListener;
import org.mirah.jvm.mirrors.debug.DebuggerInterface;
import org.mirah.jvm.mirrors.debug.swing.view.DebuggerFrame;

class SwingDebugger implements DebugListener {
    private DebugController debugger = new DebugController(this, new Executor() {
        @Override
        public void execute(Runnable command) {
            SwingUtilities.invokeLater(command);
        }
    });
    private DebuggerFrame frame;
    
    public SwingDebugger() {
        debugger.step();
    }
    
    public DebuggerInterface getDebugger() {
        return debugger;
    }

    @Override
    public void stopped() {
        if (frame == null) {
            frame = new DebuggerFrame(debugger);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            frame.debuggerModel1.setDebugger(debugger);
        }
        frame.debuggerModel1.stopped(debugger.where());
    }
}
