package com.example.android.myapplication;

import java.io.*;
import java.net.Socket;

public class ActionsForMaster extends Thread {
    StatsPacket stats = null;
    Socket connection = null;
    ObjectInputStream in;
    ObjectOutputStream out;
    VectorPacket packet, result;

    public ActionsForMaster(Socket s, VectorPacket packet, ObjectInputStream in, ObjectOutputStream out) {
        connection = s;
        this.packet = packet;
        this.in = in;
        this.out = out;
    }

    public void run() {
        PartOfArray part = new PartOfArray(packet.getVector(), packet.getMatrix());
        result=new VectorPacket(packet.getNumberOfRow(),part.calculate_XU(),packet.getMatrix());
        // System.out.println(com.example.android.myapplication.PartOfArray.Y().getEntry(0,0));
        this.sendResultsToMaster();
    }

    public void sendResultsToMaster(){
        try {
            out.writeObject(result);
            out.flush();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
