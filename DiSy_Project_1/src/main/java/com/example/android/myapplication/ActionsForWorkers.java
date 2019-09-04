package com.example.android.myapplication;

import java.io.*;
import java.net.Socket;

public class ActionsForWorkers extends Thread {
    StatsPacket stats = null;
    Socket connection = null;
    ObjectInputStream in;
    ObjectOutputStream out;
    VectorPacket data;
    int mode, workerID;

    public ActionsForWorkers() {}

    public ActionsForWorkers(Socket s, ActionsForWorkers AFW, int workerID) {

        try {
            connection = s;
            in = new ObjectInputStream(connection.getInputStream());
            out = new ObjectOutputStream(connection.getOutputStream());
            this.data = AFW.data;
            this.mode = AFW.mode;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ActionsForWorkers(Socket s, VectorPacket d, int mode, int workerID) {

        try {
            connection = s;
            in = new ObjectInputStream(connection.getInputStream());
            out = new ObjectOutputStream(connection.getOutputStream());
            data=d;
            this.mode=mode;
            this.workerID = workerID;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcast(){
        try {
            out.writeObject(data);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.close();
    }

    public void run() {

        try {
            out.writeObject(data);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            data = (VectorPacket)in.readObject();
            if (mode==1) {
                synchronized (Master.Y) {
                    Master.Y.setRowMatrix(data.getNumberOfRow(), data.getVector().transpose());
                    Master.Ycount++;
                }
            }
            else{
                synchronized (Master.X) {
                    Master.X.setRowMatrix(data.getNumberOfRow(), data.getVector().transpose());
                    Master.Xcount++;
                }
            }
            this.close();
            //System.out.println("Thread : "+ this.toString());
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            in.close();
            out.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}