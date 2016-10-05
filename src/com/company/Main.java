package com.company;
import java.io.*;
import java.util.Random;
import java.util.Scanner;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.Id3;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.SystemInfo;
import weka.core.converters.ArffLoader.ArffReader;
import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;
import weka.filters.unsupervised.attribute.Remove;

public class Main {

    public static void main(String[] args) throws Exception {
        ArffReader arff;
        Instances data;
        Instances train;
        Instances test;
        Resample filter;
        Evaluation eval;

        Scanner scanner = new Scanner(System.in);
        System.out.print("Masukkan file arff: ");

        String filename = scanner.next();

        try (BufferedReader br = new BufferedReader(
                new FileReader(filename))) {

            arff = new ArffReader(br);
        }

        data = arff.getData();
        data.setClassIndex(data.numAttributes() - 1);

        System.out.print("Hilangkan atribut? (Y/N) ");
        String isRemoveAttr = scanner.next();
        if (isRemoveAttr.equals("Y")) {

            System.out.println("Masukkan indeks atribut yang ingin dihapus: ");
            String[] options = new String[2];
            options[0] = "-R";
            options[1] = scanner.next();

            Remove remove = new Remove();

            remove.setOptions(options);
            remove.setInputFormat(data);
            data = Filter.useFilter(data, remove);

        }
        System.out.print("Filter dengan Resample? (Y/N) ");
        String isResample = scanner.next();
        if (isResample.equals("Y")) {
            filter = new Resample();

            filter.setInputFormat(data);
            filter.setNoReplacement(false);
            filter.setSampleSizePercent(100);

            data = Filter.useFilter(data, filter);
        }

        System.out.println("Pilih jenis pohon yang diinginkan:");
        System.out.println("1. ID3 Weka");
        System.out.println("2. J48 Weka");
        System.out.println("3. MyID3");
        System.out.println("4. MyC45");

        int treeOpt = scanner.nextInt();

        System.out.println("Pilih jenis evaluasi yang diinginkan:");
        System.out.println("1. Masukkan test set");
        System.out.println("2. 10-Fold Cross Validation");
        System.out.println("3. Percentage Split");
        System.out.println("4. Unseen data");

        int evalOpt = scanner.nextInt();

        System.out.print("\nSave model? (Y/N) ");
        String isSave = scanner.next();
        String modelName = "tree";
        if (isSave.equals("Y")) {
            System.out.print("Masukkan nama model: ");
            modelName = scanner.next();
        }

        if ((evalOpt == 1) || (evalOpt == 4)) {
            System.out.print("Masukkan file arff: ");
            String testfile = scanner.next();

            try (BufferedReader br = new BufferedReader(
                    new FileReader(testfile))) {

                arff = new ArffReader(br);
            }

            test = arff.getData();
            test.setClassIndex(data.numAttributes() - 1);

            train = data;
        }
        else if (evalOpt == 3){
            System.out.println("Masukkan persentase training data: ");
            double percent = scanner.nextDouble();
            int trainSize = (int) Math.round(data.numInstances() * percent / 100);
            int testSize = data.numInstances() - trainSize;
            data.randomize(new Random());

            train = new Instances(data, 0, trainSize);
            test = new Instances(data, trainSize, testSize);
        }
        else {
            train = data;
            test = data;
        }

        switch (treeOpt) {
            case 1:
                Id3 treeID3 = new Id3();
                treeID3.buildClassifier(train);
                System.out.println("\n\n" + treeID3.toString());
                evaluateWekaModel(evalOpt, treeID3, test);
                saveModel(isSave, modelName, treeID3);
                break;
            case 2:
                J48 treeJ48 = new J48();
                treeJ48.buildClassifier(train);
                System.out.println("\n\n" + treeJ48.toString());
                evaluateWekaModel(evalOpt, treeJ48, test);
                saveModel(isSave, modelName, treeJ48);
                break;
            case 3:
                Id3 treeMyID3 = new Id3();
                treeMyID3.buildClassifier(train);
                //evaluate
                break;
            case 4:
                J48 treeMyC45 = new J48();
                treeMyC45.buildClassifier(train);
                //evaluate
                break;
        }



    }


    public static void evaluateWekaModel(int evalOpt, Classifier tree, Instances data) throws Exception {
        Evaluation eval = new Evaluation(data);

        if ((evalOpt == 1) || (evalOpt == 3) || (evalOpt == 4)){
            eval.evaluateModel(tree, data);
        }
        else if (evalOpt == 2) {
            eval.crossValidateModel(tree, data, 10, new Random());
        }
        System.out.println(eval.toSummaryString("\n\n\nResult\n======", false));
    }

     private static void saveModel(String isSave, String modelName, Classifier tree) throws IOException {
            if (isSave.equals("Y")) {
                ObjectOutputStream oos = new ObjectOutputStream(
                        new FileOutputStream("savedmodel/" + modelName + ".model"));

                oos.writeObject(tree);

                oos.flush();
                oos.close();
            }
    }
}