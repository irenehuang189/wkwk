package com.if4071.classifiers.trees;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Discretize;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by angelynz95 on 05-Oct-16.
 */
public class MyC45 extends Classifier {
    private MyNode root;

    public MyC45() {

    }

    public void buildClassifier(Instances data) throws Exception {
        data = discretizeData(data);
    }

    private Instances discretizeData(Instances data) throws Exception {
        Instances discreteData;
        Discretize filter = new Discretize();

        filter.setInputFormat(data);
        filter.setBins(2);
        discreteData = Filter.useFilter(data, filter);

        return discreteData;
    }

    private MyNode makeTree (Instances data, int i){
        MyNode node;
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
        if (i!=0){
            node = new MyNode(Integer.toString(i));
            node.addChild("1", makeTree(data, i-1));
            node.addChild("2", makeTree(data, 0));
        }
        else {
            node = new MyNode("leaf");
        }
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

    public void test(Instances data) throws Exception {
        data = discretizeData(data);

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
            myC45.test(data);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
