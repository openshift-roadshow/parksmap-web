package com.openshift.evg.roadshow.rest.gateway;

import com.openshift.evg.roadshow.rest.gateway.api.BackendServiceLocal;
import com.openshift.evg.roadshow.rest.gateway.api.BackendServiceRemote;
import com.openshift.evg.roadshow.rest.gateway.api.DataServiceRemote;
import com.openshift.evg.roadshow.rest.gateway.model.Backend;
import com.openshift.evg.roadshow.rest.gateway.model.DataPoint;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.jaxrs.JAXRSContract;
import feign.slf4j.Slf4jLogger;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API Gateway. It will dispatch connections to the appropriate backend
 *
 * Created by jmorales on 24/08/16.
 */
@RestController
@RequestMapping("/ws/data")
public class DataGatewayController {

    private Map<String, DataServiceRemote> remoteServices = new HashMap<String, DataServiceRemote>();

    public DataGatewayController(){
    }

    public final void addBackend(String backendId, String url){
        if (remoteServices.get(backendId)==null) {
            remoteServices.put(backendId, Feign.builder().contract(new JAXRSContract()).encoder(new JacksonEncoder())
                    .decoder(new JacksonDecoder()).target(DataServiceRemote.class, url));
            System.out.println("[INFO] Backend ("+backendId+") added to the Data Gateway");
        }else{
            System.out.println("[ERROR] This backend ("+backendId+") did already exist in the Data Gateway");
        }
    }

    public final void removeBackend(String backendId){
        if (remoteServices.get(backendId)!=null) {
            remoteServices.remove(backendId);
            System.out.println("[INFO] Backend ("+backendId+") removed from the Data Gateway");
        }else{
            System.out.println("[ERROR] This backend ("+backendId+") did NOT exist in the Data Gateway");
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{id}", produces = "application/json")
    public List<DataPoint> within(@PathVariable("id") String id) {
        DataServiceRemote remote = remoteServices.get(id);
        if (remote!=null){
            System.out.println("Calling remote service for " + id);
            return remote.getAllParks();
        }
        else{
            System.out.println("[ERROR] No remote service for " + id);
            return null;
        }
    }
}
