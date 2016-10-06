package com.if4071.classifiers.trees;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

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
    public MyC45() {

    }

    public void buildClassifier(Instances data) {

    }

    private Map<Attribute, Map<String, Map<String, Integer>>> countAttributeValuesOccurence(Instances data, Attribute classAttribute) {
        Map<Attribute, Map<String, Map<String, Integer>>> attributeValuesOccurence = new HashMap<>();
        Enumeration attributes = data.enumerateAttributes();
        Enumeration attributeValues;
        Enumeration instances = data.enumerateInstances();
        Attribute attribute;
        String attributeValue;
        Instance instance;
        int count;

        while (attributes.hasMoreElements()) {
            attribute = (Attribute) attributes.nextElement();
            attributeValuesOccurence.put(attribute, new HashMap<>());
            attributeValues = attribute.enumerateValues();
            while (attributeValues.hasMoreElements()) {
                attributeValue = attributeValues.nextElement().toString();
                attributeValuesOccurence.get(attribute).put(attributeValue, new HashMap<>());
                for (int i = 0; i < classAttribute.numValues(); i++) {
                    attributeValuesOccurence.get(attribute).get(attributeValue).put(classAttribute.value(i), 0);
                }
            }
        }

        while (instances.hasMoreElements()) {
            instance = (Instance) instances.nextElement();
            for (int i = 0; i < instance.numAttributes(); i++) {
                attribute = instance.attribute(i);
                count = attributeValuesOccurence.get(attribute).get(instance.stringValue(attribute)).get(instance.stringValue(classAttribute));
                count++;
                attributeValuesOccurence.get(attribute).get(instance.stringValue(attribute)).put(instance.stringValue(classAttribute), count);
            }
        }

        return attributeValuesOccurence;
    }

    public void testCountAttributeValuesOccurence(Instances data) {
        Attribute attribute;
        Attribute classAttribute = data.attribute(data.numAttributes()-1);
        Map<Attribute, Map<String, Map<String, Integer>>> attributeValuesOccurence = countAttributeValuesOccurence(data, classAttribute);
        Enumeration attributes = data.enumerateAttributes();

        while (attributes.hasMoreElements()) {
            attribute = (Attribute) attributes.nextElement();
            System.out.println(attribute.name());
            for (int i = 0; i < classAttribute.numValues(); i++) {
                System.out.println(classAttribute.name() + " " + classAttribute.value(i));
                for (int j = 0; j < attribute.numValues(); j++) {
                    System.out.println("\t" + attribute.value(j) + " " + attributeValuesOccurence.get(attribute).get(attribute.value(j)).get(classAttribute.value(i)));
                }
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        String fileName = "data/weather.nominal.arff";
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            ArffReader arffReader = new ArffReader(br);

            MyC45 myC45 = new MyC45();
            myC45.testCountAttributeValuesOccurence(arffReader.getData());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
