package ControlPlane;

import utils.NetworkAddress;

public interface AddWorkerCallback {

    void perform(NetworkAddress worker);
}
