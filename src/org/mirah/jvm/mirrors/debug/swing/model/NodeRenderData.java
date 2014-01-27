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

import java.awt.Color;
import javax.swing.Icon;
import org.netbeans.swing.outline.RenderDataProvider;

/**
 *
 * @author ribrdb
 */
public class NodeRenderData implements RenderDataProvider {

    @Override
    public String getDisplayName(Object o) {
        return o.getClass().getSimpleName();
    }

    @Override
    public boolean isHtmlDisplayName(Object o) {
        return false;
    }

    @Override
    public Color getBackground(Object o) {
        return null;
    }

    @Override
    public Color getForeground(Object o) {
        return null;
    }

    @Override
    public String getTooltipText(Object o) {
        return o.toString();
    }

    @Override
    public Icon getIcon(Object o) {
        return null;
    }
    
}
