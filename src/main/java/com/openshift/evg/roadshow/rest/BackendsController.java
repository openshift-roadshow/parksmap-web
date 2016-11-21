package com.openshift.evg.roadshow.rest;

import com.openshift.evg.roadshow.rest.gateway.ApiGatewayController;
import com.openshift.evg.roadshow.rest.gateway.DataGatewayController;
import com.openshift.evg.roadshow.rest.gateway.helpers.EndpointWatcher;
import com.openshift.evg.roadshow.rest.gateway.model.Backend;
import io.fabric8.kubernetes.api.model.Endpoints;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Backend controller. Every time a backend appears/dissapears in OpenShift
 * it will send a notification to the web to show/hide the appropriate layer/map
 * <p>
 * Created by jmorales on 24/08/16.
 */
@RestController
@RequestMapping("/ws/backends")
public class BackendsController implements Watcher<Service> {

    private static final Logger logger = LoggerFactory.getLogger(BackendsController.class);

    private static final String PARKSMAP_BACKEND_LABEL = "type=parksmap-backend";
    private static final String ROUTE_LABEL = "route";

    private String currentNamespace = null;

    @Value("${test}")
    private Boolean test;

    @Autowired
    private KubernetesClient client;

    private Watch watch;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ApiGatewayController apiGateway;

    @Autowired
    private DataGatewayController dataGateway;

    private Map<String, Backend> registeredBackends = new HashMap<String, Backend>();

    private Map<String, EndpointWatcher> serviceEndpointsWatcher = new HashMap<String, EndpointWatcher>();

    BackendsController() {
        if (client != null) {
            logger.info("KubernetesClient configured");
        } else {
            logger.info("KubernetesClient NOT configured");
        }
    }

    /*
     * This method will append port 8080 to services not ending with .cup (so that are not a route)
     */
    private final String getURLforService(String service) {
        Service k8sService = client.services().inNamespace(currentNamespace).withName(service).get();
        String route = k8sService.getMetadata().getLabels().get("route");
        String serviceURL = "";
        if ((route!=null) && (!"".equals(route))) {
            serviceURL = "http://"+route;
        } else {
            int port = 8080;
            try {
                port = k8sService.getSpec().getPorts().get(0).getPort();
            }catch(Exception e){
                logger.error("Service {} does not have a port assigned", service);
            }
            serviceURL =  "http://" + service + ":"+ port;
        }
        logger.info("[INFO] Computed service URL: {}", serviceURL);
        return serviceURL;
    }

    /**
     * This will get notified every time there is a new service with label parksmap-backend added or removed
     *
     * @param action
     * @param service
     */
    @Override
    public void eventReceived(Action action, Service service) {
        logger.info("Action: {}, Service: {}", action, service);

        /*
         * We can set a route label that will be used to get to the service
         * instead of the service name.
         */
        String serviceName = service.getMetadata().getName();
//        String route = service.getMetadata().getLabels().get("route");
//        if (serviceurl == null) {
            String serviceurl = serviceName;
//        }
        if (action == Action.ADDED) {
            logger.info("Service {} added", serviceurl);
            EndpointWatcher epW = serviceEndpointsWatcher.get(serviceurl);
            if (epW == null) {
                epW = new EndpointWatcher(this, client, currentNamespace, serviceName);
                serviceEndpointsWatcher.put(serviceName, epW);
            }
            // register(serviceurl);
        } else if (action == Action.DELETED) {
            logger.info("Service {} deleted", serviceurl);
            EndpointWatcher epW = serviceEndpointsWatcher.get(serviceurl);
            if (epW != null) {
                epW.close();
                unregister(serviceurl);
                serviceEndpointsWatcher.remove(serviceName);
            }
        } else if (action == Action.MODIFIED) {
            // TODO: Modification of a service is cumbersome. Look into how to best implement this
            EndpointWatcher epW = serviceEndpointsWatcher.get(serviceurl);
            serviceEndpointsWatcher.remove(serviceName);
            unregister(serviceurl);
            epW = new EndpointWatcher(this, client, currentNamespace, serviceName);
            serviceEndpointsWatcher.put(serviceName, epW);
        } else if (action == Action.ERROR) {
            logger.error("Service ERRORED");
            EndpointWatcher epW = serviceEndpointsWatcher.get(serviceurl);
            serviceEndpointsWatcher.remove(serviceName);
            epW = new EndpointWatcher(this, client, currentNamespace, serviceName);
            serviceEndpointsWatcher.put(serviceName, epW);
        }
    }

    /**
     * This will get notified when Kubernetes client is closed
     *
     * @param e
     */
    @Override
    public void onClose(KubernetesClientException e) {
        logger.error("[ERROR] There was an error in the client {}", e.getMessage());
        reinitWatches();
    }

    public void reinitWatches(){
        if (watch != null) watch.close();
        // If this watch has been closed, we create complete set of new watches
        for (EndpointWatcher epWatcher: serviceEndpointsWatcher.values()){
            epWatcher.close();
        }
        serviceEndpointsWatcher.clear();
        init();
    }

    /**
     * This method is used to start monitoring for services
     */
    @RequestMapping(method = RequestMethod.GET, value = "/init")
    @PostConstruct
    public void init() {
        logger.info("Watching for services started");

        if (currentNamespace == null) {
            currentNamespace = client.getNamespace();
            logger.info("[INFO] ------------------ {}", currentNamespace);
        }

        // If there is no proper permission, don't fail misserably
        try {
            // Get the list of current services and register them, and then watch for changes
            ServiceList services = client.services().inNamespace(currentNamespace).withLabel(PARKSMAP_BACKEND_LABEL).list();
            for (Service service : services.getItems()) {
                String serviceName = service.getMetadata().getName();
                EndpointWatcher epW = serviceEndpointsWatcher.get(serviceName);
                if (epW == null) {
                    epW = new EndpointWatcher(this, client, currentNamespace, serviceName);
                    serviceEndpointsWatcher.put(serviceName, epW);
                }
            }

            // TODO: This code needs to move to a proper initialization place
            watch = client.services().inNamespace(currentNamespace).withLabel(PARKSMAP_BACKEND_LABEL).watch(this);
        }catch(KubernetesClientException e){
            logger.error("Error initialiting application. Probably you need the appropriate permissions to view this namespace {}. {}", currentNamespace, e.getMessage());
        }
    }

    /**
     * @param
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "/register", produces = "application/json")
    public List<Backend> register(@RequestParam(value = "service") String service) {
        logger.info("Backends.register service at ({})", service);

        Backend newBackend = null;
        // Query for backend data.
        String urlForService = getURLforService(service);
        if ((newBackend = apiGateway.getFromRemote(urlForService)) != null) {
            if (registeredBackends.get(service) == null) {
                // TODO: BackendId should not be fetched from remote. For now I replace the remote one with the local one.
                newBackend.setId(service);
                // Register the new backend
                apiGateway.add(service, urlForService);
                dataGateway.add(service, urlForService);
                registeredBackends.put(service, newBackend);

                logger.info("Backend from server: ({}) ", newBackend);
                // Notify web
                messagingTemplate.convertAndSend("/topic/add", newBackend);
            } else {
                logger.info("Backend with provided id ({}) already registered", service);
            }
        }
        return new ArrayList<Backend>(registeredBackends.values());
    }

    @RequestMapping(method = RequestMethod.GET, value = "/unregister", produces = "application/json")
    public List<Backend> unregister(@RequestParam(value = "service") String service) {
        logger.info("Backends.unregister service at ({})", service);

        Backend backend = null;
        if ((backend = registeredBackends.get(service)) != null) {
            messagingTemplate.convertAndSend("/topic/remove", backend); // Notify web

            registeredBackends.remove(service);
            apiGateway.remove(service);
            dataGateway.remove(service);
        } else {
            logger.info("No backend at ({})", service);
        }
        return new ArrayList<Backend>(registeredBackends.values());
    }


    @RequestMapping(method = RequestMethod.GET, value = "/list", produces = "application/json")
    public List<Backend> getAll() {
        logger.info("Backends: getAll");
        return new ArrayList<Backend>(registeredBackends.values());
    }

}
