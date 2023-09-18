package ControlPlane;

import utils.NetworkAddress;
import workers.Address;

public interface GetStateCallback {
    NetworkAddress perform(String word);
}
