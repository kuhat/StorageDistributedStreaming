package ControlPlane;

import java.util.Comparator;

public class SortByPoint implements Comparator<WorkerPoint> {
    @Override
    public int compare(WorkerPoint o1, WorkerPoint o2) {
        return o2.getPoint() - o1.getPoint();
    }
}
