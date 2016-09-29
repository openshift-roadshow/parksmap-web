package com.openshift.evg.roadshow.rest.gateway.api;

import com.openshift.evg.roadshow.rest.gateway.model.DataPoint;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.List;

/**
 * Created by jmorales on 28/09/16.
 */
public interface DataServiceLocal {
    @RequestMapping(method = RequestMethod.GET, value = "/", produces = "application/json")
    @GET()
    @Path("/{id}")
    @Produces("application/json")
    public List<DataPoint> getAllParks(@PathVariable("id") String id);

    @RequestMapping(method = RequestMethod.GET, value = "/within", produces = "application/json")
    @GET()
    @Path("/{id}/within")
    @Produces("application/json")
    public List<DataPoint> findParksWithin(@PathVariable("id") String id,
            @RequestParam("lat1") float lat1,
            @RequestParam("lon1") float lon1,
            @RequestParam("lat2") float lat2,
            @RequestParam("lon2") float lon2);

    @RequestMapping(method = RequestMethod.GET, value = "/centered", produces = "application/json")
    @GET()
    @Path("/{id}/centered")
    @Produces("application/json")
    public List<DataPoint> findParksCentered(@PathVariable("id") String id, @RequestParam("lat") float lat, @RequestParam("lon") float lon, @RequestParam("maxDistance") int maxDistance, @RequestParam("minDistance") int minDistance);

}
