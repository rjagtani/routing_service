package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;


public class Server {

    private Graph graph;

    public void load() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(1111)) {
            String file = "src/main/java/org/example/schleswig-holstein.json";

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            MapJson jsonMap = mapper.readValue(new FileInputStream(file), MapJson.class);

            graph = new Graph(jsonMap);


            while (true) {

                Socket clientSocket = serverSocket.accept();
                Thread thRoute = findRoute(clientSocket);
                thRoute.start();

            }
        }
    }

    public Thread findRoute(Socket socket){
        Thread t = new Thread() {
            public void run() {
                InputStream is = null;
                try {
                    is = socket.getInputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                OutputStream os = null;
                try {
                    os = socket.getOutputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ObjectInputStream ois = null;
                try {
                    ois = new ObjectInputStream(is);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ObjectOutputStream oos = null;
                try {
                    oos = new ObjectOutputStream(os);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                double[] coordinates = new double[0];
                try {
                    coordinates = (double[])ois.readObject();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                double originLat = coordinates[0];
                double originLon = coordinates[1];
                double destinationLat = coordinates[2];
                double destinationLon = coordinates[3];
                double algorithm = coordinates[4];

                ArrayList<Node> route;

                if (algorithm == 0) {
                    route = graph.anyLocationDijkstra(originLat, originLon, destinationLat, destinationLon);

                } else {
                    route = graph.anyLocationAStar(originLat, originLon, destinationLat, destinationLon);
                }
                String routeGeoJson = null;
                try {
                    routeGeoJson = MapJson.routeToJson(route);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                try {
                    oos.writeObject(routeGeoJson);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        };
        return t;
    }
}
