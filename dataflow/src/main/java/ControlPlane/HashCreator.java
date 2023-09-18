package ControlPlane;

import utils.NetworkAddress;
import workers.Worker;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.UUID;

/*
 * hash creator for each worker, each worker get 5 points on the ring
 *
 * */
public class HashCreator {
    final String[] hashFunctions = {"SHA-256", "SHA-384", "SHA-512", "MD5", "SHA3-256", "SHA3-512"};

    /**
     * genPoints: generate the points of each worker on the ring
     * */
    public ArrayList<WorkerPoint> genPoints(NetworkAddress address){

        ArrayList<WorkerPoint> results = new ArrayList<>(hashFunctions.length);
        try {

            for (String hash : hashFunctions) {
                int point = genOneHash(address.toString(), hash);
                results.add(new WorkerPoint(point, address));
            }
        }catch(NoSuchAlgorithmException e){
            System.out.println(e.toString());

        }
        return results;
    }

    /**
     * genOneHash: generate each point for the workers, private  for the genPoints to use
     */

    private int genOneHash(String name, String hash) throws NoSuchAlgorithmException {
        int a = 0;
        MessageDigest md;

        try {
            md = MessageDigest.getInstance(hash);
        }catch(NoSuchAlgorithmException e){
            return -1;
        }
        byte[] result = md.digest(name.getBytes(StandardCharsets.UTF_8));
        for(int i = result.length-1; i > result.length - 5; i --){
            a = (a<<8) + (result[i] & 0xFF);
            if(i == result.length-1){
                a &= 0x7F; //making the first bit to be 0 to get positive numbers
            }
        }
        return a;
    }

    public int hashWord(String word){

        try {
            return genOneHash(word,"SHA-256");
        }catch(NoSuchAlgorithmException e){
            return -1;
        }
    }


    public static void main(String[] args){
        HashCreator hc = new HashCreator();
        String worker1 = "112.234.345.345";
//        ArrayList<Integer> result = hc.genPoints(,worker1);
//        System.out.println(result.toString());
    }

}
