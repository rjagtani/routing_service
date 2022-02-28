package org.example;

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
    @Produces(MediaType.TEXT_PLAIN)
//    public String getIt() {
//        return "Got it!";
//    }
    public String test(
            @QueryParam("originLat") @DefaultValue("8.681495") float start_lat,
            @QueryParam("originLon") @DefaultValue("49.41461") float start_lon,
            @QueryParam("destinationLat") @DefaultValue("8.686507") float end_lat,
            @QueryParam("destinationLon") @DefaultValue("49.41943") float end_lon
    ) { return ShortestPath.RouteFinder(start_lat, start_lon, end_lat, end_lon); }
}
// http://localhost:9090/sysdev/orsdirections?originLat=8.681495&originLon=49.41461&destinationLat=8.686507&destinationLon=49.41943
