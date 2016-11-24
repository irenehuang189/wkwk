package com.if4071.clusterers;

import weka.core.Attribute;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

/**
 * Created by TOSHIBA on 23-Nov-16.
 */
public class MyKMeans {
    private Instances currentCentroids;
    private Instances data;
    private Instances initialCentroids;
    private int numAttributes;
    private int numClusters;
    private int numData;
    private int numIterations;
    private Map<Instance, Instances> clusters;

    public MyKMeans() {
        clusters = new HashMap<>();
    }

    public void buildClusterer(Instances data, int numCluster) {
        this.data = data;
        this.numAttributes = this.data.numAttributes();
        this.numClusters = numCluster;
        this.currentCentroids = new Instances(this.data, this.numClusters);
        this.initialCentroids = new Instances(this.data, this.numClusters);
        this.numData = this.data.numInstances();
        this.numIterations = 0;
        this.clusters.clear();

        Instances previousCentroids = new Instances(this.data, this.numClusters);

        initializeCentroids();
        do {
            numIterations++;
            clusterByEuclideanDistance();
            updatePreviousCentroids(previousCentroids);
            calculateCentroids();
        } while (!isConvergent(previousCentroids));

        printResult();
    }

    private void initializeCentroids() {
        Instance centroid;
        int[] centroidIndexes = new Random(10).ints(0, numData).distinct().limit(numClusters).toArray();

        for (int i = 0; i < centroidIndexes.length; i++) {
            centroid = data.instance(centroidIndexes[i]);
            initialCentroids.add(centroid);
            currentCentroids.add(centroid);
            clusters.put(currentCentroids.instance(i), new Instances(data, numData));
        }
    }

    private void clusterByEuclideanDistance() {
        double distance, minDistance;
        Instance centroid, closestCentroid, instance;
        EuclideanDistance euclidean = new EuclideanDistance(data);

        resetClusters();
        for (int i = 0; i < numData; i++) {
            instance = data.instance(i);
            closestCentroid = currentCentroids.instance(0);
            minDistance = euclidean.distance(instance, closestCentroid);

            for (int j = 1; j < numClusters; j++) {
                centroid = currentCentroids.instance(j);
                distance = euclidean.distance(instance, centroid);

                if (distance < minDistance) {
                    minDistance = distance;
                    closestCentroid = centroid;
                }
            }

            clusters.get(closestCentroid).add(instance);
        }
    }

    private boolean isConvergent(Instances previousCentroids) {
        Instance currentCentroid, previousCentroid;

        for (int i = 0; i < numClusters; i++) {
            currentCentroid = currentCentroids.instance(i);
            previousCentroid = previousCentroids.instance(i);

            for (int j = 0; j < numAttributes; j++) {
                if (currentCentroid.attribute(j).isNominal()) {
                    if (!currentCentroid.stringValue(j).equals(previousCentroid.stringValue(j))) {
                        return false;
                    }
                } else {
                    if (currentCentroid.value(j) != previousCentroid.value(j)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private void updatePreviousCentroids(Instances previousCentroids) {
        int numPreviousCentroidsInstance = previousCentroids.numInstances();

        for (int i = 0; i < numClusters; i++) {
            if (numPreviousCentroidsInstance == 0) {
                previousCentroids.add(currentCentroids.instance(i));
            } else {
                for (int j = 0; j < numAttributes; j++) {
                    if (data.attribute(j).isNominal()) {
                        previousCentroids.instance(i).setValue(j, currentCentroids.instance(i).stringValue(j));
                    } else {
                        previousCentroids.instance(i).setValue(j, currentCentroids.instance(i).value(j));
                    }
                }
            }
        }
    }

    private void resetClusters() {
        for (int i = 0; i < numClusters; i++) {
            clusters.get(currentCentroids.instance(i)).delete();
        }
    }

    private void calculateCentroids(){
        for(int i=0; i < currentCentroids.numInstances(); i++){
            Instances cluster = clusters.get(currentCentroids.instance(i));

            for(int j = 0; j < cluster.numAttributes(); j++){
                double meanOrMode =  cluster.meanOrMode(j);

                if(cluster.attribute(j).isNumeric()){
                    currentCentroids.instance(i).setValue(j, meanOrMode);
                }
                else {
                    String meanOrModeValue = cluster.attribute(j).value((int)meanOrMode);
                    currentCentroids.instance(i).setValue(j, meanOrModeValue);
                }

            }
        }
    }

    private void printResult() {
        Attribute attribute;
        double meanOrModeValue;
        Instance centroid;
        Instances cluster;
        int numClusterInstances;

        System.out.println("=== Run information ===");
        System.out.println();
        System.out.println("Relation:\t" + data.relationName());
        System.out.println("Instances:\t" + numData);
        System.out.println("Attributes:\t" + numAttributes);
        for (int i = 0; i < numAttributes; i++) {
            System.out.println("\t\t\t" + data.attribute(i).name());
        }
        System.out.println();
        System.out.println();
        System.out.println("=== Clustering model ===");
        System.out.println();
        System.out.println("MyKMeans");
        System.out.println("========");
        System.out.println();
        System.out.println("Number of iterations: " + numIterations);
        System.out.println();
        System.out.println("Initial starting points (random):");
        System.out.println();
        for (int i = 0; i < numClusters; i++) {
            System.out.println("Cluster " + i + ": " + initialCentroids.instance(i));
        }
        System.out.println();
        System.out.println("Final cluster centroids:");
        System.out.println();
        for (int i = 0; i < numClusters; i++) {
            centroid = currentCentroids.instance(i);
            System.out.println("Cluster " + i + " (" + clusters.get(centroid).numInstances() + "): " + centroid);
        }
        System.out.println();
        System.out.println();
        System.out.println("=== Model and evaluation on training set ===");
        System.out.println();
        System.out.println("Clustered Instances");
        System.out.println();
        for (int i = 0; i < numClusters; i++) {
            centroid = currentCentroids.instance(i);
            numClusterInstances = clusters.get(centroid).numInstances();
            System.out.print(i + "\t\t" + numClusterInstances + "\t(");
            System.out.printf("%.2f", (double) numClusterInstances / (double) numData * 100);
            System.out.print("%)");
            System.out.println();
        }
    }

    public static void main(String[] args) {
        int numCluster;
        MyKMeans myKMeans = new MyKMeans();
        Scanner scanner = new Scanner(System.in);
        String fileName = "data/iris.arff";

        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            ArffLoader.ArffReader arffReader = new ArffLoader.ArffReader(br);
            Instances data = arffReader.getData();

//            System.out.print("Masukkan jumlah cluster: ");
//            numClusters = scanner.nextInt();
//            scanner.nextLine();
            numCluster = 2;
            myKMeans.buildClusterer(data, numCluster);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}