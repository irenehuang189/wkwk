package com.if4071.clusterers;

import com.if4071.classifiers.trees.MyNode;
import weka.core.DistanceFunction;
import weka.core.EuclideanDistance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by irn on 23/11/2016.
 */
public class MyAgnes {
    private Instances data;
    private int clusterNum;
    private int linkType;

    private DistanceFunction distanceFunction;
    private double[][] initDistances;
    private ArrayList<ArrayList<Integer>> indices;
    private MyAgnesNode tree;

    final int SINGLE = 0;
    final int COMPLETE = 1;

    public MyAgnes(Instances data, int clusterNum, int linkType) {
        this.data = data;
        this.clusterNum = clusterNum;
        this.linkType = linkType;

        distanceFunction = new EuclideanDistance();
        distanceFunction.setInstances(data);

        int instancesNum = data.numInstances();
        initDistances = new double[instancesNum][instancesNum];

        indices = new ArrayList<>();
        for(int i=0; i<instancesNum; i++) {
            ArrayList index = new ArrayList();
            index.add(i);
            indices.add(index);
        }
    }

    public void setData(Instances data) {
        this.data = data;
    }

    public void buildClusterer() {
        initDistances();
        printDistances();

        MyPairDistance nearestPairDistance = getNearestInitDistances();
        System.out.println(nearestPairDistance.toString());
        tree = new MyAgnesNode(String.valueOf(nearestPairDistance.getDistance()));
    }

    public MyPairDistance getNearestInitDistances() {
        double minDistance = 99999999;
        ArrayList<Integer> nearestPair = new ArrayList<>();
        for(int i=0; i<initDistances.length; i++) {
            for(int j=i+1; j<initDistances[i].length; j++) {
                if(minDistance > initDistances[i][j]) {
                    minDistance = initDistances[i][j];
                    nearestPair.clear();
                    nearestPair.add(i);
                    nearestPair.add(j);
                }
            }
        }

        return new MyPairDistance(nearestPair, minDistance);
    }

    public void initDistances() {
        int numInstances = data.numInstances();
        for(int i=0; i<numInstances; i++) {
            for(int j=0; j<numInstances; j++) {
                if(i == j) {
                    initDistances[i][j] = 0;
                } else {
                    initDistances[i][j] = distanceFunction.distance(data.instance(i), data.instance(j));
                }
            }
        }
    }

    public void printDistances() {
        int numInstances = data.numInstances();
        for(int i=0; i<initDistances.length; i++) {
            for(int j=0; j<initDistances.length; j++) {
                System.out.print(initDistances[i][j] + " ");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        String fileName = "data/weather.numeric.arff";
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            ArffLoader.ArffReader arffReader = new ArffLoader.ArffReader(br);
            Instances data = arffReader.getData();
            data.setClassIndex(data.numAttributes() - 1);

            MyAgnes myAgnes = new MyAgnes(data, 2, 0);
            myAgnes.buildClusterer();


//            MyEvaluation evaluation = new MyEvaluation();

//            System.out.println("Result\n-------");
//            evaluation.evaluateModel(myAgnes,data,3);
//            evaluation.showResult();
//
//
//            System.out.println("\n\n10-Fold\n-------");
//            evaluation.crossValidation(myAgnes,data,3);
//            evaluation.showResult();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
