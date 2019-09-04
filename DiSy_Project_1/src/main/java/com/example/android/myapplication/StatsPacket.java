package com.example.android.myapplication;

import java.io.*;

public class StatsPacket implements Serializable{

    private int cpu;
    private long freeMemory;


    public StatsPacket(){
        this.cpu=(int) Runtime.getRuntime().availableProcessors();
        this.freeMemory=(long) Runtime.getRuntime().freeMemory();
    }

    public int getCPU(){
        return this.cpu;
    }

    public long getFreeMemory(){
        return this.freeMemory;
    }

    public String toString(){
        return "Number of processors : "+this.getCPU()+"\nAvailable Memory : "+this.getFreeMemory();
    }
}