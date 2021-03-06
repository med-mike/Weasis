/*******************************************************************************
 * Copyright (c) 2016 Weasis Team and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nicolas Roduit - initial API and implementation
 *******************************************************************************/
package org.weasis.core.api.explorer.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class Tree<T> {

    private final T head;

    private final ArrayList<Tree<T>> leafs = new ArrayList<>();

    private volatile Tree<T> parent = null;

    private volatile HashMap<T, Tree<T>> locate = new HashMap<>();

    public Tree(T head) {
        this.head = head;
        locate.put(head, this);
    }

    public void addLeaf(T root, T leaf) {
        if (locate.containsKey(root)) {
            locate.get(root).addLeaf(leaf);
        } else {
            addLeaf(root).addLeaf(leaf);
        }
    }
    
    private Tree<T> addLeaf(T leaf) {
        Tree<T> t = new Tree<>(leaf);
        leafs.add(t);
        t.parent = this;
        t.locate = this.locate;
        locate.put(leaf, t);
        return t;
    }

    public void removeLeaf(T leaf) {
        Tree<T> t = locate.remove(leaf);
        if (t != null) {
            t.parent.leafs.remove(t);
            t.parent = null;
            t.locate = null;
        }
    }

    public Tree<T> setAsParent(T parentRoot) {
        Tree<T> t = new Tree<>(parentRoot);
        t.leafs.add(this);
        this.parent = t;
        t.locate = this.locate;
        t.locate.put(head, this);
        t.locate.put(parentRoot, t);
        return t;
    }

    public T getHead() {
        return head;
    }

    public Tree<T> getTree(T element) {
        return locate.get(element);
    }

    public Tree<T> getParent() {
        return parent;
    }

    public Collection<T> getSuccessors(T root) {
        Collection<T> successors = new ArrayList<>();
        Tree<T> tree = getTree(root);
        if (null != tree) {
            for (Tree<T> leaf : tree.leafs) {
                successors.add(leaf.head);
            }
        }
        return successors;
    }

    public Collection<Tree<T>> getSubTrees() {
        return leafs;
    }

    public static <T> Collection<T> getSuccessors(T of, Collection<Tree<T>> in) {
        for (Tree<T> tree : in) {
            if (tree.locate.containsKey(of)) {
                return tree.getSuccessors(of);
            }
        }
        return new ArrayList<>();
    }

    @Override
    public String toString() {
        return printTree(0);
    }

    public void clear() {
        locate.clear();
        leafs.clear();
        locate.put(head, this);
    }

    private String printTree(int increment) {
        char[] value = new char[increment];
        for (int i = 0; i < value.length; i++) {
            value[i] = ' ';
        }
        String inc = new String(value);
        StringBuilder s = new StringBuilder(inc + head);
        for (Tree<T> child : leafs) {
            s.append("\n"); //$NON-NLS-1$
            s.append(child.printTree(increment + 2));
        }
        return s.toString();
    }
}
