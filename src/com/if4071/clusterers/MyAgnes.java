package com.if4071.clusterers;

import weka.core.*;
import weka.core.converters.ArffLoader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

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
    private ArrayList<MyAgnesNode> leaves;

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
        leaves = new ArrayList<>();
        for(int i=0; i<instancesNum; i++) {
            ArrayList index = new ArrayList();
            index.add(i);
            indices.add(index);

            leaves.add(new MyAgnesNode(String.valueOf(i)));
        }

    }

    public void setData(Instances data) {
        this.data = data;
    }

    public void buildClusterer() {
        initDistances();

        MyPairDistance nearestPairDistance = getNearestInitDistances(initDistances);
        mergeNearestIndices(nearestPairDistance);

        while(indices.size() > clusterNum) {
            buildNewCluster();
        }
    }

    private void buildNewCluster() {
        double[][] newDistances = new double[indices.size()][indices.size()];
        for(int i=0; i<indices.size(); i++) {
            for(int j=0; j<indices.size(); j++) {
                if(i != j) {
                    newDistances[i][j] = getNearestDistanceBetweenClusters(indices.get(i), indices.get(j));
                } else {
                    newDistances[i][j] = 0;
                }
            }
        }

        MyPairDistance nearestPairDistance = getNearestInitDistances(newDistances);
        mergeNearestIndices(nearestPairDistance);
    }

    private void mergeNearestIndices(MyPairDistance pairDistance) {
        ArrayList<Integer> nearestIndices = pairDistance.getNearestPair();
        // Get node label in string
        ArrayList<String> nodeLabels = new ArrayList<>();
        String parentLabel = "";
        for(int i=0; i<nearestIndices.size(); i++) {
            int idx = nearestIndices.get(i);
            String nodeLabel = intArrayToString(indices.get(idx));
            nodeLabels.add(nodeLabel);

            // Get parent label
            if(!parentLabel.isEmpty()) {
                parentLabel += " ";
            }
            parentLabel += nodeLabel;
        }

        // Create new parent and add to current tree
        MyAgnesNode parent = new MyAgnesNode(parentLabel);
        leaves.add(parent);
        for(int i=0; i<nodeLabels.size(); i++) {
            boolean isFound = false;
            int j = 0;
            while(j<leaves.size() - 1 && !isFound) { // Stop when reach size-1 because the last index is parent
                String label = leaves.get(j).getLabel();
                if(label.equals(nodeLabels.get(i))) {
                    isFound = true;
                    leaves.get(j).setParent(parent);
                    String edgeLabel = String.valueOf(pairDistance.getDistance()) + i;
                    parent.addChild(edgeLabel, leaves.get(j));
                } else {
                    j++;
                }
            }
        }

        // Merge nearest indices
        Integer destIndex = nearestIndices.get(0);
        Integer oriIndex = nearestIndices.get(1);
        indices.get(destIndex).addAll(indices.get(oriIndex));
        indices.remove((int) oriIndex);
    }

    private String intArrayToString(ArrayList<Integer> arr) {
        String result = "";
        for(int i=0; i<arr.size(); i++) {
            result += arr.get(i);
            if (i < arr.size()-1) {
                result += " ";
            }
        }
//        System.out.println(result);
        return result;
    }

    private double getNearestDistanceBetweenClusters(ArrayList<Integer> cluster1, ArrayList<Integer> cluster2) {
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

    private MyPairDistance getNearestInitDistances(double[][] distances) {
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

        return new MyPairDistance(nearestPair, minDistance);
    }

    private void initDistances() {
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

    public void printResult(long elapsedTime) {
        System.out.println("=== Run information ===\n");
        System.out.println("Relation:\t" + data.relationName());
        System.out.println("Instances:\t" + data.numInstances());
        System.out.println("Attributes:\t" + data.numAttributes());
        for (int i = 0; i < data.numAttributes(); i++) {
            System.out.println("\t\t\t" + data.attribute(i).name());
        }

        System.out.println("\n\n=== Clustering model (full training set) ===\n");
        int clusterNum = 0;
        for (int i = 0; i < leaves.size() - 1; i++) {
            MyAgnesNode node = leaves.get(i);
            if (node.getParent() == null) {
                System.out.println("Cluster " + clusterNum);
                node.print("", "");
                System.out.println();
                clusterNum++;
            }
        }
        System.out.println("Cluster " + clusterNum);
        leaves.get(leaves.size() - 1).print("", "");

        System.out.println("\n\n\nTime taken to build model : " + TimeUnit.NANOSECONDS.toSeconds(elapsedTime) + " seconds");
        System.out.println("\n=== Model and evaluation on training set ===\n");
        System.out.println("Clustered Instances\n");
        for (int i = 0; i < indices.size(); i++) {
            int clusterDataNum = indices.get(i).size();
            double percentage = (double) clusterDataNum / (double) data.numInstances() * 100;
            System.out.println(i + "\t" + clusterDataNum + "( " + (int) percentage + "%)");
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

            long startTime = System.nanoTime();
            MyAgnes myAgnes = new MyAgnes(data, 2, 1);
            myAgnes.buildClusterer();
            long elapsedTime = System.nanoTime() - startTime;
            myAgnes.printResult(elapsedTime);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
