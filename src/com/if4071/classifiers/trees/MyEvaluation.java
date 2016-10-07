package com.if4071.classifiers.trees;

import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by jessica on 10/06/2016.
 */
public class MyEvaluation {
    private ArrayList<String> predictedClass = new ArrayList<>();
    private ArrayList<String> actualClass = new ArrayList<>();
    private int correctInstances;
    private int incorrectInstances;
    private int totalInstances = 0;

    public int getIncorrectInstances() {
        return incorrectInstances;
    }

    public int getTotalInstances() {
        return totalInstances;
    }

    public void evaluateModel(MyC45 tree, Instances data){
        totalInstances = data.numInstances();
        inputActualClass(data);
        for (int i =0; i < totalInstances; i++){
            predictedClass.add(predictClass(data.instance(i), tree, data));
        }
        countAccuracy();
        showResult();
    }

    private String predictClass(Instance instance, MyC45 tree, Instances data){
        MyNode node = tree.getRoot();
        String value;
        while (!node.isLeaf()){
            value = instance.stringValue((data.attribute(node.getLabel())));
            node = node.getChild(value);
            //if value == ?
        }
        return node.getLabel();
    }

    private void countAccuracy(){
        correctInstances = 0;
        incorrectInstances = 0;
        for (int i=0; i<actualClass.size(); i++){
            if (actualClass.get(i).equals(predictedClass.get(i))){
                correctInstances++;
            }
            else {
                incorrectInstances++;
            }
        }
    }

    private void showResult(){
        System.out.println("Correctly Classified Instances \t\t\t" + correctInstances + "\t" + (double)(correctInstances/totalInstances*100) + "%");
        System.out.println("Incorrectly Classified Instances \t\t" + incorrectInstances + "\t" + (double)(incorrectInstances/totalInstances*100) + "%");
        System.out.println("Total Number of Instances \t\t\t\t" + totalInstances);

    }
    private void inputActualClass (Instances data){
        for (int i=0; i < totalInstances; i++){
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

    public static void main(String[] args) throws IOException {
        String fileName = "data/weather.nominal.arff";
        Instances data;
        try (BufferedReader br = new BufferedReader(
                new FileReader(fileName))) {

            ArffLoader.ArffReader arff = new ArffLoader.ArffReader(br);

            data = arff.getData();
            data.setClassIndex(data.numAttributes() - 1);
            System.out.println("\nDataset:\n");
            System.out.println(data);

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

            eval.evaluateModel(tree, data);
            System.out.println(Arrays.toString(eval.getActualClass().toArray()));
            System.out.println(Arrays.toString(eval.getPredictedClass().toArray()));




        }
    }
}
