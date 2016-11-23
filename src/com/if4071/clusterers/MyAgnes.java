package com.if4071.clusterers;

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

        MyPairDistance nearestPairDistance = getNearestInitDistances(initDistances);
        tree = new MyAgnesNode(String.valueOf(nearestPairDistance.getDistance()));
        mergeNearestIndices(nearestPairDistance.getNearestPair());
        System.out.println(nearestPairDistance.getDistance());

        while(indices.size() > clusterNum) {
            buildNewCluster();
        }
        System.out.println(indices);
    }

    public void buildNewCluster() {
        double[][] newDistances = new double[indices.size()][indices.size()];
        for(int i=0; i<indices.size(); i++) {
            for(int j=0; j<indices.size(); j++) {
                newDistances[i][j] = getNearestDistanceBetweenClusters(indices.get(i), indices.get(j));
            }
        }

        MyPairDistance nearestPairDistance = getNearestInitDistances(newDistances);
        mergeNearestIndices(nearestPairDistance.getNearestPair());
        System.out.println(nearestPairDistance.getDistance());
    }

    public void mergeNearestIndices(ArrayList<Integer> nearestIndices) {
        Integer destIndex = nearestIndices.get(0);
        Integer oriIndex = nearestIndices.get(1);
        indices.get(destIndex).addAll(indices.get(oriIndex));
        indices.remove((int) oriIndex);
    }

    public double getNearestDistanceBetweenClusters(ArrayList<Integer> cluster1, ArrayList<Integer> cluster2) {
        double chosenDistance = 0;
        switch (linkType){
            case SINGLE:
                chosenDistance = 999999999;
                break;
            case COMPLETE:
                chosenDistance = -999999999;
                break;
        }

        for(int i=0; i<cluster1.size(); i++) {
            for(int j=0; j<cluster2.size(); j++) {
                Integer index1 = cluster1.get(i);
                Integer index2 = cluster2.get(j);
                double distance = initDistances[index1][index2];
                switch (linkType){
                    case SINGLE:
                        if(chosenDistance > distance) {
                            chosenDistance = distance;
                        }
                        break;
                    case COMPLETE:
                        if(chosenDistance < distance) {
                            chosenDistance = distance;
                        }
                        break;
                }
            }
        }

        return chosenDistance;
    }

    public MyPairDistance getNearestInitDistances(double[][] distances) {
        double minDistance = 999999999;
        ArrayList<Integer> nearestPair = new ArrayList<>();
        for(int i=0; i<distances.length; i++) {
            for(int j=i+1; j<distances[i].length; j++) {
                if(minDistance > distances[i][j]) {
                    minDistance = distances[i][j];
                    nearestPair.clear();
                    nearestPair.add(i);
                    nearestPair.add(j);
                }
            }
        }
        System.out.println(nearestPair.get(0) + " " + nearestPair.get(1));
        System.out.println("Pair: " + data.instance(nearestPair.get(0)) + " " + data.instance(nearestPair.get(1)));

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

    public void printDistances(double[][] distances) {
        for(int i=0; i<distances.length; i++) {
            for(int j=0; j<distances.length; j++) {
                System.out.print(distances[i][j] + "    ");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        String fileName = "data/weather.arff";
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
