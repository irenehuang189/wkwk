package com.if4071.clusterers;

import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Scanner;

/**
 * Created by jessica on 11/24/2016.
 */
public class Main {
    static Scanner scanner = new Scanner(System.in);
    public static void main(String[] args) throws Exception {
        displayMainMenu();
        //Meminta masukan pilihan jenis cluster
        //1 untuk MyAgnes dan 2 untuk MyKMeans
        int clusterOpt = getClusteringOpt();

        //Meminta masukan dataset
        String arff = getArffFile();

        BufferedReader br = new BufferedReader(new FileReader(arff));
        ArffLoader.ArffReader arffReader = new ArffLoader.ArffReader(br);
        Instances data = arffReader.getData();

        //Meminta masukan jumlah cluster
        int numCluster = getClusterNum();

        if(clusterOpt == 1){
            /* Jika memilih MyAgnes */
            int linkType = getLinkType();
            long startTime = System.nanoTime();
            MyAgnes myAgnes = new MyAgnes(data, numCluster, linkType);
            myAgnes.buildClusterer();
            long elapsedTime = System.nanoTime() - startTime;

            myAgnes.printResult(elapsedTime);
        }
        else if (clusterOpt == 2){
            /* Jika memilih MyKMeans */
            MyKMeans myKMeans = new MyKMeans();
            myKMeans.buildClusterer(data, numCluster);

        }
    }

    private static void displayMainMenu(){
        System.out.println("************************************************************");
        System.out.println("**                                                        **");
        System.out.println("**                    WKWK Clustering                     **");
        System.out.println("**                                                        **");

    }

    private static int getClusteringOpt(){
        int option;
        System.out.println("************************************************************");
        System.out.println("**                                                        **");
        System.out.println("**        Pilih jenis clustering yang diinginkan:         **");
        System.out.println("**                      1. MyAgnes                        **");
        System.out.println("**                      2. MyK-Means                      **");
        System.out.print("**               Masukkan pilihan cluster: ");
        option = scanner.nextInt();

        while ((option != 1) && (option != 2)){
            System.out.println("**      Pilihan tidak ada. Masukkan pilihan 1 atau 2.     **");
            System.out.print("**               Masukkan pilihan cluster: ");
            option = scanner.nextInt();
        }

        return option;
    }

    private static String getArffFile(){
        System.out.println("**                                                        **");
        System.out.println("************************************************************");
        System.out.println("**                                                        **");
        System.out.print("**           Masukkan file ARFF: ");
        return scanner.next();
    }

    private static int getClusterNum(){
        System.out.println("**                                                        **");
        System.out.println("************************************************************");
        System.out.println("**                                                        **");
        System.out.print  ("**              Masukkan jumlah cluster: ");
        int num = scanner.nextInt();
        System.out.println("**                                                        **");
        System.out.println("************************************************************");
        return num;
    }

    private static int getLinkType(){
        int option;
        System.out.println("************************************************************");
        System.out.println("**                                                        **");
        System.out.println("**         Pilih jenis tipe link yang diinginkan:         **");
        System.out.println("**                      1. Single link                    **");
        System.out.println("**                      2. Complete link                  **");
        System.out.print("**               Masukkan pilihan tipe link: ");
        option = scanner.nextInt();

        while ((option != 1) && (option != 2)){
            System.out.println("**      Pilihan tidak ada. Masukkan pilihan 1 atau 2.     **");
            System.out.print("**               Masukkan pilihan tipe link: ");
            option = scanner.nextInt();
        }

        return option;
    }
}
