package com.if4071.classifiers.trees;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by angelynz95 on 04-Oct-16.
 */
public class MyNode {
    private Map<String, MyNode> children = new LinkedHashMap<>();
    private String value;

    public MyNode() {

    }

    public MyNode(String value) {
        this.value = value;
    }

    public Map<String, MyNode> getChildren() {
        return children;
    }

    public String getValue() {
        return value;
    }

    public void setChildren(Map<String, MyNode> children) {
        this.children = children;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public MyNode getChild(String attributeValue) {
        return children.get(attributeValue);
    }

    public void addChild(String attributeValue, MyNode child) {
        children.put(attributeValue, child);
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public void print(String tab, String attributeValue) {
        System.out.println(tab + attributeValue + value);
        for (Map.Entry<String, MyNode> child : children.entrySet()) {
            child.getValue().print(tab + "\t", child.getKey() + " - ");
        }
    }

    public static void main(String[] args) {
        MyNode node = new MyNode("Outlook");
        node.addChild("Sunny", new MyNode("Humidity"));
        node.addChild("Overcast", new MyNode("Yes"));
        node.addChild("Rain", new MyNode("Wind"));
        MyNode child = node.getChild("Sunny");
        child.addChild("High", new MyNode("No"));
        child.addChild("Normal", new MyNode("Yes"));
        child = node.getChild("Rain");
        child.addChild("Strong", new MyNode("No"));
        child.addChild("Weak", new MyNode("Yes"));
        node.print("", "");
    }
}
