package com.openshift.evg.roadshow.rest;

import com.openshift.evg.roadshow.rest.gateway.ApiGatewayController;
import com.openshift.evg.roadshow.rest.gateway.DataGatewayController;
import com.openshift.evg.roadshow.rest.gateway.model.Backend;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Backend controller. Every time a backend appears/dissapears in OpenShift
 * it will send a notification to the web to show/hide the appropriate layer/map
 *
 * Created by jmorales on 24/08/16.
 */
@RestController
@RequestMapping("/ws/backends")
public class BackendsController implements Watcher<Service> {

    public static final String PARKSMAP_BACKEND = "parksmap-backend";

    // TODO: Fix the kubernetes client to do auto-discovery
//    @Autowired
//    private KubernetesClient client;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ApiGatewayController apiGateway;


    @Autowired
    private DataGatewayController dataGateway;


    Map<String, Backend> backends = new HashMap<String, Backend>();

    BackendsController() {
        // Add a watcher to the backend services
//        client.services().withLabelIn(PARKSMAP_BACKEND).watch(this);
    }

    /**
     * This will get notified every time there is a new service with label parksmap-backend added or removed
     *
     * @param action
     * @param service
     */
    @Override
    public void eventReceived(Action action, Service service) {
        if (action==Action.ADDED) {
            System.out.println("Service " + service.getMetadata().getName() + " added");
            // TODO: create the Backend and call register
        }else if (action==Action.DELETED){
            System.out.println("Service " + service.getMetadata().getName() + " deleted");
            // TODO: create the Backend and call unregister
        }
    }

    /**
     * This will get notified when Kubernetes client is closed
     * @param e
     */
    @Override
    public void onClose(KubernetesClientException e) {
        System.out.println("There was an error in the client: " + e.getMessage());
    }



    @RequestMapping(method = RequestMethod.POST, value = "/", produces = "application/json", consumes = "application/json")
    public List<Backend> register(@RequestBody Backend backend) {
        // TODO: Change the Backend to just id and service
        System.out.println("[INFO] Backends.register(" + backend + ")");

        if (backends.get(backend.getId())==null) {
            apiGateway.addBackend(backend.getId(), backend.getService());
            dataGateway.addBackend(backend.getId(), backend.getService());

            // Query for backend data.
            Backend newBackend = apiGateway.get(backend.getId());

            backends.put(backend.getId(), newBackend);

            System.out.println("[INFO] Backend from server: " + newBackend);
            // Notify web
            messagingTemplate.convertAndSend("/topic/add", newBackend);
        }else{
            System.out.println("[INFO] Backend with provided id (" + backend + ") already registered");
        }
        return new ArrayList<Backend>(backends.values());
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/", produces = "application/json", consumes = "application/json")
    public List<Backend> delete(@RequestBody Backend backend) {
        // TODO: Change the Backend to just id
        System.out.println("[INFO] Backends.delete(" + backend + ")");

        if (backends.get(backend.getId())!=null) {
            // Query for backend data.
            Backend newBackend = apiGateway.get(backend.getId());
            backends.put(backend.getId(), newBackend);

            backends.remove(newBackend.getId());
            // Notify web
            messagingTemplate.convertAndSend("/topic/remove", newBackend);


            apiGateway.removeBackend(backend.getId());
            dataGateway.removeBackend(backend.getId());
        }else{
            System.out.println("[INFO] No backend with provided id (" + backend + ")");
        }


        return new ArrayList<Backend>(backends.values());
    }


    @RequestMapping(method = RequestMethod.GET, value = "/", produces = "application/json")
    public List<Backend> getAll() {
        System.out.println("[INFO] Backends: getAll");

        return new ArrayList<Backend>(backends.values());
    }

}
