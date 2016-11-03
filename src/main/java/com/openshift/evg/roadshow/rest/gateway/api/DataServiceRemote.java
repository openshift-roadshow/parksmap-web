package com.openshift.evg.roadshow.rest.gateway.api;

import com.openshift.evg.roadshow.rest.gateway.model.DataPoint;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.List;

/**
 * Created by jmorales on 28/09/16.
 */
@RequestMapping("/ws/data")
@Path("/ws/data")
@RestController
public interface DataServiceRemote {
    @RequestMapping(method = RequestMethod.GET, value = "/", produces = "application/json")
    @GET()
    @Path("/all")
    @Produces("application/json")
    public List<DataPoint> getAll();

    @RequestMapping(method = RequestMethod.GET, value = "/within", produces = "application/json")
    @GET()
    @Path("/within")
    @Produces("application/json")
    public List<DataPoint> findWithin(
            @RequestParam("lat1") @QueryParam("lat1") float lat1,
            @RequestParam("lon1") @QueryParam("lon1") float lon1,
            @RequestParam("lat2") @QueryParam("lat2") float lat2,
            @RequestParam("lon2") @QueryParam("lon2") float lon2);
/*
    @RequestMapping(method = RequestMethod.GET, value = "/centered", produces = "application/json")
    @GET()
    @Path("/centered")
    @Produces("application/json")
    public List<DataPoint> findCentered(@RequestParam("lat") float lat, @RequestParam("lon") float lon, @RequestParam("maxDistance") int maxDistance, @RequestParam("minDistance") int minDistance);
*/
}