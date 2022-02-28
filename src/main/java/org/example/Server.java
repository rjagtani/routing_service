package org.example;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class Server {

    private Graph graph;

    public void load() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(1234)) {
            String file = "src/main/java/org/example/schleswig-holstein.json";

            //ObjectMapper maps a json to a pojo. For our purposes the pojo is an instance of our Geojson class
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            Geojson mapJson = mapper.readValue(new FileInputStream(file), Geojson.class);
            //Create Graph object from the Map-json, to do path-searches on.
            graph = new Graph(mapJson);


            while (true) {

                Socket clientSocket = serverSocket.accept();
                Thread thRoute = startRoute(clientSocket);
                thRoute.start();

            }
        }
    }

    public Thread startRoute(Socket socket){
        System.out.println("Received new routing request");
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

                //Receive double array of coordinates for the path-search
                ObjectInputStream ois = null;
                try {
                    ois = new ObjectInputStream(is);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //Send Object Outputstream to send Geojson path through.
                ObjectOutputStream oos = null;
                try {
                    oos = new ObjectOutputStream(os);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                double[] coords = new double[0];
                try {
                    coords = (double[])ois.readObject();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                double originLat = coords[0];
                double originLong = coords[1];
                double destinationLat = coords[2];
                double destinationLong = coords[3];
                double algorithm = coords[4];

                ArrayList<Node> route;

                if (algorithm == 0) {
                    route = graph.anyLocationDijkstra(originLat, originLong, destinationLat, destinationLong);

                } else {
                    route = graph.anyLocationAStar(originLat, originLong, destinationLat, destinationLong);
                }
                String routeGeoJson = null;
                try {
                    routeGeoJson = Geojson.transformToString(route);
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
