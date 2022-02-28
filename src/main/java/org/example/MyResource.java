package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("orsdirections")
public class MyResource {

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String test(
            @QueryParam("originLat") @DefaultValue("8.681495") double start_lat,
            @QueryParam("originLon") @DefaultValue("49.41461") double start_lon,
            @QueryParam("destinationLat") @DefaultValue("8.686507") double end_lat,
            @QueryParam("destinationLon") @DefaultValue("49.41943") double end_lon
    ) { return ShortestPath.RouteFinder(start_lat, start_lon, end_lat, end_lon); }


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String post(JsonObject postRequest) throws JsonProcessingException {
        JsonNumber start_latJson = (JsonNumber) postRequest.get("originLat");
        JsonNumber start_lonJson = (JsonNumber) postRequest.get("originLon");
        JsonNumber end_latJson = (JsonNumber) postRequest.get("destinationLat");
        JsonNumber end_lonJson = (JsonNumber) postRequest.get("destinationLon");
        double start_lat = (float) start_latJson.doubleValue();
        double start_lon = start_lonJson.doubleValue();
        double end_lat = end_latJson.doubleValue();
        double end_lon = end_lonJson.doubleValue();
        return ShortestPath.RouteFinder(start_lat, start_lon, end_lat, end_lon);
    }
}
// http://localhost:9090/sysdev/orsdirections?originLat=8.681495&originLon=49.41461&destinationLat=8.686507&destinationLon=49.41943
