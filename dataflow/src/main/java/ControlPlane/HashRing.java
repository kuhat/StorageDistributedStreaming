package ControlPlane;

import utils.NetworkAddress;


import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class HashRing {
    private HashMap<NetworkAddress, ArrayList<WorkerPoint>> workermap;
    private ArrayList<WorkerPoint> ring;
    private HashCreator hashCreator;
    public HashRing(){
        workermap = new HashMap<>();
        ring = new ArrayList<>();
        hashCreator = new HashCreator();
    }

    public void addWorker(NetworkAddress address){
        ArrayList<WorkerPoint> list = hashCreator.genPoints(address);
        workermap.put(address, list);
        ring.addAll(list);
        ring.sort(new SortByPoint());
    }

    //todo: remove worker

    public void removeWorker(NetworkAddress address){
        workermap.remove(address);

        ring.removeIf(w -> w.getWorker().equals(address));
    }

    /**
     * find the worker responsible for the word
     */

    public NetworkAddress findState(String word){

        int hash = hashCreator.hashWord(word);
//        System.out.println("Word: "+ word+" -----Hash Value: " + hash);
        int len = ring.size();
        for(int i = 0; i < len; i ++){
            // hash values that >= to the point belongs to this worker
            if(ring.get(i).getPoint() <= hash){
                return ring.get(i).getWorker();
            }
        }
        return ring.get(len-1).getWorker();
    }

    /**
    * print the workermap, not sorted
    * */
    @Override
    public String toString() {
        String result = "";
        for(NetworkAddress address: workermap.keySet()){
            int i = 1;
            for(WorkerPoint a:workermap.get(address)){
                result += address.toString() +", Point " + i + ": "+ a.getPoint() + "\n";
                i++;
            }
        }
        return result;
    }

    /**
    * print the sorted ring structure
    * */
    public void printRing(){
        for(WorkerPoint p : ring){
            System.out.println(p);
        }

//        for(WorkerPoint p : ring){
//            System.out.print(p.getPoint() + " ");
//        }

    }


    public HashMap<NetworkAddress, ArrayList<int[]>> RoutingTable(){
        HashMap<NetworkAddress, ArrayList<int[]>> structure = new HashMap<>();
        for(int i = 0; i < ring.size(); i++){
            int[] range = new int[2];
            int[] range2 = new int[2];
            if(i == 0){
                range[0] = ring.get(i).getPoint();
                range[1] = ((int)Math.pow(2,32));
                range2[1] = ring.get(ring.size()-1).getPoint();

            }else{
                range[0] = (ring.get(i).getPoint());
                range[1] = (ring.get(i-1).getPoint());
            }

            NetworkAddress add = ring.get(i).getWorker();
            if(!structure.containsKey(add)){
                ArrayList<int[]> ranges = new ArrayList<>();
                ranges.add(range);
                structure.put(add,ranges);
                if(i == 0){
                    structure.get(add).add(range2);
                }
            }else{
                structure.get(add).add(range);
            }

        }

        for(NetworkAddress w:structure.keySet()){
            System.out.println(w);
            System.out.print("Range: ");
            for(int[] a : structure.get(w)){
                System.out.print(Arrays.toString(a) + ", ");
            }
            System.out.println();
        }

        return structure;
    }

}
