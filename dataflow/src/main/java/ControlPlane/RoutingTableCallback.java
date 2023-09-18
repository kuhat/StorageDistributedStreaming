package ControlPlane;

import utils.NetworkAddress;
import workers.router.RoutingTable;

import java.util.ArrayList;
import java.util.HashMap;

public interface RoutingTableCallback {
    HashMap<NetworkAddress, ArrayList<int[]>> perform();
}
