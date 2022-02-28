package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.io.*;
import java.net.Socket;

@Path("/astar")
public class AStarRessource {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String get(@QueryParam("originLat") double originLat, @QueryParam("originLon") double originLong,
                      @QueryParam("destinationLat") double destinationLat, @QueryParam("destinationLon") double
                              destinationLong) throws IOException {
        String path = new String();
        try (Socket socket = new Socket("localhost", 1234)) {
            OutputStream os = socket.getOutputStream();
            InputStream is = socket.getInputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);
            ObjectInputStream ois = new ObjectInputStream(is);

            double[] requestArray = {originLat, originLong, destinationLat, destinationLong, 1};
            oos.writeObject(requestArray);
            path = (String) ois.readObject();


            os.close();
            is.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return path;
    }
}
