package org.example;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;


import java.util.*;

class Node {
    double lat;
    double lon;
    ArrayList<Node> neighbors;
    Node (double lat, double lon, ArrayList<Node> neighborList){
        this.lat = lat;
        this.lon = lon;
        neighbors = neighborList;
    }
}


class MapJson {

    public String type;
    public MapJson[] features;
    public MapJson geometry;
    public double[][] coordinates;
    public HashMap<String, Integer> properties;

    public static String routeToJson(ArrayList<Node> routeList) throws JsonProcessingException {

        MapJson routeJson = new MapJson();
        routeJson.type = "FeatureCollection";
        MapJson[] featuresJson = new MapJson[1];
        featuresJson[0] = new MapJson();
        featuresJson[0].type = "Feature";
        featuresJson[0].geometry = new MapJson();
        featuresJson[0].geometry.type = "LineString";
        featuresJson[0].geometry.coordinates = new double[routeList.size()][2];
        for (int i =0; i< routeList.size(); i++){
            featuresJson[0].geometry.coordinates[i] = new double[]{routeList.get(i).lon, routeList.get(i).lat};
        }
        featuresJson[0].properties = new HashMap<>();
        routeJson.features = featuresJson;

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String routeString = mapper.writeValueAsString(routeJson);

        return routeString;
    }
}

public class Graph {
    ArrayList<Node> nodelist;
    HashMap<String, Node> latlonMap;

    Graph(MapJson map) {

        nodelist = new ArrayList<Node>();
        latlonMap = new HashMap<String, Node>();

        for (int i = 1; i < map.features.length; i++) {
            MapJson featuresNew = map.features[i];
            MapJson geo = featuresNew.geometry;
            String type = geo.type;

            if (type.contentEquals("LineString")) {
                double[][] coordinates = geo.coordinates;
                for (int j = 0; j < coordinates.length - 1; j++) {
                    double[] coordinate_pair = coordinates[j];
                    double lat = coordinate_pair[1];
                    double lon = coordinate_pair[0];

                    String key = lat + "," + lon;
                    if (!latlonMap.containsKey(key)) {
                        ArrayList<Node> initNeighbor = new ArrayList<Node>();
                        Node nodeAdd = new Node(lat, lon, initNeighbor);
                        nodelist.add(nodeAdd);
                        latlonMap.put(key, nodeAdd);
                    }

                    double[] vertexNext = coordinates[j + 1];
                    double latNext = vertexNext[1];
                    double lonNext = vertexNext[0];

                    String keyNext = latNext + "," + lonNext;
                    if (!latlonMap.containsKey(keyNext)) {

                        ArrayList<Node> initNeighborNext = new ArrayList<Node>();
                        Node nodeAddNext = new Node(latNext, lonNext, initNeighborNext);
                        nodelist.add(nodeAddNext);
                        latlonMap.put(keyNext, nodeAddNext);
                    }

                    Node startNode = latlonMap.get(key);
                    Node endNode = latlonMap.get(keyNext);
                    startNode.neighbors.add(endNode);
                }

            }
        }
    }

    public Node findNearestNode(double lat, double lon){
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
        //earth radius
        final int radius = 6371;
        double distLat = (lat1-lat2)*Math.PI/180;
        double distLon = (lon1-lon2)*Math.PI/180;
        double a = Math.sin(distLat/2)*Math.sin(distLat/2)+Math.cos(lat1*Math.PI/180)*Math.cos(lat2*Math.PI/180)*
                Math.sin(distLon/2)*Math.sin(distLon/2);
        double c = 2*Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = radius*c;
        return dist;
    }


    public ArrayList<Node> getRoute(double originLat, double originLon, double destinationLat,
                                    double destinationLon, String algorithm){

        double infinity = 9999999999.9;
        Node startNode = latlonMap.get(originLat + "," + originLon);
        Node endNode = latlonMap.get(destinationLat + "," + destinationLon);

        HashMap<Node, Double> costs = new HashMap<>();
        HashMap<Node, Boolean> processed = new HashMap();
        HashMap<Node, Node> parent = new HashMap<>();

        for (Node node : nodelist) {
            if (node == startNode) {
                costs.put(node, 0.0000000);
            } else {
                costs.put(node, infinity);

            }
            parent.put(node, null);
            processed.put(node, false);
        }

        // Comparator for Priority Queue

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
                if ((costs.get(n1) +addDistance1) - (costs.get(n2)+addDistance2) > 0) {
                    return 1;
                } else if ((costs.get(n1) +addDistance1) - (costs.get(n2)+addDistance2) < 0) {
                    return -1;
                } else {
                    return 0;
                }
            }
        };

        PriorityQueue<Node> graphPriorityQueue = new PriorityQueue<>(nodeComparator);
        graphPriorityQueue.add(startNode);

        while (processed.get(endNode) == false) {

            Node currentNode = graphPriorityQueue.remove();
            if (processed.get(currentNode) == false) {
                for (Node neighbor : currentNode.neighbors) {
                    if (processed.get(neighbor) == false) {
                        double cost = haversineDist(currentNode.lat, currentNode.lon, neighbor.lat, neighbor.lon);
                        if (costs.get(currentNode) + cost < costs.get(neighbor)) {
                            costs.put(neighbor, costs.get(currentNode) + cost);
                            graphPriorityQueue.add(neighbor);
                            parent.put(neighbor, currentNode);
                        }

                    }
                }
                processed.put(currentNode, true);
            }
            }

            ArrayList<Node> routeList = new ArrayList<>();
            Node nodePath = endNode;
            routeList.add(nodePath);

            while (parent.get(nodePath) != null) {
                routeList.add(parent.get(nodePath));
                nodePath = routeList.get(routeList.size() - 1);

            }

            Collections.reverse(routeList);

            return routeList;
        }

        public ArrayList anyLocationDijkstra(double originLat, double originLon, double destinationLat,
                                             double destinationLon){
            Node start = findNearestNode(originLat,originLon);
            Node end = findNearestNode(destinationLat,destinationLon);
            ArrayList route = getRoute(start.lat, start.lon, end.lat, end.lon, "Dijkstra");
            return route;
        }

        public ArrayList anyLocationAStar(double originLat, double originLon, double destinationLat,
                                             double destinationLon){
            Node start = findNearestNode(originLat,originLon);
            Node end = findNearestNode(destinationLat,destinationLon);
            ArrayList route = getRoute(start.lat, start.lon, end.lat, end.lon, "AStar");
            return route;
        }

    }






