package com.openshift.evg.roadshow.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.openshift.evg.roadshow.rest.gateway.ApiGatewayController;
import com.openshift.evg.roadshow.rest.gateway.DataGatewayController;
import com.openshift.evg.roadshow.rest.gateway.helpers.EndpointRegistrar;
import com.openshift.evg.roadshow.rest.gateway.model.Backend;

/**
 * Backend controller. Every time a backend appears/dissapears in OpenShift
 * it will send a notification to the web to show/hide the appropriate layer/map
 * <p>
 * Created by jmorales on 24/08/16.
 */
@RestController
@RequestMapping("/ws/backends")
public class BackendsController implements EndpointRegistrar {

    private static final Logger logger = LoggerFactory.getLogger(BackendsController.class);

    @Value("${test}")
    private Boolean test;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ApiGatewayController apiGateway;

    @Autowired
    private DataGatewayController dataGateway;
    
    @Autowired
    private ServiceWatcher serviceWatcher;
    
    @Autowired
    private RouteWatcher routeWatcher;
    
    private Map<String, Backend> registeredBackends = new HashMap<String, Backend>();
    
    /**
     * This method is used to start monitoring for services
     */
    @RequestMapping(method = RequestMethod.GET, value = "/init")
    @PostConstruct
    public void init() {
    	routeWatcher.init(this);
    	serviceWatcher.init(this);
    }

    /**
     * @param
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "/register", produces = "application/json")
    public List<Backend> register(@RequestParam(value = "endpoint") String endpoint) {
        logger.info("Backends.register endpoint at ({})", endpoint);

        Backend newBackend = null;
        
        String endpointUrl = routeWatcher.getUrl(endpoint); // try to find a route for endpoint
        if (endpointUrl == null || endpointUrl.trim().equals("")) { 
        	endpointUrl = serviceWatcher.getUrl(endpoint); // otherwise, find a service for endpoint
        }
        
        // Query for backend data.
        if (endpoint != null) {
        	if ((newBackend = apiGateway.getFromRemote(endpointUrl)) != null) {
                // TODO: BackendId should not be fetched from remote. For now I replace the remote one with the local one.
                newBackend.setId(endpoint);
                // Register the new backend
                apiGateway.add(endpoint, endpointUrl);
                dataGateway.add(endpoint, endpointUrl);
                registeredBackends.put(endpoint, newBackend);

                logger.info("Backend from server: ({}) ", newBackend);
                // Notify web
                messagingTemplate.convertAndSend("/topic/add", newBackend);
            } else {
                logger.info("Backend with provided id ({}) already registered", endpoint);
            }
        }
        return new ArrayList<Backend>(registeredBackends.values());
    }

    @RequestMapping(method = RequestMethod.GET, value = "/unregister", produces = "application/json")
    public List<Backend> unregister(@RequestParam(value = "endpointName") String endpointName) {
        logger.info("Backends.unregister service at ({})", endpointName);

        Backend backend = null;
        if ((backend = registeredBackends.get(endpointName)) != null) {
            messagingTemplate.convertAndSend("/topic/remove", backend); // Notify web

            registeredBackends.remove(endpointName);
            apiGateway.remove(endpointName);
            dataGateway.remove(endpointName);
        } else {
            logger.info("No backend at ({})", endpointName);
        }
        return new ArrayList<Backend>(registeredBackends.values());
    }

    @RequestMapping(method = RequestMethod.GET, value = "/list", produces = "application/json")
    public List<Backend> getAll() {
        logger.info("Backends: getAll");
        return new ArrayList<Backend>(registeredBackends.values());
    }
}
