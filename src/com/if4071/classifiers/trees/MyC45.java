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
public class MyC45 extends Classifier {
    private MyNode root;
    private ArrayList<MyNode> prunableLeaf = new ArrayList<>();

    public MyC45() {

    }

    public void buildClassifier(Instances data) throws Exception {
        data = discretizeData(data);
        data = replaceMissingValues(data);
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

    private float countPreSplitError(int numWrongInstances, int numInstances) {
        return (numWrongInstances + 0.5f) / numInstances;
    }

    private float countPostSplitError(int numLeaves, int numWrongInstances, int numInstances) {
        return (numWrongInstances + 0.5f * numLeaves) / numInstances;
    }

    public void test(Instances data) throws Exception {
        data = discretizeData(data);
        data = replaceMissingValues(data);

        Attribute attribute;
        Attribute classAttribute = data.attribute(data.numAttributes()-1);
        Map<String, Map<String, Map<String, Integer>>> attributeValuesOccurrence = countAttributeValuesOccurrence(data);
        Enumeration attributes = data.enumerateAttributes();

        while (attributes.hasMoreElements()) {
            attribute = (Attribute) attributes.nextElement();
            System.out.println(attribute.name());
            for (int i = 0; i < classAttribute.numValues(); i++) {
                System.out.println(classAttribute.name() + " " + classAttribute.value(i));
                for (int j = 0; j < attribute.numValues(); j++) {
                    System.out.println("\t" + attribute.value(j) + " " + attributeValuesOccurrence.get(attribute.name()).get(attribute.value(j)).get(classAttribute.value(i)));
                }
            }
            System.out.println();
        }
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
        String fileName = "data/weather.numeric.arff";
        Instances data;
        try (BufferedReader br = new BufferedReader(
                new FileReader(fileName))) {
            ArffReader arff = new ArffReader(br);
            data = arff.getData();
            data.setClassIndex(data.numAttributes() - 1);

            MyC45 myC45 = new MyC45();
            //myC45.test(data);
            myC45.buildClassifier(data);
            System.out.println(Arrays.toString(myC45.getPrunableLeaf().toArray()));
            myC45.getRoot().print("","");
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
