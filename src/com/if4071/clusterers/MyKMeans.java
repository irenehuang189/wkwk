package com.if4071.clusterers;

import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SystemInfo;
import weka.core.converters.ArffLoader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
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
    private int numAttributes;
    private int numClusters;
    private int numData;
    private Map<Instance, Instances> clusters;

    public MyKMeans() {
        clusters = new HashMap<>();
    }

    public void buildClusterer(Instances data, int numCluster) {
        this.data = data;
        this.numAttributes = this.data.numAttributes();
        this.numClusters = numCluster;
        this.currentCentroids = new Instances(this.data, this.numClusters);
        this.numData = this.data.numInstances();
        this.clusters.clear();

        Instances previousCentroids = new Instances(this.data, this.numClusters);

        initializeCentroids();
        do {
            clusterByEuclideanDistance();
            updatePreviousCentroids(previousCentroids);
            calculateCentroids();
        } while (!isConvergent(previousCentroids));
    }

    private void initializeCentroids() {
        int[] centroidIndexes = new Random(10).ints(0, numData).distinct().limit(numClusters).toArray();
        for (int i = 0; i < centroidIndexes.length; i++) {
            currentCentroids.add(data.instance(centroidIndexes[i]));
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

    public void printResult() {
        Instance centroid;
        Instances cluster;
        int numClusterInstance;

        System.out.println("HASIL CLUSTERING KMEANS");
        System.out.println();
        for (int i = 0; i < numClusters; i++) {
            centroid = currentCentroids.instance(i);
            cluster = clusters.get(centroid);
            numClusterInstance = cluster.numInstances();

            System.out.println("Cluster " + (i + 1) + " (" + numClusterInstance + "): " + centroid);
            for (int j = 0; j < numClusterInstance; j++) {
                System.out.println(cluster.instance(j));
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        int numCluster;
        MyKMeans myKMeans = new MyKMeans();
        Scanner scanner = new Scanner(System.in);
        String fileName = "data/weather.nominal.arff";

        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            ArffLoader.ArffReader arffReader = new ArffLoader.ArffReader(br);
            Instances data = arffReader.getData();

//            System.out.print("Masukkan jumlah cluster: ");
//            numClusters = scanner.nextInt();
//            scanner.nextLine();
            numCluster = 2;
            myKMeans.buildClusterer(data, numCluster);
            myKMeans.calculateCentroids();
            myKMeans.printResult();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}