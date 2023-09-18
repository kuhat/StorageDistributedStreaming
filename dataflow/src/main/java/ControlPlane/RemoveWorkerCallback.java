package ControlPlane;

import utils.NetworkAddress;

public interface RemoveWorkerCallback {
    void perform(NetworkAddress worker);
}
