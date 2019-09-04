package com.example.android.myapplication;/*This Class is just Serializable Class in order to send
a RealMatrix to be calculated from Master to com.example.android.myapplication.Worker and from
com.example.android.myapplication.Worker to Master*/

import java.io.Serializable;
import org.apache.commons.math3.linear.RealMatrix;

public class VectorPacket implements Serializable {
    int row;
    private RealMatrix matrix,vector;
    int id, mode;

    public VectorPacket(){

    }
    public VectorPacket(RealMatrix vec, RealMatrix mat, int mode){
        this.row=-1;
        if(vec != null) this.vector=vec.copy();
        if(mat != null) this.matrix=mat.copy();
        this.mode = mode;
    }

    public VectorPacket(int row,RealMatrix vec,RealMatrix mat){
        this.row=row;
        if(vec != null) this.vector=vec.copy();
        if(mat != null) this.matrix=mat.copy();
        this.mode = -1;
    }

    public VectorPacket(int row, int mode){
        this.row=row;
        this.mode = mode;
    }


    public RealMatrix getMatrix(){
        return this.matrix;
    }

    public void setMatrix(RealMatrix part){
        this.matrix=part.copy();
    }

    public RealMatrix getVector(){
        return this.vector;
    }

    public void setVector(RealMatrix part){
        this.vector=part.copy();
    }

    public int getNumberOfRow(){
        return this.row;
    }

    public void setNumberOfRow(int number){
        this.row=number;
    }

    public int getID(){
        return this.id;
    }

    public void setID(int number){
        this.id=number;
    }

    public int getMode(){
        return this.mode;
    }

    public void setMode(int number){
        this.mode=number;
    }

}