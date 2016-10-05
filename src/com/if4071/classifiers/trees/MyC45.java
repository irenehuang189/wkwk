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

    private Map<String, Map<String, Map<String, Integer>>> countAttributeValuesOccurence(Instances data, Attribute classAttribute) {
//        lagi dikerjain angela
//        misal dataset weather.nominal, class attribute play
//        get("humidity").get("high").get("yes") untuk dapetin banyaknya humidity high dengan play bernilai yes pada dataset
        return new HashMap<>();
    }

    public static void main(String[] args) {
        String fileName = "data/weather.nominal.arff";
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            ArffReader arffReader = new ArffReader(br);

            MyC45 myC45 = new MyC45();
            myC45.buildClassifier(arffReader.getData());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
