package com.example.android.myapplication;/*
 * com.example.android.myapplication.Vec2.java
 */
import java.io.Serializable;
import java.util.Comparator;

public class Vec2<T, Y> implements Serializable{
    /*
     * The actual T type data
     */
    protected T value1;

    /*
     * The actual Y type data
     */
    protected Y value2;

    /*
     * Basic constructor setting automatically all fields equal to null (or 0.0).
     */
    public Vec2(){}

    /*
     * Overloaded constructor for two-dimensional vectors constructed in State.java; supporting several numerical
     * operators that can be used on com.example.android.myapplication.Vec2<T, Y> and T, Y data.
     * value1 The actual T type data.
     * value2 The actual Y type data.
     */
    public Vec2(T value1, Y value2){
        this.value1 = value1;
        this.value2 = value2;
    }

    /*
     * Getter for value1: the actual T type data
     * value1 Value of the actual T type data
     */
    public T getTValue(){
        return value1;
    }

    /*
     * Getter for value2: the actual Y type data
     * value2 Value of the actual Y type data
     */
    public Y getYValue(){
        return value2;
    }

    /*
     * Setter for value1: the actual T type data
     * value1 Value of the actual T type data
     */
    public void setTValue(T value1){
        this.value1 = value1;
    }

    /*
     * Setter for value2: the actual Y type data
     * value2 Value of the actual Y type data
     */
    public void setYValue(Y value2){
        this.value2 = value2;
    }

    /*
     * Implementation of overridden method toString.
     */
    public String toString(){
        return this.value1 + ", " + this.value2;
    }
}
