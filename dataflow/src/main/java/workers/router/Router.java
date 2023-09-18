package workers.router;

import backends.DAG.NodeInfo;
import com.google.common.primitives.Chars;
import utils.NetworkAddress;
import utils.ScaleDirection;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

// temp router placeholder with hardcoded load balancing routing table...
public class Router {
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private List<UUID> clients;
    private HashMap<UUID, Set<Character>> clientRoutingTable;
    private HashMap<UUID, Set<Character>> previousRoutingTable;
    private List<List<Set<Character>>> plans;

    // hardcoded routing table to handle 1,2,3 workers
    // TODO: 1 how to campactly store the keys,
    // 1. easy tp query
    // 2. easy to update, when add a new worker.
    private char[][][] ROUTE_PLANS =
            {
                    {
                            {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k','l','m',
                                    'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'}
                    },
                    {
                            {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k','l'},
                            {'m', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'}
                    },
                    {
                            {'a', 'b', 'c', 'd', 'e', 'f', 'g'},
                            {'p', 'q', 'r','s', 't', 'u', 'v', 'w', 'x', 'y', 'z'},
                            {'h', 'i', 'j', 'k','l','m', 'n', 'o', }
                    }
            };

    public Router()
    {
        this.clients = new ArrayList<>();
        this.plans = new ArrayList<>();
        this.clientRoutingTable = new HashMap<>();
        for(char[][] plan : ROUTE_PLANS)
        {
            List<Set<Character>> table = new ArrayList<>();
            this.plans.add(table);
            for(char[] partitionKeys : plan)
            {
                Set<Character> partition = new HashSet<>();
                partition.addAll(Chars.asList(partitionKeys));
                table.add(partition);
            }
        }
    }

    /**
     * get called when a new worker added to DAE
     * add new worker uuid to the routering map
     * @param clientID
     */
    public void addNode(UUID clientID)
    {
        // add to client set
        this.clients.add(clientID);

        // backup current routing table as previouseClientRoutingTable
        this.previousRoutingTable = new HashMap<>();
        for(UUID client : clientRoutingTable.keySet())
        {
            HashSet<Character> stateSet = new HashSet<>();
            for(Character stateKey : clientRoutingTable.get(client))
            {
                stateSet.add(stateKey);
            }
            this.previousRoutingTable.put(client, stateSet);
        }

        // update routing table;
        List<Set<Character>> plan = plans.get(this.clients.size() - 1);
        for(UUID client : clients)
        {
            // place empty hashset for each client
            clientRoutingTable.put(client, new HashSet<>());
            clientRoutingTable.get(client).addAll(plan.get(clients.indexOf(client)));
        }
        logger.log(Level.INFO, "Router backed up routing table: " + previousRoutingTable);
        logger.log(Level.INFO, "Router current routing table: " + clientRoutingTable);
        logger.log(Level.INFO, "Router add node " + clientID + " with key set: " + this.clientRoutingTable.get(clientID));
    }

    public void removeNode(UUID clientID)
    {
        // remove client from client set
        this.clients.remove(clientID);

        // backup current routing table as previouseClientRoutingTable
        this.previousRoutingTable = new HashMap<>();
        for(UUID client : clientRoutingTable.keySet())
        {
            this.previousRoutingTable.put(client, new HashSet<>(clientRoutingTable.get(client)));
        }

        // update routing table;
        List<Set<Character>> plan = plans.get(this.clients.size() - 1);
        for(UUID client : clients)
        {
            // place empty hashset for each client
            clientRoutingTable.put(client, new HashSet<>());
            clientRoutingTable.get(client).addAll(plan.get(clients.indexOf(client)));
        }
        logger.log(Level.INFO, "Router removed node " + clientID + " with key set: " + this.clientRoutingTable.get(clientID));
    }

    /**
     * route the data based on its key
     * returns the worker uuid that the data should be routed to
     *
     * @param data
     * @return
     */
    public UUID routeByKey(String data)
    {
//        logger.log(Level.INFO, "Router data " + data + " with plan#" + this.clients.size());
//        logger.log(Level.INFO, "Router data clients: " + clients);

        UUID res = null;
        if(data.length() == 0)
        {
            return res;
        }
//        List<Set<Character>> plan = plans.get(this.clients.size() - 1);
////        logger.log(Level.INFO, "Router data plan: " + plan);
//        for(int partition = 0; partition < plan.size(); partition++)
//        {
//            if(plan.get(partition).contains(Character.toLowerCase(data.charAt(0))))
//            {
//                if(partition >= 0 && partition < clients.size())
//                {
//                    res = clients.get(partition);
////                    logger.log(Level.INFO, "Router data" + data + " with " + this.clients.size());
//                }
//                break;
//            }
//        }
        for(Map.Entry<UUID, Set<Character>> entry : clientRoutingTable.entrySet())
        {
            if(entry.getValue().contains(Character.toLowerCase(data.charAt(0))))
            {
                res = entry.getKey();
//                if(this.clientRoutingTable.size() > 1)
//                {
//                    logger.log(Level.INFO, "Route [" + data + "] to " + entry);
//                }
                break;
            }
        }

        return res;
    }

    public Map<UUID, Set<Character>> getCurrentRoutingPlan()
    {
        return this.clientRoutingTable;
    }

    public Set<Character> getKeyDiff(UUID nodeID, int shift)
    {
        Set<Character> currentPlan = clientRoutingTable.get(nodeID);
        logger.log(Level.INFO, "get key diff for node id " + nodeID + ", which is manging key range : " + currentPlan);
        Set<Character> previousePlan = new HashSet<>();
        logger.log(Level.INFO, "Previously routing table : " + previousRoutingTable);
        if(previousRoutingTable.containsKey(nodeID))
        {
            previousePlan.addAll(previousRoutingTable.get(nodeID));
        }
        logger.log(Level.INFO, "get key diff for node id " + nodeID + ", which previously manging charging key range : " + previousePlan);


        Set<Character> bigger, smaller;
        if(currentPlan.size() > previousePlan.size())
        {
            bigger = currentPlan;
            smaller = previousePlan;
        }
        else {
            bigger = previousePlan;
            smaller = currentPlan;
        }

        for(Character c : smaller)
        {
            bigger.remove(c);
        }
        return bigger;
    }

    public Map<Integer, Set<Character>> getPlanDiff(int planA, int planB)
    {
        Map<Integer,  Set<Character>> res = new HashMap<>();
        char arr0[] = {'j', 'k','l','m'};
        char arr1[] = {'n', 'o', 'p', 'q'};
        if(planA == 2 && planB == 3)
        {
            res.put(0, new HashSet<Character>(Chars.asList(arr0)));
            res.put(1, new HashSet<Character>(Chars.asList(arr1)));
        }
        if(planA == 3 && planB == 2)
        {
            res.put(0, new HashSet<Character>(Chars.asList(arr0)));
            res.put(1, new HashSet<Character>(Chars.asList(arr1)));
        }
        return res;
    }

    public List<UUID> getClients() {
        return clients;
    }
}
