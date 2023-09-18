package ControlPlane;

/**
 * Unit test to check if the control plane is accurate andif the ring is load balanced:
 * 1. after adding one worker, if the load is redistributed
 * 2. when a worker is removed, if routing table resumes to previous one (it should)
 * 3. if the load is evenly balanced between workers.
 * */



import utils.NetworkAddress;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class test {

    public static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

    private ControlPlane cp;
    private Random random;
    private ArrayList<NetworkAddress> init_list;
    private HashMap<NetworkAddress,ArrayList<String>> map;
    private HashMap<NetworkAddress, Integer> count;
    private ArrayList<NetworkAddress> workerlist;

    private ArrayList<String> list_word;
    public test() throws IOException {
        NetworkAddress add1 = new NetworkAddress("Worker1", 1);
        ArrayList<String> list1 = new ArrayList<>();
        NetworkAddress add2 = new NetworkAddress("Worker2", 2);
        ArrayList<String> list2 = new ArrayList<>();
        init_list = new ArrayList<>();
        init_list.add(add1);
        init_list.add(add2);
        workerlist = new ArrayList<>();
        workerlist.add(add1);
        workerlist.add(add2);
        cp = ControlPlane.getControlPlane(init_list);
        random = new Random();
        map = new HashMap<>();
        count = new HashMap<>();
        map.put(add1, list1);
        map.put(add2, list2);
        count.put(add1, 0);
        count.put(add2,0);

        list_word = new ArrayList<>();
        for(int i = 0; i < 2000; i++){
            list_word.add(generateRandomString(random.nextInt(15)));
        }
    }

    /**
     * Add one worker to CP
     * */
    public void addWorker(){
        NetworkAddress add = new NetworkAddress("Worker" + (workerlist.size()+1), random.nextInt(10));
        ArrayList<String> list = new ArrayList<>();
        cp.addWorker(add);
        map.put(add,list);
        count.put(add, 0);
        workerlist.add(add);
    }


    /**
     * remove one worker from CP, removed the most recently added one by default
     * */
    public void removeWorker(){
        NetworkAddress last = workerlist.get(workerlist.size()-1);

        cp.removeWorker(last);
        map.remove(last);
        count.remove(last);
        workerlist.remove(last);
    }
    /**
     * remove one specific worker
     * */
    public void removeWorker(NetworkAddress worker){
        cp.removeWorker(worker);
        map.remove(worker);
        count.remove(worker);
        workerlist.remove(worker);
    }

    /**
     * print the sorted ring structure
     */
    public void printRing(){
        cp.printRing();
    }


    /**
     * Get the list of workers
     *
     */

    public ArrayList<NetworkAddress> getWorkerList(){
        return workerlist;
    }

    /**
     * Show the list of worker and the number of words each is responsible for
     *
     *
    * */

    public void loadWord(){
        for(String value:list_word){
            NetworkAddress place = this.cp.findState(value);
            map.get(place).add(value);
            Integer current = count.get(place) + 1;
            count.put(place, current);
        }
    }
    public void showWordCount(){

        System.out.println("-------------------");
        String workers = "[";
        String counts = "[";

        for(NetworkAddress worker: count.keySet()){

            workers += "'"+ worker.toString() + "', ";
            counts += count.get(worker) + ", ";

            count.put(worker,0);
        }
        workers += "]";
        counts += "]";
        System.out.println(workers);
        System.out.println(counts);

    }


    public void showWordList(){
        for (NetworkAddress worker : workerlist) {
            System.out.println(worker);
            System.out.println(map.get(worker));
            map.get(worker).clear();
        }

    }

    public void getRoutingTable(){
        cp.getRoutingTable();
    }
    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    public static void main(String[] args) throws IOException {
        test testrun = new test();
//        testrun.printRing();
        testrun.loadWord();
        testrun.getRoutingTable();
        testrun.showWordCount();
//        testrun.showWordList();
        for(int i = 0; i < 10; i++){
            testrun.addWorker();
            if(i == 1 || i == 6){
                testrun.loadWord();
                testrun.showWordCount();
//                testrun.printRing();
//                testrun.getRoutingTable();
//                testrun.showWordList();
            }
        }
//        testrun.showWorker();
        for(int i = 0; i < 10; i++){

            if(i == 2 || i == 5){
                testrun.loadWord();
                testrun.showWordCount();
            }
//            testrun.removeWorker();

        }
//        testrun.showWorker();

//        testrun.printRing();

    }
}
