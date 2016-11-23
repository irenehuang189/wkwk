package com.if4071.clusterers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by angelynz95 on 04-Oct-16.
 */
public class MyAgnesNode {
    private Map<String, MyAgnesNode> children;
    private MyAgnesNode parent;
    private String label;

    public MyAgnesNode(String label) {
        this.children = new LinkedHashMap<>();
        this.parent = null;
        this.label = label;
    }

    public MyAgnesNode(String label, MyAgnesNode parent) {
        this.children = new LinkedHashMap<>();
        this.parent = parent;
        this.label = label;
    }

    public Map<String, MyAgnesNode> getChildren() {
        return children;
    }

    public MyAgnesNode getParent() {
        return parent;
    }

    public String getLabel() {
        return label;
    }

    public void setChildren(Map<String, MyAgnesNode> children) {
        this.children = children;
    }

    public void setParent(MyAgnesNode parent) {
        this.parent = parent;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public MyAgnesNode getChild(String attributeValue) {
        return children.get(attributeValue);
    }

    public void addChild(String attributeValue, MyAgnesNode child) {
        children.put(attributeValue, child);
    }

    public int numChildren() {
        return children.size();
    }

    public void removeAllChildren() {
        children.clear();
    }

    public boolean isParentChildrenLeaf() {
        List<MyAgnesNode> parentChildren = new ArrayList<>(parent.getChildren().values());
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
        for (Map.Entry<String, MyAgnesNode> child : children.entrySet()) {
            child.getValue().print(tab + "\t", child.getKey() + " - ");
        }
    }

    public static void main(String[] args) {
        MyAgnesNode node = new MyAgnesNode("Outlook");
        node.addChild("Sunny", new MyAgnesNode("Humidity", node));
        node.addChild("Overcast", new MyAgnesNode("Yes", node));
        node.addChild("Rain", new MyAgnesNode("Wind", node));
        MyAgnesNode child = node.getChild("Sunny");
        child.addChild("High", new MyAgnesNode("No", child));
        child.addChild("Normal", new MyAgnesNode("Yes", child));
        child = node.getChild("Rain");
        child.addChild("Strong", new MyAgnesNode("No", child));
        child.addChild("Weak", new MyAgnesNode("Yes", child));
        node.print("", "");
    }
}
