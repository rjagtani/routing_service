package org.example;

import java.util.ArrayList;

public class Node {
    double lat;
    double lon;
    ArrayList<Node> outnodes;
    Node (double latitude, double longitude, ArrayList<Node> outnodesList){
        lat = latitude;
        lon = longitude;
        outnodes = outnodesList;
    }

    public  String toString(){
        return "["+Double.toString(lat)+","+Double.toString(lon)+"]";
    }
}
