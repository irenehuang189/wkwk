package com.if4071.clusterers;

import java.util.ArrayList;

/**
 * Created by irn on 24/11/2016.
 */
public class MyPairDistance {
    private ArrayList<Integer> nearestPair;
    private double distance;

    public MyPairDistance(ArrayList<Integer> nearestPair, double distance) {
        this.nearestPair = nearestPair;
        this.distance = distance;
    }

    public ArrayList<Integer> getNearestPair() {
        return nearestPair;
    }

    public double getDistance() {
        return distance;
    }

    @Override
    public String toString() {
        return "MyPairDistance{" +
                "nearestPair=" + nearestPair +
                ", distance=" + distance +
                '}';
    }
}
