package ControlPlane;

import io.grpc.stub.StreamObserver;
import org.apache.commons.compress.harmony.pack200.NewAttribute;
import utils.NetworkAddress;
import workers.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

public class ControlPlaneService extends ControlPlaneGrpc.ControlPlaneImplBase {
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private RemoveWorkerCallback removeWorkerCallback;
    private AddWorkerCallback addWorkerCallback;
    private RoutingTableCallback routingTableCallback;
    private GetStateCallback getStateCallback;
    public ControlPlaneService(RemoveWorkerCallback rw, AddWorkerCallback aw, RoutingTableCallback rt, GetStateCallback s){
        removeWorkerCallback = rw;
        addWorkerCallback = aw;
        routingTableCallback = rt;
        getStateCallback = s;
    }

    /**
     *   rpc AddWorker(WorkerInformation) returns (RingUpdate);
     *   rpc RemoveWorker(WorkerInformation) returns (RingUpdate);
     *   rpc GetRoutingTable(RingUpdate) returns (RoutingTable);
     *   rpc GetState(StateRequest) returns (Address);
     */
    @Override
    public void addWorker(WorkerInformation info, StreamObserver<RingUpdate> observer){
        String domain = info.getAddress().getDomain();
        int port =  info.getAddress().getPort();
        NetworkAddress newWorker = new NetworkAddress(domain, port);
        System.out.println("received ADD worker request to add worker: " + domain + ":" +port);
        addWorkerCallback.perform(newWorker);
        observer.onNext(RingUpdate.newBuilder().setConfirm(true).build());
        observer.onCompleted();
    }

    @Override
    public void removeWorker(WorkerInformation info, StreamObserver<RingUpdate> observer){
        String domain = info.getAddress().getDomain();
        int port =  info.getAddress().getPort();
        NetworkAddress newWorker = new NetworkAddress(domain, port);
        System.out.println("received REMOVE worker request to remove worker: " + domain + ":" +port);
        removeWorkerCallback.perform(newWorker);
        observer.onNext(RingUpdate.newBuilder().setConfirm(true).build());
        observer.onCompleted();
    }

    @Override
    public void getRoutingTable(RingUpdate info, StreamObserver<RoutingTable> observer){

        System.out.println("received GET ROUTING TABLE request");
        HashMap<NetworkAddress, ArrayList<int[]>> table = routingTableCallback.perform();
        RoutingTable.Builder rt = RoutingTable.newBuilder();
        for(NetworkAddress w:table.keySet()){
            AddressToInterval.Builder address = AddressToInterval.newBuilder();
            address.setAddress(Address.newBuilder().setDomain(w.getDomain()).setPort(w.getPort()).build());
            for(int[] range:table.get(w)){
                Interval interval = Interval.newBuilder().setInterval(range[0], range[1]).build();
                address.addIntervals(interval);
            }
            rt.addTable(address.build());
        }
        observer.onNext(rt.build());
        observer.onCompleted();
    }
    @Override
    public void getState(StateRequest word, StreamObserver<Address> observer){
        String w = word.getWord();
        System.out.println("received STATE REQUEST for word: " + w);
        NetworkAddress worker = getStateCallback.perform(w);
        observer.onNext(Address.newBuilder().setDomain(worker.getDomain()).setPort(worker.getPort()).build());
        observer.onCompleted();
    }
}
