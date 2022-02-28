package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.*;

import java.util.*;

public class Graph {
    ArrayList<Node> nodelist;
    HashMap<String, Node> coordMap;

    Graph(Geojson map) {

        coordMap = new HashMap<String, Node>(); //to quickly map coordinates to node
        nodelist = new ArrayList<Node>();


        double[][] coordinates = map.features[0].geometry.coordinates;
        for (double[] coord : coordinates) {
            double lat = coord[1]; //be careful, the schleswig holstein map has longitude first and latitude second
            double lon = coord[0];

            ArrayList<Node> outnodes = new ArrayList<Node>();
            Node node = new Node(lat, lon, outnodes);
            nodelist.add(node);
            String latlon = lat + "," + lon;
            coordMap.put(latlon, node);

        }

        for (int i = 1; i < map.features.length; i++) {
            Geojson featuresNew = map.features[i];
            Geojson geo = featuresNew.geometry;
            String type = geo.type;

            if (type.contentEquals("LineString")) {//add edges to outnodes
                double[][] vertices = geo.coordinates;
                //Iterate through LineString
                for (int j = 0; j < vertices.length - 1; j++) {
                    double[] vertex = vertices[j];
                    double lat = vertex[1];
                    double lon = vertex[0];

                    //check if startnode is already in Graph. If not, add it.
                    String key = lat + "," + lon;
                    if (!coordMap.containsKey(key)) {

                        ArrayList<Node> emptyOutnodes = new ArrayList<Node>();
                        Node nodeAdd = new Node(lat, lon, emptyOutnodes);
                        //add to nodelist
                        nodelist.add(nodeAdd);
                        //add coordinate-string - node  -pair to hashmap
                        coordMap.put(key, nodeAdd);

                    }
                    //check if outnode is already in the Graph. If not, add it.
                    double[] vertexNext = vertices[j + 1];
                    double latNext = vertexNext[1];
                    double lonNext = vertexNext[0];

                    String keyNext = latNext + "," + lonNext;
                    if (!coordMap.containsKey(keyNext)) {

                        ArrayList<Node> emptyOutnodesNext = new ArrayList<Node>();
                        Node nodeAddNext = new Node(latNext, lonNext, emptyOutnodesNext);
                        //add to nodelist
                        nodelist.add(nodeAddNext);
                        //add coordinate-string - node  -pair to hashmap
                        coordMap.put(keyNext, nodeAddNext);
                    }
                    //add outnode-pointer into the outnode list
                    Node startNode = coordMap.get(key);
                    Node endNode = coordMap.get(keyNext);
                    startNode.outnodes.add(endNode);
                }

            }
        }
    }
    public Node nextNode(double lat, double lon){
        Node nearestNode = nodelist.get(0);
        for (int i =1; i< nodelist.size(); i++){
            Node currentNode = nodelist.get(i);
            double currentDistance = haversineDist(lat,lon,currentNode.lat, currentNode.lon);
            if (currentDistance < haversineDist(lat, lon, nearestNode.lat,nearestNode.lon)){
                nearestNode = currentNode;
            }
        }

        return nearestNode;
    }

    public static double euclidDist(double lat1, double lon1, double lat2, double lon2) {
        return Math.sqrt(Math.pow(lat1 - lat2, 2) + Math.pow(lon1 - lon2, 2));
    }

    public static double haversineDist(double lat1, double lon1, double lat2, double lon2){
        final int radius = 6371; //earth radius
        double distLat = (lat1-lat2)*Math.PI/180;
        double distLon = (lon1-lon2)*Math.PI/180;
        double a = Math.sin(distLat/2)*Math.sin(distLat/2)+Math.cos(lat1*Math.PI/180)*Math.cos(lat2*Math.PI/180)*
                Math.sin(distLon/2)*Math.sin(distLon/2);
        double c = 2*Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = radius*c;
        return dist;
    }
    public ArrayList<Node> getPath(double originLat, double originLon, double destinationLat,
                             double destinationLon, String algorithm){

        double infinity = 9999999999.9;
        Node startNode = coordMap.get(originLat + "," + originLon);
        Node endNode = coordMap.get(destinationLat + "," + destinationLon);

        //create Hashmaps
        HashMap<Node, Double> distances = new HashMap<>();
        HashMap<Node, Node> previous = new HashMap<>();
        HashMap<Node, Boolean> finished = new HashMap();

        //initialize Hashmaps
        for (Node node : nodelist) {
            if (node == startNode) {
                distances.put(node, 0.0000000);
            } else {
                distances.put(node, infinity);

            }
            previous.put(node, null);
            finished.put(node, false);
        }

        //priority queue for discovered nodes

        Comparator<Node> nodeComparator = new Comparator<Node>() {
            @Override
            public int compare(Node n1, Node n2) {
                double addDistance1;
                double addDistance2;
                if (algorithm == "Dijkstra"){
                    addDistance1 = 0;
                    addDistance2 = 0;
                }else {
                    addDistance1 = haversineDist(n1.lat, n1.lon, destinationLat,destinationLon);
                    addDistance2 = haversineDist(n2.lat, n2.lon, destinationLat,destinationLon);
                }
                if ((distances.get(n1) +addDistance1) - (distances.get(n2)+addDistance2) > 0) {
                    return 1;
                } else if ((distances.get(n1) +addDistance1) - (distances.get(n2)+addDistance2) < 0) {
                    return -1;
                } else {
                    return 0;
                }
            }
        };

        PriorityQueue<Node> discoveredQue = new PriorityQueue<>(nodeComparator);
        discoveredQue.add(startNode);

        while (finished.get(endNode) == false) {

            Node currentNode = discoveredQue.remove();

            for (Node neighbor : currentNode.outnodes) {

                if (finished.get(neighbor) == false) {
                    double distance = haversineDist(currentNode.lat, currentNode.lon, neighbor.lat, neighbor.lon);

                    if (distances.get(currentNode) + distance < distances.get(neighbor)) {
                        //update distance, if new distance is shorter

                        distances.put(neighbor, distances.get(currentNode) + distance);
                        //put node again into queue so the updated distance will be relevant for the order
                        discoveredQue.add(neighbor);
                        //update previous

                        previous.put(neighbor, currentNode);

                    }
                    if (!discoveredQue.contains(neighbor)) {//add discovered node to que if it's not part of it yet.
                        discoveredQue.add(neighbor);

                    }
                }

            }

            finished.put(currentNode, true);
            }
            //create route list to return path.
            ArrayList<Node> routeList = new ArrayList<>();
            Node nodePath = endNode;
            routeList.add(nodePath);
            while (previous.get(nodePath) != null) {
                routeList.add(previous.get(nodePath));
                nodePath = routeList.get(routeList.size() - 1);

            }
            Collections.reverse(routeList);

            return routeList;
        }

        public ArrayList anyLocationDijkstra(double originLat, double originLon, double destinationLat,
                                             double destinationLon){
            Node start = nextNode(originLat,originLon);
            Node end = nextNode(destinationLat,destinationLon);
            ArrayList route = getPath(start.lat, start.lon, end.lat, end.lon, "Dijkstra");
            return route;
        }

        public ArrayList anyLocationAStar(double originLat, double originLon, double destinationLat,
                                             double destinationLon){
            Node start = nextNode(originLat,originLon);
            Node end = nextNode(destinationLat,destinationLon);
            ArrayList route = getPath(start.lat, start.lon, end.lat, end.lon, "AStar");
            return route;
        }



        public String toString() {
            String printString = "";
            for (Node entry : nodelist) {
                printString = printString + entry.toString();
            }
            return printString;
        }


    }






