package com.if4071.classifiers.trees;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by jessica on 10/06/2016.
 */
public class MyEvaluation {
    private ArrayList<String> predictedClass = new ArrayList<>();
    private ArrayList<String> actualClass = new ArrayList<>();
    private int correctInstances = 0;
    private int incorrectInstances = 0;
    private int totalInstances = 0;

    public void crossValidation(Classifier tree, Instances data, int treeOpt) throws Exception {
        correctInstances = 0;
        //divide into 10
        ArrayList<Integer> startIndex = new ArrayList<>();
        setTotalInstances(data.numInstances());
        int div = getTotalInstances()/10;
        int mod = getTotalInstances()%10;
        int start = 0;
        for(int i=0; i < 10; i++) {
            startIndex.add(start);
            if (i < mod){
                start += div +1;
            }
            else
                start += div;
        }
        startIndex.add(start);
        //System.out.println(startIndex.toString());
        Instances trainingSet;
        Instances testSet;

        for (int i=0; i < 10; i++){
            testSet = new Instances(data,startIndex.get(i),startIndex.get(i+1)-startIndex.get(i));
            trainingSet = new Instances(data);

            for (int j=startIndex.get(i); j < startIndex.get(i+1); j++){
                trainingSet.delete(j);
            }

            MyID3 newtree = new MyID3();
            newtree.buildClassifier(trainingSet);

            getActualClass().clear();
            getPredictedClass().clear();
            evaluate(newtree,testSet,treeOpt);
            //showResult();
        }

        showResult();
    }

    public void evaluateModel(Classifier tree, Instances data, int treeOpt) throws Exception {
        setTotalInstances(data.numInstances());
        correctInstances = 0;
        evaluate(tree, data,treeOpt);
        showResult();
    }

    private void evaluate(Classifier tree, Instances data, int treeOpt) throws Exception {
        // if treeOpt = 3 then ID3, if 4  then C45

        inputActualClass(data);
        if (treeOpt == 4) {
            for (int i = 0; i < data.numInstances(); i++) {
                predictedClass.add(predictClass(data.instance(i), (MyC45) tree, data));
            }
        }
        if (treeOpt == 3) {
            for (int i = 0; i < data.numInstances(); i++) {
                predictedClass.add(predictClass(data.instance(i), (MyID3) tree, data));
            }
        }
        countAccuracy();
    }

    private String predictClass(Instance instance, MyC45 tree, Instances data) throws Exception {
        MyNode node = tree.getRoot();
        String value;
        ReplaceMissingValues replace = new ReplaceMissingValues();
        replace.setInputFormat(data);
        data = Filter.useFilter(data, replace);

        while (!node.isLeaf()){
            value = instance.stringValue((data.attribute(node.getLabel())));
            node = node.getChild(value);
        }
        return node.getLabel();
    }

    private String predictClass(Instance instance, MyID3 tree, Instances data){
        MyNode node = tree.getTree();
        String value;
        while (!node.isLeaf()){
            value = instance.stringValue((data.attribute(node.getLabel())));
            node = node.getChild(value);
            //if value == ?
        }
        return node.getLabel();
    }

    private void countAccuracy(){
        for (int i=0; i<actualClass.size(); i++){
            if (actualClass.get(i).equals(predictedClass.get(i))){
                correctInstances++;
            }
            else {
                incorrectInstances++;
            }
        }
    }

    public void showResult(){
        System.out.println("Correctly Classified Instances \t\t\t" + getCorrectInstances() + "\t" + String.format("%.2f", (((double)getCorrectInstances() / (double)getTotalInstances()) *(double)100)) + "%");
        System.out.println("Incorrectly Classified Instances \t\t" + getIncorrectInstances() + "\t" + String.format("%.2f", (((double)getIncorrectInstances() / (double)getTotalInstances()) *(double)100)) + "%");
        System.out.println("Total Number of Instances \t\t\t\t" + getTotalInstances());

    }
    private void inputActualClass (Instances data){
        for (int i=0; i < data.numInstances(); i++){
            actualClass.add(data.instance(i).stringValue(data.classIndex()));
        }
    }



    public ArrayList<String> getPredictedClass() {
        return predictedClass;
    }

    public void setPredictedClass(ArrayList<String> predictedClass) {
        this.predictedClass = predictedClass;
    }

    public ArrayList<String> getActualClass() {
        return actualClass;
    }

    public void setActualClass(ArrayList<String> actualClass) {
        this.actualClass = actualClass;
    }

    public static void main(String[] args) throws Exception {
        String fileName = "data/weather.nominal.arff";
        Instances data;
        try (BufferedReader br = new BufferedReader(
                new FileReader(fileName))) {

            ArffLoader.ArffReader arff = new ArffLoader.ArffReader(br);

            data = arff.getData();
            data.setClassIndex(data.numAttributes() - 1);
            System.out.println("\nDataset:\n");
            //System.out.println(data);

            MyEvaluation eval = new MyEvaluation();
            //eval.inputActualClass(data);
            //System.out.println(Arrays.toString(eval.getActualClass().toArray()));

            MyC45 tree = new MyC45();
            MyNode node = new MyNode("outlook", 0);
            node.addChild("sunny", new MyNode("humidity", 1, node));
            node.addChild("overcast", new MyNode("yes", 1, node));
            node.addChild("rainy", new MyNode("windy", 1, node));
            MyNode child = node.getChild("sunny");
            child.addChild("high", new MyNode("no", 2, child));
            child.addChild("normal", new MyNode("yes", 2, child));
            child = node.getChild("rainy");
            child.addChild("TRUE", new MyNode("no", 2, child));
            child.addChild("FALSE", new MyNode("yes", 2, child));
            //node.print("", "");

            tree.setRoot(node);
            eval.crossValidation(tree, data, 4);
            //eval.evaluateModel(tree, data, 4);
            //System.out.println(Arrays.toString(eval.getActualClass().toArray()));
            //System.out.println(Arrays.toString(eval.getPredictedClass().toArray()));




        }
    }

    public int getCorrectInstances() {
        return correctInstances;
    }

    public void setCorrectInstances(int correctInstances) {
        this.correctInstances = correctInstances;
    }

    public int getIncorrectInstances() {
        return incorrectInstances;
    }

    public void setIncorrectInstances(int incorrectInstances) {
        this.incorrectInstances = incorrectInstances;
    }

    public int getTotalInstances() {
        return totalInstances;
    }

    public void setTotalInstances(int totalInstances) {
        this.totalInstances = totalInstances;
    }
}
