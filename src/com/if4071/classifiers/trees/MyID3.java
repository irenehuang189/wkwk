package com.if4071.classifiers.trees;

import org.w3c.dom.Attr;
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
    private ArrayList<MyNode> tree;
    private Map<String, Map<String, Map<String, Integer>>> occurrences;
    private Instances data;

    public MyID3() {
        tree = new ArrayList<>();
    }

    public void setData(Instances data) {
        this.data = data;
    }

    public void buildClassifier() {
        occurrences = countAttributeValuesOccurrence(data);
        String attributeRoot = getAttributeRoot();
        MyNode node = new MyNode(attributeRoot);

    }

    private void makeTree() {
        if(data.numInstances() != 0) {

        }
    }

//    private Instances getSplittedData(Instances unsplitedData) {
//        for(int i=0; i<unsplitedData.numAttributes(); i++) {
//
//        }
//    }

    private String getAttributeRoot() {
        String rootAttribute = "";
        double maxInfoGain = 0;
        for(int i=0; i<data.numAttributes()-1; i++) {
            String attribute = data.attribute(i).name();
            double infoGain = countEntropyTotal() - countRemainder(attribute);
            if(infoGain > maxInfoGain) {
                maxInfoGain = infoGain;
                rootAttribute = attribute;
            }
        }
        return rootAttribute;
    }

    private double countEntropyTotal() {
        Map<String, Integer> classOccurrences = getClassOccurrences();
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

    private double countRemainder(String attribute) {
        double remainder = 0;
        for(int i=0; i<data.attribute(attribute).numValues(); i++) {
            String attributeValue = data.attribute(attribute).value(i);
            double subRemainder = countAttributeOccurrences(attribute, attributeValue) / (double)data.numInstances() * countEntropy(attribute, attributeValue);
            remainder += subRemainder;
        }
        return remainder;
    }

    private int countAttributeOccurrences(String attribute, String instance) {
        int occurrencesSum = 0;
        Map<String, Integer> classMap = occurrences.get(attribute).get(instance);
        for (int value : classMap.values()) {
            occurrencesSum += value;
        }
        return occurrencesSum;
    }

    private double countEntropy(String attribute, String attributeValue) {
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

    private Map<String, Integer> getClassOccurrences() {
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

        // Delete values with no occurrences on data
        Iterator entries = classOccurrences.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry occurrences = (Map.Entry) entries.next();
            Object className = occurrences.getKey();
            Object occurrence = occurrences.getValue();
            if(occurrence.equals(0)) {
                classOccurrences.remove(className.toString());
            }
        }
        return classOccurrences;
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

    public static void main(String[] args) {
        String fileName = "data/mahasiswa.arff";
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            ArffLoader.ArffReader arffReader = new ArffLoader.ArffReader(br);
            Instances data = arffReader.getData();
            data.setClassIndex(data.numAttributes() - 1);

            MyID3 myID3 = new MyID3();
            myID3.setData(data);
            myID3.buildClassifier();

            // Print class occurrences
//            Map<String, Integer> tes = myID3.getClassOccurrences();
//            Iterator entries = tes.entrySet().iterator();
//            while (entries.hasNext()) {
//                Map.Entry thisEntry = (Map.Entry) entries.next();
//                Object key = thisEntry.getKey();
//                Object value = thisEntry.getValue();
//                System.out.println(key + " " + value);
//            }
//            System.out.println(myID3.countEntropy("deadline", "urgent"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
