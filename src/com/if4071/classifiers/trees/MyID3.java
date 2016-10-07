package com.if4071.classifiers.trees;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by irn on 05/10/2016.
 */
public class MyID3 {
    protected MyNode tree;
    private Map<String, Map<String, Map<String, Integer>>> occurrences;
    protected Instances data;
    protected Map<MyNode, Instances> nodeData;

    public MyID3() {
        tree = new MyNode("", 0);
        nodeData = new HashMap<>();
    }

    public void setData(Instances data) {
        this.data = data;
    }


    public void buildClassifier(Instances data) throws Exception {
        setData(data);
        makeTree(null, "", data);
        System.out.println("\nStruktur pohon: ");
        tree.print("", "");
    }

    private void makeTree(MyNode parent, String label, Instances data) {
        occurrences = countAttributeValuesOccurrence(data);

//        int levelTest = 0;
//        if (parent != null) {
//            levelTest = parent.getLevel();
//        }
        if(data.numInstances() != 0) {
            // Get root attribute
            String rootAttribute = "";
            double maxInfoGain = -999;
            for(int i=0; i<data.numAttributes()-1; i++) {
                String attribute = data.attribute(i).name();
//                System.out.println(attribute);
//                System.out.println("Entropy total: " + countEntropyTotal(data));
//                System.out.println("Remainder: " + countRemainder(data, attribute));
                double infoGain = countEntropyTotal(data) - countRemainder(data, attribute);
                if(infoGain > maxInfoGain) {
                    maxInfoGain = infoGain;
                    rootAttribute = attribute;
                }
            }

            int level = 0;
            if (parent != null) {
                level = parent.getLevel() + 1;
            }
            System.out.println("Info gain: " + maxInfoGain);
            if(maxInfoGain == 0) {
                // Create leaf
//                int i=0;
                // Get every instances class value
//                while(i < data.numInstances()) {
//                    System.out.println(data.instance(i).stringValue(data.classIndex()));
//                    i++;
//                }
                System.out.println("Leaf: " + data.instance(0).stringValue(data.classIndex()) + ", level: " + level);
                MyNode leaf = new MyNode(data.instance(0).stringValue(data.classIndex()), level, parent);
                if(parent != null) {
                    System.out.println("Label to parent: " + label);
                    parent.addChild(label, leaf);
                }
            } else {
                System.out.println("Node: " + rootAttribute + " level: " + level);
                MyNode node = new MyNode(rootAttribute, level, parent);
                if(parent != null) {
                    System.out.println("Label to parent: " + label);
                    parent.addChild(label, node);
                } else {
                    tree = node;
                }

                Map<String, Instances> splitData = getSplitData(data, rootAttribute);
                for(Map.Entry<String, Instances> subTree: splitData.entrySet()) {
                    System.out.println("Make tree " + subTree.getKey());
                    System.out.println();
                    makeTree(node, subTree.getKey(), subTree.getValue());
                    nodeData.put(node.getChild(subTree.getKey()), subTree.getValue());
                }
            }
        }
    }

    private Map<String, Map<String, Map<String, Integer>>> countAttributeValuesOccurrence(Instances data) {
        Attribute classAttribute = data.classAttribute();
        Map<String, Map<String, Map<String, Integer>>> attributeValuesOccurrence = new HashMap<>();
        Enumeration attributes = data.enumerateAttributes();
        Enumeration attributeValues;
        Enumeration instances = data.enumerateInstances();
        Attribute attribute;
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

    public void testCountAttributeValuesOccurrence(Instances data) {
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

    protected Map<String, Instances> getSplitData(Instances unsplitData, String attributeName) {
        Map<String, Instances> splitData = new HashMap<>();
        Attribute attribute = unsplitData.attribute(attributeName);

        // Initialize array of split data
        for(int i=0; i<attribute.numValues(); i++) {
            splitData.put(attribute.value(i), new Instances(unsplitData, unsplitData.numInstances()));
        }

        // Classify instances to its attribute value group
        for(int i=0; i<unsplitData.numInstances(); i++) {
            Instance instance = unsplitData.instance(i);
            String attributeValue = instance.stringValue(attribute);
            splitData.get(attributeValue).add(instance);
        }

        // Delete attribute value group with no instances
        ArrayList<String> deletedKeys = new ArrayList<>();
        for(Map.Entry<String, Instances> subData: splitData.entrySet()) {
            if (subData.getValue().numInstances() == 0) {
                deletedKeys.add(subData.getKey());
            }
        }
        for (String key: deletedKeys) {
            splitData.remove(key);
        }
        return splitData;
    }

    private double countEntropyTotal(Instances data) {
        Map<String, Integer> classOccurrences = getClassOccurrences(data);
        int occurrencesSum = data.numInstances();
        double entropy = 0;

        Iterator entries = classOccurrences.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry occurrences = (Map.Entry) entries.next();
            Object occurrence = occurrences.getValue();
            double proportion = Double.parseDouble(String.valueOf(occurrence)) / (double)occurrencesSum;
            double subEntropy = -proportion * Math.log(proportion) / Math.log(2);
            entropy += subEntropy;
        }
        return entropy;
    }

    private Map<String, Integer> getClassOccurrences(Instances data) {
        Map<String, Integer> classOccurrences = new HashMap<>();
        // Initialize map with class values
        for(int i=0; i<data.numClasses(); i++) {
            classOccurrences.put(data.classAttribute().value(i), 0);
        }

        // Count data occurrences
        for(int i=0; i<data.numInstances(); i++) {
            Instance instance = data.instance(i);
            String className = data.classAttribute().value((int) instance.classValue());
            classOccurrences.put(className, classOccurrences.get(className)+1);
        }

        // Delete values with no instance occurrences on data
        ArrayList<String> deletedKeys = new ArrayList<>();
        for(Map.Entry<String, Integer> occurrence: classOccurrences.entrySet()) {
            if (occurrence.getValue().equals(0)) {
                String className = occurrence.getKey();
                deletedKeys.add(className);
            }
        }
        for (String key: deletedKeys) {
            classOccurrences.remove(key);
        }
        return classOccurrences;
    }

    private double countRemainder(Instances data, String attribute) {
        double remainder = 0;
        for(int i=0; i<data.attribute(attribute).numValues(); i++) {
            String attributeValue = data.attribute(attribute).value(i);
//            System.out.println("Sub remainder: " + countAttributeOccurrences(data, attribute, attributeValue)+ " / " + data.numInstances() + " * " + countEntropy(data, attribute, attributeValue));
            double subRemainder = countAttributeOccurrences(data, attribute, attributeValue) / (double)data.numInstances() * countEntropy(data, attribute, attributeValue);
            remainder += subRemainder;
        }
        return remainder;
    }

    private int countAttributeOccurrences(Instances data, String attribute, String instance) {
        int occurrencesSum = 0;
        Map<String, Integer> classMap = occurrences.get(attribute).get(instance);
        for (int value : classMap.values()) {
            occurrencesSum += value;
        }
        return occurrencesSum;
    }

    private double countEntropy(Instances data, String attribute, String attributeValue) {
        Attribute classAttribute = data.classAttribute();

        int occurrencesSum = 0;
        ArrayList<Integer> classValueOccurrences = new ArrayList<>();
        for (int i=0; i<classAttribute.numValues(); i++) {
            int occurrence = occurrences.get(attribute).get(attributeValue).get(classAttribute.value(i));
            if (occurrence != 0) {
                classValueOccurrences.add(occurrence);
            }
            occurrencesSum += occurrence;
        }

        double entropy = 0;
        for (Integer classValueOccurrence : classValueOccurrences) {
            double proportion = (double) classValueOccurrence / (double) occurrencesSum;
            double subEntropy = -proportion * Math.log(proportion) / Math.log(2);
            entropy += subEntropy;
        }
        return entropy;
    }

    public static void main(String[] args) {
        String fileName = "data/weather.nominal.arff";
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            ArffLoader.ArffReader arffReader = new ArffLoader.ArffReader(br);
            Instances data = arffReader.getData();
            data.setClassIndex(data.numAttributes() - 1);

            MyID3 myID3 = new MyID3();
            myID3.buildClassifier(data);
            MyEvaluation evaluation = new MyEvaluation();

            System.out.println("Result\n-------");
            evaluation.evaluateModel(myID3,data,3);


            System.out.println("\n\n10-Fold\n-------");
            evaluation.crossValidation(myID3,data,3);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MyNode getTree() {
        return tree;
    }

    public void setTree(MyNode tree) {
        this.tree = tree;
    }
}
