package com.example.android.myapplication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Comparator;

public class WorkerConnection implements Comparator, Comparable<WorkerConnection>{
    String worker_IP = null;
    StatsPacket stats = null;
    Socket worker_connection = null;
    ObjectInputStream in;
    ObjectOutputStream out;
    boolean isOnline = false;
    double assessment;
    int worker_port;

    public WorkerConnection(Socket connection) {
        try {
            worker_connection = connection;
            out = new ObjectOutputStream(worker_connection.getOutputStream());
            in = new ObjectInputStream(worker_connection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connect() {
        try {
            Vec2<String, Integer> config = (Vec2<String, Integer>)in.readObject();
            worker_IP = config.getTValue();
            worker_port = config.getYValue();
            stats = (StatsPacket)in.readObject();
            System.out.println(stats);
            isOnline = true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch(ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            in.close();
            out.close();
            worker_connection.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public int compare(Object a, Object b) {
        a = (WorkerConnection)a;
        return((Comparable)a).compareTo((WorkerConnection)b);
    }

    public int compareTo(WorkerConnection p){
        if (this.assessment<p.assessment){
            return -1;
        }else if(this.assessment>p.assessment){
            return 2;
        }else {
            return 0;
        }
    }

    public ObjectInputStream in() {
        return in;
    }

    public ObjectOutputStream out() {
        return out;
    }

    public double getAssessment(){
        return assessment;
    }

    public void setAssessment(double assessment) {
        this.assessment = assessment;
    }

    public StatsPacket getStats(){
        return this.stats;
    }
}
