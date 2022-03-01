package org.example;


import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;

import java.io.StringReader;

public class ShortestPath {

    private static final String OPENROUTESERVICE_URL = "https://api.openrouteservice.org/v2/directions/driving-car/geojson";
    private static final String OPENROUTESERVICE_KEY = "5b3ce3597851110001cf62487cad17df7c1b40d3828d9ad10054c05a";

    public static String RouteFinder(double start_lat, double start_lon, double end_lat, double end_lon) {

        final JsonObject request_api=Json.createObjectBuilder()
                .add(
                        "coordinates",
                        Json.createArrayBuilder()
                                .add(Json.createArrayBuilder().add(start_lon).add(start_lat))
                                .add(Json.createArrayBuilder().add(end_lon).add(end_lat))
                                .build()
                ).build();
        // use the jersey client api to make HTTP requests
        final JerseyClient client = new JerseyClientBuilder().build();

        Response response = client.target(OPENROUTESERVICE_URL)
                .request()
                .header("Authorization", OPENROUTESERVICE_KEY)
                .header("Accept", "application/json, application/geo+json, application/gpx+xml, img/png; charset=utf-8")
                .header("Content-Type", "application/json; charset=utf-8")
                .post(Entity.json(request_api));
        // check the result
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            throw new RuntimeException("Failed: HTTP error code: " + response.getStatus());
        }
        // get the JSON response
        final String responseString = response.readEntity(String.class);
        final JsonObject jsonObject = Json.createReader(new StringReader(responseString)).readObject();
        System.out.println(responseString);
        return responseString;
    }
}

