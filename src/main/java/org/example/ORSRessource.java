package org.example;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.json.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;



/**
 * Root resource (exposed at "myresource" path)
 */
@Path("/orsdirections")
public class ORSRessource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String post(JsonObject postRequest) throws JsonProcessingException {
        JsonNumber originLatJson = (JsonNumber) postRequest.get("originLat");
        JsonNumber originLongJson = (JsonNumber) postRequest.get("originLon");
        JsonNumber destinationLatJson = (JsonNumber) postRequest.get("destinationLat");
        JsonNumber destinationLongJson = (JsonNumber) postRequest.get("destinationLon");
        double originLat = originLatJson.doubleValue();
        double originLong = originLongJson.doubleValue();
        double destinationLat = destinationLatJson.doubleValue();
        double destinationLong = destinationLongJson.doubleValue();
        String route = RequestDirection.getRoute(originLat, originLong, destinationLat, destinationLong);

        return route;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String get(@QueryParam("originLat") double originLat, @QueryParam("originLon") double originLong,
                            @QueryParam("destinationLat") double destinationLat, @QueryParam("destinationLon") double
                        destinationLong) {
        String route = RequestDirection.getRoute(originLat, originLong, destinationLat, destinationLong);
        System.out.println("test"+route);
        return route;
    }




}
