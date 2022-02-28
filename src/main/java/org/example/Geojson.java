package org.example;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;


public class Geojson {

    public String type;
    public Geojson[] features;
    public Geojson geometry;
    public double[][] coordinates;
    public HashMap<String, Integer> properties;

    public static String transformToString(ArrayList<Node> routeList) throws JsonProcessingException {
        Geojson pathJson = new Geojson();
        pathJson.type = "FeatureCollection";
        Geojson[] featuresJson = new Geojson[1];
        featuresJson[0] = new Geojson();
        featuresJson[0].type = "Feature";
        featuresJson[0].geometry = new Geojson();
        featuresJson[0].geometry.type = "LineString";
        featuresJson[0].geometry.coordinates = new double[routeList.size()][2];
        for (int i =0; i< routeList.size(); i++){
            //Be careful - in the geo-json the longitude is expected before the latitude

            featuresJson[0].geometry.coordinates[i] = new double[]{routeList.get(i).lon, routeList.get(i).lat};
        }
        featuresJson[0].properties = new HashMap<>();
        pathJson.features = featuresJson;

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); //Nested Geojsons only included subset of fileds
        String pathString = mapper.writeValueAsString(pathJson);
        return pathString;
    }
}