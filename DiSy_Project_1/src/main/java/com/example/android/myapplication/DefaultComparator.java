package com.example.android.myapplication;

import java.util.Comparator;

/**
 * com.example.android.myapplication.DefaultComparator.java
 */
final class DefaultComparator implements Comparator {
    public int compare(Object a, Object b) {
        return ((Comparable)a).compareTo(b);
    }
}