package com.company;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Random;

import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;
import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;

public class Main {

    public static void main(String[] args) throws Exception {
        ArffReader arff;
        Instances data;
        Resample filter;
        J48 tree;
        Evaluation eval;
        try (BufferedReader br = new BufferedReader(
                new FileReader("data/weather.nominal.arff"))) {

            arff = new ArffReader(br);
        }
        data = arff.getData();
        data.setClassIndex(data.numAttributes() - 1);

        filter = new Resample();

        filter.setInputFormat(data);
        filter.setNoReplacement(false);
        filter.setSampleSizePercent(100);

        data = Filter.useFilter(data, filter);

        tree = new J48();
        //Id3 tree = new Id3();
        //SimpleCart tree = new SimpleCart();
        tree.buildClassifier(data);
        System.out.println(tree.toString());

        eval = new Evaluation(data);

        eval.crossValidateModel(tree, data, 10, new Random());
        System.out.println(eval.toSummaryString("\n\n\n\n10-Fold\n======\n", false));
    }
}
