package com.if4071.clusterers;

import weka.clusterers.RandomizableClusterer;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}