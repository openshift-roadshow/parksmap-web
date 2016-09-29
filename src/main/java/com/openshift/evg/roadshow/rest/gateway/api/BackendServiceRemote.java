package com.openshift.evg.roadshow.rest.gateway.api;

import com.openshift.evg.roadshow.rest.gateway.model.Backend;

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
public interface BackendServiceRemote {
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Backend get();
}
