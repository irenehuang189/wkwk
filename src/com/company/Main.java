package com.company;
import java.io.*;
import java.util.Random;
import java.util.Scanner;

import com.if4071.classifiers.trees.MyEvaluation;
import com.if4071.classifiers.trees.MyID3;
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
import weka.filters.unsupervised.attribute.ReplaceMissingValues;

public class Main {
    static Scanner scanner = new Scanner(System.in);
    public static void main(String[] args) throws Exception {
        ArffReader arff;
        Instances data;
        Instances train;
        Instances test;
        Evaluation eval;

        int model = displayMainMenu();

        if (model == 2){
            String loadFile = displayModelFile();
            ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(loadFile));
            Classifier tree = (Classifier) ois.readObject();
            ois.close();

            int evalOpt = displayEvalOptions(model);
            if(evalOpt==1) {
                String filename = displayArffFile();


                try (BufferedReader br = new BufferedReader(
                        new FileReader(filename))) {

                    arff = new ArffReader(br);
                }

                data = arff.getData();
                data.setClassIndex(data.numAttributes() - 1);

                String[] remove = displayRemoveAttr();
                if (remove[0].equals("-R")){
                    data = removeAttr(remove,data);
                }
                String isResample = displayResample();
                if (isResample.equals("Y")) {
                    data = resample(data);
                }

                evaluateWekaModel(1, tree, data);
            }
            //else if unseen data

        }
        else {
            String filename = displayArffFile();
            //data
            try (BufferedReader br = new BufferedReader(
                    new FileReader(filename))) {

                arff = new ArffReader(br);
            }

            data = arff.getData();
            data.setClassIndex(data.numAttributes() - 1);

            String[] remove = displayRemoveAttr();
            if (remove[0].equals("-R")){
                data = removeAttr(remove,data);
            }
            String isResample = displayResample();
            if (isResample.equals("Y")) {
                data = resample(data);
            }

            int treeOpt = displayTreeOptions();

            int evalOpt = displayEvalOptions(model);

            if ((evalOpt == 1) || (evalOpt == 2)) {
                String testfile = displayArffFile();

                try (BufferedReader br = new BufferedReader(
                        new FileReader(testfile))) {

                    arff = new ArffReader(br);


                    test = arff.getData();
                    test.setClassIndex(data.numAttributes() - 1);

                    train = data;
                }
            } else if (evalOpt == 4) {
                System.out.println("**                                                        **");
                System.out.println("************************************************************");
                System.out.println("**                                                        **");
                System.out.println("**           Masukkan persentase training data: ");
                double percent = scanner.nextDouble();
                int trainSize = (int) Math.round(data.numInstances() * percent / 100);
                int testSize = data.numInstances() - trainSize;
                data.randomize(new Random());

                train = new Instances(data, 0, trainSize);
                test = new Instances(data, trainSize, testSize);
            } else {
                train = data;
                test = data;
            }

            processTree(treeOpt,evalOpt,train,test);

        }

    }

    public static int displayMainMenu(){
        System.out.println("************************************************************");
        System.out.println("**                                                        **");
        System.out.println("**                      Weka vs WKWK                      **");
        System.out.println("**                      ************                      **");
        System.out.println("**                   1. Build New Model                   **");
        System.out.println("**                   2. Load Saved Model                  **");
        System.out.print("**                     Your choice: ");
        return scanner.nextInt();
    }

    public static String displayArffFile(){
        System.out.println("**                                                        **");
        System.out.println("************************************************************");
        System.out.println("**                                                        **");
        System.out.print("**           Masukkan file ARFF: ");
        return scanner.next();
    }

    public static String displayModelFile(){
        System.out.println("**                                                        **");
        System.out.println("************************************************************");
        System.out.println("**                                                        **");
        System.out.print("**           Masukkan file MODEL: ");
        return scanner.next();
    }

    public static String[] displayRemoveAttr(){
        String[] options = new String[2];
        System.out.println("**                                                        **");
        System.out.println("************************************************************");
        System.out.println("**                                                        **");
        System.out.print("**           Hilangkan atribut? (Y/N) ");
        String isRemoveAttr = scanner.next();

        if (isRemoveAttr.equals("Y")) {
            System.out.println("**                                                        **");
            System.out.print("**     Masukkan indeks atribut yang ingin dihapus: ");

            options[0] = "-R";
            options[1] = scanner.next();


        }
        else options[0] = "N";

        return options;
    }

    public static String displayResample() {
        System.out.println("**                                                        **");
        System.out.println("************************************************************");
        System.out.println("**                                                        **");
        System.out.print("**            Filter dengan Resample? (Y/N) ");
        String isResample = scanner.next();
        return isResample;
    }

    public static Instances removeAttr(String[] options, Instances data) throws Exception{
        Remove remove = new Remove();

        remove.setOptions(options);
        remove.setInputFormat(data);
        data = Filter.useFilter(data, remove);
        return data;

    }

    public static Instances resample(Instances data) throws Exception{
        Resample filter = new Resample();

        filter.setInputFormat(data);
        filter.setNoReplacement(false);
        filter.setSampleSizePercent(100);

        data = Filter.useFilter(data, filter);
        return data;
    }

    public static int displayTreeOptions() {
        System.out.println("**                                                        **");
        System.out.println("************************************************************");
        System.out.println("**                                                        **");
        System.out.println("**           Pilih jenis pohon yang diinginkan:           **");
        System.out.println("**                      1. ID3 Weka                       **");
        System.out.println("**                      2. J48 Weka                       **");
        System.out.println("**                      3. MyID3                          **");
        System.out.println("**                      4. MyC45                          **");
        System.out.print("**               Masukkan pilihan pohon: ");
        int treeOpt = scanner.nextInt();
        return treeOpt;
    }

    public static int displayEvalOptions(int mode) {
        System.out.println("**                                                        **");
        System.out.println("************************************************************");
        System.out.println("**                                                        **");
        System.out.println("**          Pilih jenis evaluasi yang diinginkan:         **");
        System.out.println("**                1. Input Test Set                       **");
        System.out.println("**                2. Unseen Data                          **");
        if (mode == 1) {
            System.out.println("**                3. 10-Fold Cross Validation             **");
            System.out.println("**                4. Percentage Split                     **");
        }
        System.out.print("**             Masukkan pilihan evaluasi: ");
        int evalOpt = scanner.nextInt();
        return evalOpt;
    }

    public static String displaySave(){
        System.out.println("**                                                        **");
        System.out.println("************************************************************");
        System.out.println("**                                                        **");
        System.out.print("**                  Save model? (Y/N) ");
        String isSave = scanner.next();
        String modelName = null;
        if (isSave.equals("Y")) {
            System.out.print("Masukkan nama model: ");
            modelName = scanner.next();
        }
        return modelName;
    }

    public static void evaluateWekaModel(int evalOpt, Classifier tree, Instances data) throws Exception {
        Evaluation eval = new Evaluation(data);

        if ((evalOpt == 1) || (evalOpt == 2) || (evalOpt == 4)){
            eval.evaluateModel(tree, data);
        }
        else if (evalOpt == 3) {
            eval.crossValidateModel(tree, data, 10, new Random());
        }
        System.out.println(eval.toSummaryString("\n\n\nResult\n======", false));
    }


    public static void evaluateMyModel(int evalOpt, Classifier tree, Instances data, int treeOpt) throws Exception {
        MyEvaluation eval = new MyEvaluation();

        if ((evalOpt == 1) || (evalOpt == 2) || (evalOpt == 4)){
            eval.evaluateModel(tree,data,treeOpt);
        }
        else if (evalOpt == 3) {
            eval.crossValidation(tree,data,treeOpt);
        }
    }

    private static Instances[] processEval(int evalOpt, Instances data) throws Exception {
        Instances test, train;
        if ((evalOpt == 1) || (evalOpt == 2)) {
            String testfile = displayArffFile();

            try (BufferedReader br = new BufferedReader(
                    new FileReader(testfile))) {

                ArffReader arff = new ArffReader(br);


                test = arff.getData();
                test.setClassIndex(data.numAttributes() - 1);

                train = data;
            }
        } else if (evalOpt == 4) {
            System.out.println("**                                                        **");
            System.out.println("************************************************************");
            System.out.println("**                                                        **");
            System.out.println("**           Masukkan persentase training data: ");
            double percent = scanner.nextDouble();
            int trainSize = (int) Math.round(data.numInstances() * percent / 100);
            int testSize = data.numInstances() - trainSize;
            data.randomize(new Random());

            train = new Instances(data, 0, trainSize);
            test = new Instances(data, trainSize, testSize);
        } else {
            train = data;
            test = data;
        }
        Instances[] newdata = new Instances[2];
        newdata[0] = train;
        newdata[1] = test;
        return newdata;
    }

    public static void processTree(int treeOpt, int evalOpt, Instances train, Instances test) throws Exception{
        String saveName = null;
        if (treeOpt == 1 || treeOpt == 2){
            saveName = displaySave();
        }
        switch (treeOpt) {
            case 1:
                Id3 treeID3 = new Id3();
                treeID3.buildClassifier(train);
                System.out.println("\n\n" + treeID3.toString());
                evaluateWekaModel(evalOpt, treeID3, test);
                saveModel(saveName, treeID3);
                break;
            case 2:
                J48 treeJ48 = new J48();
                treeJ48.buildClassifier(train);
                System.out.println("\n\n" + treeJ48.toString());
                evaluateWekaModel(evalOpt, treeJ48, test);
                saveModel(saveName, treeJ48);
                break;
            case 3:
                MyID3 treeMyID3 = new MyID3();
                treeMyID3.buildClassifier(train);
                evaluateMyModel(evalOpt,treeMyID3,test,treeOpt);
                break;
            case 4:
                J48 treeMyC45 = new J48();
                treeMyC45.buildClassifier(train);
                evaluateMyModel(evalOpt,treeMyC45,test,treeOpt);
                break;
        }
    }

     private static void saveModel(String modelName, Classifier tree) throws IOException {
         if (!modelName.equals(null)) {
             ObjectOutputStream oos = new ObjectOutputStream(
                     new FileOutputStream("savedmodel/" + modelName + ".model"));

             oos.writeObject(tree);

             oos.flush();
             oos.close();
         }
    }
}