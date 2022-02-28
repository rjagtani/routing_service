package org.example;

import java.io.*;
import java.net.Socket;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;


@Path("/dijkstra")
public class DijkstraResource {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String get(@QueryParam("originLat") double originLat, @QueryParam("originLon") double originLon,
                      @QueryParam("destinationLat") double destinationLat, @QueryParam("destinationLon") double
                              destinationLon) throws IOException {

        String route = new String();

        try (Socket socket = new Socket("localhost", 1111)) {

            OutputStream output_stream = socket.getOutputStream();
            InputStream input_stream = socket.getInputStream();

            ObjectOutputStream obj_output_stream = new ObjectOutputStream(output_stream);
            ObjectInputStream obj_input_stream = new ObjectInputStream(input_stream);

            double[] requestArray = {originLat, originLon, destinationLat, destinationLon, 0};

            obj_output_stream.writeObject(requestArray);
            route = (String) obj_input_stream.readObject();

            output_stream.close();
            input_stream.close();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return route;
    }
}
