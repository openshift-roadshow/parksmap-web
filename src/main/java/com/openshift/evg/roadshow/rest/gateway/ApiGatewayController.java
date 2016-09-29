package com.openshift.evg.roadshow.rest.gateway;

import com.openshift.evg.roadshow.rest.gateway.api.BackendServiceLocal;
import com.openshift.evg.roadshow.rest.gateway.api.BackendServiceRemote;
import com.openshift.evg.roadshow.rest.gateway.model.Backend;
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
@RequestMapping("/ws/info")
public class ApiGatewayController implements BackendServiceLocal {

    private Map<String, BackendServiceRemote> remoteServices = new HashMap<String, BackendServiceRemote>();


    public ApiGatewayController(){
    }

    public final void addBackend(String backendId, String url){
        if (remoteServices.get(backendId)==null) {
            remoteServices.put(backendId, Feign.builder().contract(new JAXRSContract()).encoder(new JacksonEncoder())
                    .decoder(new JacksonDecoder()).logger(new Slf4jLogger()).target(BackendServiceRemote.class, url));
            System.out.println("[INFO] Backend ("+backendId+") added to the API Gateway");
        }else{
            System.out.println("[ERROR] This backend ("+backendId+") did already exist in the API Gateway");
        }
    }

    public final void removeBackend(String backendId){
        if (remoteServices.get(backendId)!=null) {
            remoteServices.remove(backendId);
            System.out.println("[INFO] Backend ("+backendId+") removed from the API Gateway");
        }else{
            System.out.println("[ERROR] This backend ("+backendId+") did NOT exist in the API Gateway");
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{id}", produces = "application/json")
    public Backend get(@PathVariable("id") String id) {
        BackendServiceRemote remote = remoteServices.get(id);
        if (remote!=null){
            System.out.println("Calling remote service for " + id);
            return remote.get();
        }
        else{
            System.out.println("[ERROR] No remote service for " + id);
            return null;
        }
    }
}
