package ControlPlane;


import utils.NetworkAddress;
import utils.PortManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class ControlPlane {

    private HashRing ring;


    private static ControlPlane cp = null;
    private ControlPlaneServer cpServer;

    public static synchronized ControlPlane getControlPlane(ArrayList<NetworkAddress> init_list) throws IOException {
        if(cp == null){
            cp = new ControlPlane(init_list);
        }
        return cp;
    }
    private ControlPlane(ArrayList<NetworkAddress> init_list) throws IOException {
        ring = new HashRing();
        for(NetworkAddress add : init_list){
            ring.addWorker(add);
        }
        RemoveWorkerCallback removeWorkerCallback = (NetworkAddress networkAddress) ->
        {
            this.removeWorker(networkAddress);
        };
        AddWorkerCallback addWorkerCallback = (NetworkAddress worker) ->
        {
            this.addWorker(worker);
        };
        GetStateCallback getStateCallback = (String word) ->
        {
            return this.findState(word);
        };
        RoutingTableCallback routingTableCallback = () ->
        {
            return getRoutingTable();
        };
        this.cpServer = new ControlPlaneServer(new NetworkAddress("LOCALHOST", PortManager.getFreePort()), removeWorkerCallback
        , addWorkerCallback, routingTableCallback, getStateCallback);
        cpServer.startServer();
    }

    /**
    *
    * For each incoming word, find the worker that's responsible for the word
     * (not neccessarily where the state is stored based on configuration)
    * */
    public NetworkAddress findState(String word){
        return ring.findState(word);
    }

    public void addWorker(NetworkAddress nworker){
        ring.addWorker(nworker);
    }


    public void removeWorker(NetworkAddress oworker){
        ring.removeWorker(oworker);
    }


    public HashMap<NetworkAddress, ArrayList<int[]>> getRoutingTable(){return ring.RoutingTable();}

    public void printRing(){ring.printRing();}
}
