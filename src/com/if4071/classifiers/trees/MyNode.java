package com.if4071.classifiers.trees;

import java.util.*;

/**
 * Created by angelynz95 on 04-Oct-16.
 */
public class MyNode {
    private int level;
    private Map<String, MyNode> children;
    private MyNode parent;
    private String label;

    public MyNode(String label, int level) {
        this.level = level;
        this.children = new LinkedHashMap<>();
        this.parent = null;
        this.label = label;
    }

    public MyNode(String label, int level, MyNode parent) {
        this.level = level;
        this.children = new LinkedHashMap<>();
        this.parent = parent;
        this.label = label;
    }

    public int getLevel() {
        return level;
    }

    public Map<String, MyNode> getChildren() {
        return children;
    }

    public MyNode getParent() {
        return parent;
    }

    public String getLabel() {
        return label;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setChildren(Map<String, MyNode> children) {
        this.children = children;
    }

    public void setParent(MyNode parent) {
        this.parent = parent;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public MyNode getChild(String attributeValue) {
        return children.get(attributeValue);
    }

    public void addChild(String attributeValue, MyNode child) {
        children.put(attributeValue, child);
    }

    public int numChildren() {
        return children.size();
    }

    public void removeAllChildren() {
        children.clear();
    }

    public boolean isParentChildrenLeaf() {
        List<MyNode> parentChildren = new ArrayList<>(parent.getChildren().values());
        for (int i = 0; i < parentChildren.size(); i++) {
            if (!parentChildren.get(i).isLeaf()) {
                return false;
            }
        }
        return true;
    }

    public boolean isRoot() {
        return parent == null;
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public void print(String tab, String attributeValue) {
        System.out.println(tab + attributeValue + label);
        for (Map.Entry<String, MyNode> child : children.entrySet()) {
            child.getValue().print(tab + "\t", child.getKey() + " - ");
        }
    }

    public static void main(String[] args) {
        MyNode node = new MyNode("Outlook", 0);
        node.addChild("Sunny", new MyNode("Humidity", 1, node));
        node.addChild("Overcast", new MyNode("Yes", 1, node));
        node.addChild("Rain", new MyNode("Wind", 1, node));
        MyNode child = node.getChild("Sunny");
        child.addChild("High", new MyNode("No", 2, child));
        child.addChild("Normal", new MyNode("Yes", 2, child));
        child = node.getChild("Rain");
        child.addChild("Strong", new MyNode("No", 2, child));
        child.addChild("Weak", new MyNode("Yes", 2, child));
        node.print("", "");
    }
}
