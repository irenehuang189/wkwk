package com.if4071.clusterers;

import weka.clusterers.RandomizableClusterer;
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
    private int numCluster;
    private int numData;
    private Map<Instance, Instances> clusters;

    public MyKMeans() {
        clusters = new HashMap<>();
    }

    public void buildClusterer(Instances data, int numCluster) {
        this.data = data;
        this.numCluster = numCluster;
        this.currentCentroids = new Instances(this.data, this.numCluster);
        this.numData = this.data.numInstances();
        this.clusters.clear();

        initializeCentroids();
        clusterByEuclideanDistance();
    }

    private void initializeCentroids() {
        int[] centroidIndexes = new Random().ints(0, numData).distinct().limit(numCluster).toArray();
        for (int i = 0; i < centroidIndexes.length; i++) {
            currentCentroids.add(data.instance(centroidIndexes[i]));
            clusters.put(currentCentroids.instance(i), new Instances(data, numData));
        }
    }

    private void clusterByEuclideanDistance() {
        double distance, minDistance;
        Instance centroid, closestCentroid, instance;
        EuclideanDistance euclidean = new EuclideanDistance(data);

        for (int i = 0; i < numData; i++) {
            instance = data.instance(i);
            System.out.println(i);
            closestCentroid = currentCentroids.instance(0);
            minDistance = euclidean.distance(instance, closestCentroid);
            System.out.println(minDistance);

            for (int j = 1; j < numCluster; j++) {
                centroid = currentCentroids.instance(j);
                distance = euclidean.distance(instance, centroid);
                System.out.println(distance);

                if (distance < minDistance) {
                    minDistance = distance;
                    closestCentroid = centroid;
                }
            }

            clusters.get(closestCentroid).add(instance);
            System.out.println();
        }
    }

    private void calculateCentroids(){
        // debug set dummy cluster
        // clusters.put(currentCentroids.instance(0),data);
        //System.out.println(clusters.toString());

        for(int i=0; i < currentCentroids.numInstances(); i++){
            Instances cluster = clusters.get(currentCentroids.instance(i));

            for(int j = 0; j < cluster.numAttributes(); j++){
                double meanOrMode =  cluster.meanOrMode(j);

                if(cluster.attribute(j).isNumeric()){
                    //System.out.println(meanOrMode);
                    currentCentroids.instance(i).setValue(j, meanOrMode);
                }
                else {
                    String meanOrModeValue = cluster.attribute(j).value((int)meanOrMode);
                    //System.out.println(meanOrModeValue);
                    currentCentroids.instance(i).setValue(j, meanOrModeValue);
                }

            }

            System.out.println("new centroid: " + currentCentroids.instance(i).toString());

        }
    }

    public void printResult() {
        Instance centroid;
        Instances cluster;
        int numClusterInstance;

        System.out.println("HASIL CLUSTERING KMEANS");
        System.out.println();
        for (int i = 0; i < numCluster; i++) {
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
//            numCluster = scanner.nextInt();
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