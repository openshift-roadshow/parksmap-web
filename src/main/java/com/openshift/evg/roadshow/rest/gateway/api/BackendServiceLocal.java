package com.openshift.evg.roadshow.rest.gateway.api;

import com.openshift.evg.roadshow.rest.gateway.model.Backend;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Contract to use in the backends to provide backend information
 *
 * Created by jmorales on 26/09/16.
 */
@Path("/ws/info")
@RequestMapping("/ws/info")
public interface BackendServiceLocal {
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RequestMapping(method = RequestMethod.GET, value = "/{id}", produces = "application/json")
    public Backend get(final @PathParam("id") String id);
}
