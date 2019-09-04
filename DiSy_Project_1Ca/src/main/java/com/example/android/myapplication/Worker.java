package com.example.android.myapplication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import org.apache.commons.math3.linear.RealMatrix;

public class Worker extends Thread{

    String server_IP = null, local_IP = null;
    StatsPacket stats = null;
    int server_port;
    public int port_for_datagrams;
    ServerSocket datagramSocket = null;
    Socket requestSocket = null;
    ObjectOutputStream out=null;
    ObjectInputStream in = null, in2 = null;
    VectorPacket result;
    protected static RealMatrix C, XY;
    int line, x;
    static int row;

    public Worker(String ServIP, String CurrIP, int connPort, int dataPort) {
        server_IP = ServIP;
        local_IP = CurrIP;
        server_port = connPort;
        port_for_datagrams = dataPort;
        stats = new StatsPacket();
    }

    public void initialize() {
        try {
            requestSocket = new Socket(server_IP,server_port);
            datagramSocket = new ServerSocket(port_for_datagrams);
            /* Create the streams to send and receive data from server */
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            in = new ObjectInputStream(requestSocket.getInputStream());
        }catch(UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            in.close();
            out.close();
            requestSocket.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void run() {
        this.initialize();
        this.sendConfiguration();
        this.sendStatistics();
        this.getCP();
        this.disconnect();
        try {
            int i = 0;
            while (true) {
                requestSocket = datagramSocket.accept();
                out = new ObjectOutputStream(requestSocket.getOutputStream());
                in = new ObjectInputStream(requestSocket.getInputStream());
                System.out.println("Conn#" + (i++) + ": Master successfully connected!");
                try {
                    VectorPacket packet = (VectorPacket) in.readObject();
                    int mode = packet.getMode();
                    if(mode < -1 ) {
                        XY = packet.getMatrix();
                    } else if(mode > -1) {
                        if (mode == 0) {
                            this.row = packet.getNumberOfRow();
                            packet.setVector(C.getRowMatrix(row));
                            packet.setMatrix(XY);
                        } else if (mode == 1) {
                            this.row = packet.getNumberOfRow();
                            packet.setVector(C.getColumnMatrix(row).transpose());
                            packet.setMatrix(XY);
                        }
                        ActionsForMaster t = new ActionsForMaster(requestSocket, packet, in, out);
                        t.start();
                    } else {
                        this.disconnect();
                        continue;
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    break;
                } catch(IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Worker ending connection!");
        } finally {
            this.disconnect();
        }
    }

    public void sendStatistics() {
        try {
            out.writeObject(stats);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendConfiguration() {
        try {
            Vec2<String, Integer> config = new Vec2<String, Integer>(local_IP, port_for_datagrams);
            out.writeObject(config);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getCP() {
        try {
            VectorPacket packet = (VectorPacket)in.readObject();
            C = packet.getVector();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        //new com.example.android.myapplication.Worker("localhost", "localhost",4201, 4205).start();
        //new Worker("192.168.0.105", "192.168.0.105",4201, 4207).start();
        new Worker("192.168.0.105", "192.168.0.105",4201, 4208).start();
    }
}
