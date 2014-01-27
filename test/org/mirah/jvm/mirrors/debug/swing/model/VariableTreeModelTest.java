/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mirah.jvm.mirrors.debug.swing.model;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import mirah.lang.ast.Script;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mirah.typer.Typer;

/**
 *
 * @author ribrdb
 */
public class VariableTreeModelTest {
    
    public VariableTreeModelTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testAddVariable() {
        System.out.println("addVariable");
        String name = "node";
        Object value = new Script();
        VariableTreeModel instance = new VariableTreeModel();
        instance.addVariable(name, value);

        Object child = instance.getChild(instance.getRoot(), 0);
        List<String> items = new LinkedList<>();
        for (int i = 0; i < instance.getChildCount(child); ++i) {
            items.add(((VariableModel)instance.getChild(child, i)).getName());
        }
        assertEquals(Arrays.asList("(type)", "body", "clone_listeners", "originalNode", "parent", "position"), items);
    }

    
}
