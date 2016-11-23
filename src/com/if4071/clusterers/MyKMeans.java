package com.if4071.clusterers;

import weka.clusterers.RandomizableClusterer;
import weka.core.Attribute;
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
    private Map<Instance,Instances> clusters = new HashMap<>();

    public MyKMeans() {

    }

    public void buildClusterer(Instances data, int numCluster) {
        this.data = data;
        this.numCluster = numCluster;
        this.currentCentroids = new Instances(this.data, this.numCluster);
        this.numData = this.data.numInstances();
        initializeCentroids();
    }

    private void initializeCentroids() {
        int[] centroidIndexes = new Random().ints(0, numData).distinct().limit(numCluster).toArray();
        for (int i = 0; i < centroidIndexes.length; i++) {
            currentCentroids.add(data.instance(centroidIndexes[i]));
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

    public static void main(String[] args) {
        int numCluster;
        MyKMeans myKMeans = new MyKMeans();
        Scanner scanner = new Scanner(System.in);
        String fileName = "data/weather.numeric.arff";

        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            ArffLoader.ArffReader arffReader = new ArffLoader.ArffReader(br);
            Instances data = arffReader.getData();

//            System.out.print("Masukkan jumlah cluster: ");
//            numCluster = scanner.nextInt();
//            scanner.nextLine();
            numCluster = 1;
            myKMeans.buildClusterer(data, numCluster);
            //System.out.println(myKMeans.currentCentroids.toString());
            myKMeans.calculateCentroids();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}