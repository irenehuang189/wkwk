package com.if4071.classifiers.trees;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Discretize;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by angelynz95 on 05-Oct-16.
 */
public class MyC45 extends MyID3 {
    private MyNode root;
    private ArrayList<MyNode> prunableLeaf = new ArrayList<>();

    public MyC45() {

    }

    public void buildClassifier() throws Exception {
        data = discretizeData(data);
        data = replaceMissingValues(data);
        super.buildClassifier();
        prunableLeaf.add(tree);
        System.out.println("Prunable Leaf " + prunableLeaf.size());
//        MyNode node = tree;
//        node = node.getChild("rainy");
//        System.out.println("Prune" + isPruned(node));
    }

    private Instances discretizeData(Instances data) throws Exception {
        Instances discreteData;
        Discretize filter = new Discretize();

        filter.setInputFormat(data);
        filter.setBins(2);
        discreteData = Filter.useFilter(data, filter);

        return discreteData;
    }

    private Instances replaceMissingValues(Instances data) throws Exception {
        Instances replacedData;
        ReplaceMissingValues filter = new ReplaceMissingValues();

        filter.setInputFormat(data);
        replacedData = Filter.useFilter(data, filter);

        return replacedData;
    }

    private MyNode makeTree (Instances data, int i){
        MyNode node = new MyNode("root", 0);
        /*
        kalau data kosong
        if (data.numInstances() == 0){
            node = new MyNode ("null");
            return node;
        }
        /*
        double[] infoGains = new double[data.numAttributes()];
        Enumeration attEnum = data.enumerateAttributes();
        while (attEnum.hasMoreElements())
        {
            Attribute att = (Attribute)attEnum.nextElement();
            infoGains[att.index()] = computeInfoGain(data, att);
        }
             */

        //i = angka asal cuma buat test
//        if (i!=0){
//            node = new MyNode(Integer.toString(i));
//            node.addChild("1", makeTree(data, i-1));
//            node.addChild("2", makeTree(data, 0));
//        }
//        else {
//            node = new MyNode("leaf");
//        }
        return node;
    }

    private double computeInfoGain(Instances data, Attribute att){
        return 0;
    }

    private Map<String, Map<String, Map<String, Integer>>> countAttributeValuesOccurrence(Instances data) {
        Map<String, Map<String, Map<String, Integer>>> attributeValuesOccurrence = new HashMap<>();
        Enumeration attributes = data.enumerateAttributes();
        Enumeration attributeValues;
        Enumeration instances = data.enumerateInstances();
        Attribute attribute;
        Attribute classAttribute = data.classAttribute();
        String attributeValue;
        Instance instance;
        int count;

        while (attributes.hasMoreElements()) {
            attribute = (Attribute) attributes.nextElement();
            attributeValuesOccurrence.put(attribute.name(), new HashMap<>());
            attributeValues = attribute.enumerateValues();
            while (attributeValues.hasMoreElements()) {
                attributeValue = attributeValues.nextElement().toString();
                attributeValuesOccurrence.get(attribute.name()).put(attributeValue, new HashMap<>());
                for (int i = 0; i < classAttribute.numValues(); i++) {
                    attributeValuesOccurrence.get(attribute.name()).get(attributeValue).put(classAttribute.value(i), 0);
                }
            }
        }

        while (instances.hasMoreElements()) {
            instance = (Instance) instances.nextElement();
            for (int i = 0; i < instance.numAttributes()-1; i++) {
                attribute = instance.attribute(i);
                count = attributeValuesOccurrence.get(attribute.name()).get(instance.stringValue(attribute)).get(instance.stringValue(classAttribute));
                count++;
                attributeValuesOccurrence.get(attribute.name()).get(instance.stringValue(attribute)).put(instance.stringValue(classAttribute), count);
            }
        }

        return attributeValuesOccurrence;
    }

    private boolean isPruned(MyNode parentNode) {
        float preSplitError, postSplitError;
        int numIncorrectInstances, numInstances, numLeaves;
        MyEvaluation myEvaluation;
        MyNode tempParentNode;
        String prunedLabel = getMostOccurClassValue(data);

        data = nodeData.get(parentNode);

        tempParentNode = new MyNode(prunedLabel, 0);
        this.root = tempParentNode;
        myEvaluation = new MyEvaluation();
        myEvaluation.evaluateModel(this, data);
        numIncorrectInstances = myEvaluation.getIncorrectInstances();
        System.out.println("Num Incorrect Instances " + numIncorrectInstances);
        numInstances = myEvaluation.getTotalInstances();
        System.out.println("Num Instances " + numInstances);
        preSplitError = countPreSplitError(numIncorrectInstances, numInstances);

        this.root = parentNode;
        myEvaluation = new MyEvaluation();
        myEvaluation.evaluateModel(this, data);
        numIncorrectInstances = myEvaluation.getIncorrectInstances();
        System.out.println("Num Incorrect Instances " + numIncorrectInstances);
        numInstances = myEvaluation.getTotalInstances();
        System.out.println("Num Instances " + numInstances);
        numLeaves = this.root.numChildren();
        postSplitError = countPostSplitError(numLeaves, numIncorrectInstances, numInstances);

        System.out.println("Pre " + preSplitError);
        System.out.println("Post " + postSplitError);
        if (preSplitError <= postSplitError) {
            this.root.removeAllChildren();
            this.root.setLabel(prunedLabel);
            return true;
        }

        return false;
    }

    private String getMostOccurClassValue(Instances data) {
        Attribute classAttribute = data.classAttribute();
        Enumeration instances = data.enumerateInstances();
        Instance instance;
        int count, max;
        Map<String, Integer> classValuesOccurrence = new HashMap<>();
        String classValue, mostOccurClassValue = "";

        for (int i = 0; i < classAttribute.numValues(); i++) {
            classValuesOccurrence.put(classAttribute.value(i), 0);
        }

        while (instances.hasMoreElements()) {
            instance = (Instance) instances.nextElement();
            classValue = instance.stringValue(classAttribute);
            count = classValuesOccurrence.get(classValue);
            count++;
            classValuesOccurrence.put(classValue, count);
        }

        max = -1;
        for (int i = 0; i < classAttribute.numValues(); i++) {
            classValue = classAttribute.value(i);
            count = classValuesOccurrence.get(classValue);
            if (count > max) {
                max = count;
                mostOccurClassValue = classValue;
            }
        }

        return mostOccurClassValue;
    }

    private float countPreSplitError(int numIncorrectInstances, int numInstances) {
        return (numIncorrectInstances + 0.5f) / numInstances;
    }

    private float countPostSplitError(int numLeaves, int numIncorrectInstances, int numInstances) {
        return (numIncorrectInstances + 0.5f * numLeaves) / numInstances;
    }

    public void test(Instances data) throws Exception {
        data = discretizeData(data);
        data = replaceMissingValues(data);

        Map<MyNode, Instances> nodeData = new HashMap<>();
        Map<String, Instances> splitData;

        MyNode node = new MyNode("outlook", 0);
        splitData = this.getSplitData(data, node.getLabel());
        node.addChild("sunny", new MyNode("humidity", 1, node));
        nodeData.put(node.getChild("sunny"), splitData.get("sunny"));
        node.addChild("overcast", new MyNode("yes", 1, node));
        nodeData.put(node.getChild("overcast"), splitData.get("overcast"));
        node.addChild("rainy", new MyNode("windy", 1, node));
        nodeData.put(node.getChild("rainy"), splitData.get("rainy"));
        MyNode child = node.getChild("sunny");
        splitData = this.getSplitData(nodeData.get(child), child.getLabel());
        child.addChild("high", new MyNode("no", 2, child));
        nodeData.put(child.getChild("high"), splitData.get("high"));
        child.addChild("normal", new MyNode("yes", 2, child));
        nodeData.put(child.getChild("normal"), splitData.get("normal"));
        child = node.getChild("rainy");
        splitData = this.getSplitData(nodeData.get(child), child.getLabel());
        child.addChild("TRUE", new MyNode("no", 2, child));
        nodeData.put(node.getChild("TRUE"), splitData.get("TRUE"));
        child.addChild("FALSE", new MyNode("yes", 2, child));
        nodeData.put(node.getChild("FALSE"), splitData.get("FALSE"));

//        System.out.println(isPruned(child, nodeData.get(child)));

        node.print("", "");

//        Attribute attribute;
//        Attribute classAttribute = data.attribute(data.numAttributes()-1);
//        Map<String, Map<String, Map<String, Integer>>> attributeValuesOccurrence = countAttributeValuesOccurrence(data);
//        Enumeration attributes = data.enumerateAttributes();
//
//        while (attributes.hasMoreElements()) {
//            attribute = (Attribute) attributes.nextElement();
//            System.out.println(attribute.name());
//            for (int i = 0; i < classAttribute.numValues(); i++) {
//                System.out.println(classAttribute.name() + " " + classAttribute.value(i));
//                for (int j = 0; j < attribute.numValues(); j++) {
//                    System.out.println("\t" + attribute.value(j) + " " + attributeValuesOccurrence.get(attribute.name()).get(attribute.value(j)).get(classAttribute.value(i)));
//                }
//            }
//            System.out.println();
//        }
    }

    private void pruneTree(){
        int lowestLevel = getLowestLevel();
        while ((lowestLevel >= 0) && !prunableLeaf.isEmpty()){
            ArrayList<MyNode> lowestLeaf = getLowestLeaf(lowestLevel);
            pruneLowestLeaf(lowestLeaf);
            lowestLevel--;
        }
    }

    private int getLowestLevel(){
        int lowestLevel = getPrunableLeaf().get(0).getLevel();
        for (MyNode leaf: getPrunableLeaf()){
            if (leaf.getLevel() < lowestLevel) {
                lowestLevel = leaf.getLevel();
            }
        }
        return  lowestLevel;
    }

    private ArrayList<MyNode> getLowestLeaf(int level){
        ArrayList<MyNode> lowestLeaf = new ArrayList<>();
        for (MyNode leaf: getPrunableLeaf()) {
            if (leaf.getLevel() == level){
                lowestLeaf.add(leaf);
            }
        }
        return lowestLeaf;
    }

    private void pruneLowestLeaf(ArrayList<MyNode> lowestLeaf){
        for (MyNode leaf: getPrunableLeaf()) {
            //check if all neighbors are leaves, if true check, else delete from prunableleaf
                //check if prune, if true then add parent to prunableleaf and delete self and neighbors from prunableleaf and from tree
                //else if not prune delete self from prunableleaf
            /* if (leaf.neighborAreLeaves){

            }
            else {
                prunableLeaf.remove(leaf);
            }
             */

        }
    }

    private void checkPrunableLeafs(){

    }

    public MyNode getRoot() {
        return root;
    }

    public void setRoot(MyNode root) {
        this.root = root;
    }

    public static void main(String[] args) {
        String fileName = "data/weather.nominal.arff";
        Instances data;
        try (BufferedReader br = new BufferedReader(
                new FileReader(fileName))) {
            ArffReader arff = new ArffReader(br);
            data = arff.getData();
            data.setClassIndex(data.numAttributes() - 1);

            MyC45 myC45 = new MyC45();
            myC45.setData(data);
            myC45.buildClassifier();
//            myC45.buildClassifier(data);
//            System.out.println(Arrays.toString(myC45.getPrunableLeaf().toArray()));
//            myC45.getRoot().print("","");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<MyNode> getPrunableLeaf() {
        return prunableLeaf;
    }

    public void setPrunableLeaf(ArrayList<MyNode> prunableLeaf) {
        this.prunableLeaf = prunableLeaf;
    }
}
