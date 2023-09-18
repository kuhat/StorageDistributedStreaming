package ControlPlane;

import utils.NetworkAddress;

import java.util.UUID;

public class WorkerPoint {

    private int point;
    private NetworkAddress address;

    public WorkerPoint(int p, NetworkAddress addr){
        point = p;
        address = addr;
    }

    public int getPoint(){
        return point;
    }

    public NetworkAddress getWorker(){
        return address;
    }

    @Override
    public String toString() {
        return "Worker: " + address.toString()+ " Point:" + point;
    }
}
