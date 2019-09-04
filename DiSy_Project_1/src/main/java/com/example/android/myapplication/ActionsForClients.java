package com.example.android.myapplication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Collections;

public class ActionsForClients extends Thread {
    Socket connection = null;
    ObjectInputStream in;
    ObjectOutputStream out;
    int user_ID,poisNum;

    public ActionsForClients(Socket s) {
        try {
            connection = s;
            in = new ObjectInputStream(connection.getInputStream());
            out = new ObjectOutputStream(connection.getOutputStream());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        /* Read user coordinates
         */
        Vec2<Double, Double> coord;
        try {
            user_ID = in.readInt();
            coord = (Vec2<Double, Double>)in.readObject();
            poisNum =in.readInt();
            System.out.println("User coordinates: " + coord);
			connectToDatabase();
            List<Poi> bestPois = calculateBestPoisForUser(user_ID, coord.getTValue(), coord.getYValue(), poisNum);
            this.sendQueries(bestPois);
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void sendQueries(List<Poi> result) {
        try {
            out.writeObject(result);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
	
	public synchronized void connectToDatabase() {
		//code for database connection???
	}

    List<Poi> calculateBestPoisForUser(int u, double c, double d, int j) {
        List<Poi> lp=new ArrayList<Poi>();
        ArrayList<Double> ad=new ArrayList<Double>();
        HashMap<Poi,Double> pd= new HashMap<>();
        double distance,s;
        for (int i = 0; i< Master.apoi.size()-1; i++){
            s=calculateScore(u,i);
            distance=10000*Math.sqrt(Math.pow((d- Master.apoi.get(i).getLongitude()),2)+Math.pow((c- Master.apoi.get(i).getLatitude()),2));
            if (distance>1){
                s/=distance;
            }
            pd.put(Master.apoi.get(i),s);

        }

        ad.addAll(pd.values());
        Collections.sort(ad, Collections.reverseOrder());
        pd.forEach((k,v)->{
            if (ad.subList(0,j).contains(v)){
                lp.add(k);
            }
        });
        return lp;
    }

        /*
         * Score=rating poi i gia ton user u
         */
        double calculateScore(int u, int i){
            return Master.X.getRowVector(u).dotProduct(Master.Y.getRowVector(i));
        }
}
