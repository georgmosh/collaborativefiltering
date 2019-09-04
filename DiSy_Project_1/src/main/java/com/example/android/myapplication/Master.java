package com.example.android.myapplication;


import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;

public class Master {

    public static void main(String args[]) {
        new Master(2, 4201, 4202).startMaster();
    }

    static RealMatrix R, X, Y, C, P;
    int k = 20; // inner matrix dimension, f variable in the paper
    int fileChooser=2; // Reading mode: 1 for csv , 2 for JSON
    double alpha = 40; // variable α for C matrix calculation
    static double lambda = 0.1; // weight for the sum of X,Y norms
    int num_of_workers;
    public int port_for_workers, port_for_android;
    protected static int Xcount = 0, Ycount = 0;
    ArrayList<Integer> WLF;
    ArrayList<Double> WLF2;
    static ArrayList<Poi> apoi = null;

    /* Define the socket that is used to handle the connection */
    ServerSocket providerSocket, androidSocket;
    static ArrayList<WorkerConnection> worker_connections = null;

    public Master(int n1, int n2, int n3) {
        num_of_workers = n1;
        port_for_workers = n2;
        port_for_android = n3;
        worker_connections = new ArrayList<WorkerConnection>(num_of_workers);
    }

    private void initialize() {
        /* Create Server Socket */
        try {
            providerSocket=new ServerSocket(port_for_workers,num_of_workers);
            androidSocket=new ServerSocket(port_for_android);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Master is Online!");
    }

    public void prepare() {
        R=this.readSparseMatrix();
        X=MatrixUtils.createRealMatrix(R.getRowDimension(), k);
        Y =MatrixUtils.createRealMatrix( R.getColumnDimension(), k);
        /*
        Populate X,Y with random numbers;
         */
        RandomGenerator randomGenerator=new JDKRandomGenerator();
        randomGenerator.setSeed(1);
        for (int i=0; i<X.getRowDimension();i++){    //Populating X
            for (int j=0; j<X.getColumnDimension(); j++) {
                X.setEntry(i, j, randomGenerator.nextDouble());
            }
        }
        for (int i=0; i<Y.getRowDimension();i++){    //Populating Y
            for (int j=0; j<Y.getColumnDimension(); j++){
                Y.setEntry(i,j,randomGenerator.nextDouble());
            }
        }
        C=R.copy();
        C = calculateCMatrix();
        P=R.copy();
        P = calculatePMatrix();
    }

    public void startMaster() {
        this.initialize();
        /* 1. read ratings from file
         *  2. create R RealMatrix
         *  3. open connection for workers
         *  4. train and create R
         *  5. open connection for clients
         *  6. make suggestions using POI scores
         */
        this.prepare();
        this.waitForWorkers();
        this.executeMaster();
    }

    public void executeMaster() {
        this.loadFactorDistribute();
        if (fileChooser==1) {
            apoi = this.readPoiCatalogue();
        } else if (fileChooser==2) {
            apoi = this.readPoiCatalogue1();
        }
        try {
            while (true) {
                /* Read the necessary information the master requires from the workers */
                try {
                    Socket connection = androidSocket.accept();
                    Thread t = new ActionsForClients(connection);
                    System.out.println("Android device successfully connected!");
                    t.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) { //CTRL + D to force stop
            e.printStackTrace();
            System.err.println("Server ending connection!");
        } finally {
            try {
                providerSocket.close();
                androidSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadFactorDistribute() {
        WLF2 = this.costFunction();
        worker_connections.sort(new DefaultComparator());
        Collections.reverse(worker_connections);
        for(int i = 0; (i < 5 && this.calculateError() > 10e-10); i++) {
            System.out.println("Master starting training round #" + (i+1) + ".");
            this.distributor((byte)0);
            this.distributor((byte) 1);
            System.err.println("Training error in epoch " + (i+1) + " = " + this.calculateError());
        }
    }

    private synchronized void distributor(byte mode) {
        if(mode==0) {
            System.out.println("Distributing Y");
            for(int j = 0; j < worker_connections.size(); j++){
                try {
                    ActionsForWorkers AFW = new ActionsForWorkers(new Socket(worker_connections.get(j).worker_IP, worker_connections.get(j).worker_port), new VectorPacket(null, Y, -2), -1, j);
                    AFW.broadcast();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            distributeXMatrixToWorkers();
        }
        else {
            System.out.println("Distributing X");
            for(int j = 0; j < worker_connections.size(); j++){
                try {
                    ActionsForWorkers AFW = new ActionsForWorkers(new Socket(worker_connections.get(j).worker_IP, worker_connections.get(j).worker_port), new VectorPacket(null, X, -2), -1, j);
                    AFW.broadcast();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            distributeYMatrixToWorkers();
        }
    }

    private void waitForWorkers() {
        try {
            for(int i = 0; i < num_of_workers; i++){

                /* Define the socket that is used to accept the connection */
                Socket s = providerSocket.accept();

                /* Handle the request */
                worker_connections.add(i, new WorkerConnection(s));
                System.out.println("Worker #" + (i+1) + " successfully connected!");
                worker_connections.get(i).connect();
                worker_connections.get(i).out().writeObject(new VectorPacket(C, null,-1));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Server ending connection!");
        }
    }
    ArrayList<Poi> readPoiCatalogue1(){
        JSONParser parser = new JSONParser();
        apoi = new ArrayList<Poi>();
        try{
            Object obj =parser.parse(new FileReader("src/POIs.json"));
            JSONObject jsonObject=(JSONObject) obj;
            int valueSize =jsonObject.values().size();
            System.out.println("Master got  "+ valueSize+" available POIs!");
            for(int i=0;i<valueSize;i++){
                JSONObject jsonObject2=(JSONObject)jsonObject.get(String.valueOf(i));
                Poi p = new Poi();
                p.setID((String)jsonObject2.get("POI"));
                p.setLatitude((Double)jsonObject2.get("latidude"));
                p.setLongitude((Double)jsonObject2.get("longitude"));
                p.setImage((String)jsonObject2.get("photos"));
                p.setCategory((String)jsonObject2.get("POI_category_id"));
                p.setName((String)jsonObject2.get("POI_name"));
                apoi.add(p);
            }
            return apoi;
        }catch (Exception e) {
                e.printStackTrace();
        }
        return null;
    }
    ArrayList<Poi> readPoiCatalogue() {
        String csvFile = "src/PoisLocations.csv";
        String line = "";
        String cvsSplitBy = ", ";

        apoi = new ArrayList<Poi>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

            int i = 0;

            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] assignments = line.split(cvsSplitBy);
                Poi p = new Poi();
                p.setLatitude(Double.parseDouble(assignments[0]));
                p.setLongitude(Double.parseDouble(assignments[1]));
               // p.setID(i++);
                apoi.add(p);

            }

            return apoi;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    RealMatrix readSparseMatrix() {
        //Dimioyrgia Sparse pinaka. O pinakas autos mporei na xrisimopoithei gia pinakes me arketa midinika.
        //Den ta apothikeuei se antithesi me tous parakatw.
        OpenMapRealMatrix sparse_m = new OpenMapRealMatrix(764+1,1963+1);
        String csvFile = "src/input_matrix_no_zeros.csv";
        String line = "";
        String cvsSplitBy = ", ";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] assignments = line.split(cvsSplitBy);
                sparse_m.setEntry(Integer.parseInt(assignments[0]), Integer.parseInt(assignments[1]), Double.parseDouble(assignments[2]));

            }

            return sparse_m.getSubMatrix(0, 764, 0, 1963);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    RealMatrix calculateCMatrix(){

        C=C.scalarMultiply(alpha);
        C=C.scalarAdd(1);
        return C;

    }
    RealMatrix calculatePMatrix(){

        for (int i=0; i<P.getRowDimension();i++){
            for (int j=0; j<P.getColumnDimension();j++){
                P.setEntry(i,j, R.getEntry(i,j)!=0?1:0);
            }
        }
        return P;

    }

    /*
     * ArrayList for the amount of the work that each worker can take
     * */
    private ArrayList<Double> costFunction() {
        double total = 0;
        WLF2 = new ArrayList<Double>();
        for(WorkerConnection WC: worker_connections) {
            total += WC.getStats().getFreeMemory()/WC.getStats().getCPU();
        }
        for(WorkerConnection WC: worker_connections) {
            double assessment = (WC.getStats().getFreeMemory()/WC.getStats().getCPU())/total;
            WC.setAssessment(assessment);
            WLF2.add(assessment);
        }
        return WLF2;
    }

    void distributeXMatrixToWorkers() {
        ArrayList<ActionsForWorkers> AFWL = new ArrayList<ActionsForWorkers>();
        ArrayList<Double> WLF3 = new ArrayList<Double>(WLF2);
        int total = 0, difference = 0;
        for(int i = 0; i < WLF3.size(); i++) {
            WLF3.set(i, WLF3.get(i)*(X.getRowDimension()-1));
            total += (WLF3.get(i).intValue());
            WLF3.set(i, (double)WLF3.get(i).intValue()); // round down
        }
        difference = X.getRowDimension() - 1 - total;
        for(int i = 0; (i < WLF3.size() && difference > 0); i++) {
            if(difference > 0) {
                WLF3.set(i, (double)WLF3.get(i).intValue() + 1); //round up
                difference--;
            }
        }
        for(int i = 0; i < WLF3.size(); i++) {
            System.out.println("Worker #" + (i+1) + " was assigned # of lines = " + WLF3.get(i));
        }
        int j = 0;
        for(int i = 0; i < X.getRowDimension()/*-1*/; i++){
            try {
                int rep = 0;
                while(WLF3.get(j) <= 0) {
                    j++;
                    j %= WLF3.size();
                    if(rep >= WLF3.size()) break;
                        else rep++;
                }
                ActionsForWorkers AFW = new ActionsForWorkers(new Socket(worker_connections.get(j % num_of_workers).worker_IP, worker_connections.get(j % num_of_workers).worker_port), new VectorPacket(i, 0), 0, j);
                AFWL.add(AFW);
                AFW.start();
                WLF3.set(j, WLF3.get(j)-1);
                j++;
                j %= WLF3.size();
            } catch (IOException e) {
                System.err.println("Worker #" + j + " disconnected. Starting rebalancing...");
                worker_connections.remove(j);
                num_of_workers-=1;
                this.executeMaster();
            }
        }
        try {
            for (ActionsForWorkers AFW : AFWL) {
                AFW.join();
            }
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    void distributeYMatrixToWorkers() {
        ArrayList<ActionsForWorkers> AFWL = new ArrayList<ActionsForWorkers>();
        ArrayList<Double> WLF3 = new ArrayList<Double>(WLF2);
        int total = 0, difference = 0;
        for(int i = 0; i < WLF3.size(); i++) {
            WLF3.set(i, WLF3.get(i)*(Y.getRowDimension()-1));
            total += (WLF3.get(i).intValue());
            WLF3.set(i, (double)WLF3.get(i).intValue()); // round down
        }
        difference = Y.getRowDimension() - 1 - total;
        for(int i = 0; (i < WLF3.size() && difference > 0); i++) {
            if(difference > 0) {
                WLF3.set(i, (double)WLF3.get(i).intValue() + 1); //round up
                difference--;
            }
        }
        for(int i = 0; i < WLF3.size(); i++) {
            System.out.println("Worker #" + (i+1) + " was assigned # of lines = " + WLF3.get(i));
        }
        int j = 0;
        for(int i = 0; i < Y.getRowDimension()/*-1*/; i++){
            try {
                int rep = 0;
                while(WLF3.get(j) <= 0) {
                    j++;
                    j %= WLF3.size();
                    if(rep >= WLF3.size()) break;
                    else rep++;
                }
                ActionsForWorkers AFW = new ActionsForWorkers(new Socket(worker_connections.get(j % num_of_workers).worker_IP, worker_connections.get(j % num_of_workers).worker_port),new VectorPacket(i, 1),1, j);
                AFWL.add(AFW);
                AFW.start();
                WLF3.set(j, WLF3.get(j)-1);
                j++;
                j %= WLF3.size();
            } catch (IOException e) {
                System.err.println("Worker #" + j + " disconnected. Starting rebalancing...");
                worker_connections.remove(j);
                num_of_workers-=1;
                this.executeMaster();
            }
        }
        try {
            for (ActionsForWorkers AFW : AFWL) {
                AFW.join();
            }
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*
     * error=Sum Cui(Pui-xu*yi)^2+λ*(Sum ||xu||^2+Sum||yi||^2)
     * ||x||=vector norm (eukleidia norma dianysmatos, etoimi synartisi)
     */
    double calculateError(){
        RealMatrix E=P.subtract(X.multiply(Y.transpose()));
        for (int i=0; i< E.getRowDimension(); i++){
            for(int j=0; j<E.getColumnDimension(); j++){
                E.setEntry(i,j, E.getEntry(i,j)*Math.pow(C.getEntry(i,j),0.5));
            }
        }
        return Math.pow(E.getFrobeniusNorm(),2)+lambda*(Math.pow(X.getFrobeniusNorm(),2)+Math.pow(Y.getFrobeniusNorm(),2));
    }

}