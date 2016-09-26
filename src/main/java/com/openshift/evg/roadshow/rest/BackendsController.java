package com.openshift.evg.roadshow.rest;

import com.openshift.evg.roadshow.rest.model.Backend;
import com.openshift.evg.roadshow.rest.model.Coordinates;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.KubernetesClient;
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
        System.out.println("[INFO] Backends.register(" + backend + ")");

        backends.put(backend.getName(), backend);
        // Notify web
        messagingTemplate.convertAndSend("/topic/add", backend);

        return new ArrayList<Backend>(backends.values());
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/", produces = "application/json", consumes = "application/json")
    public List<Backend> delete(@RequestBody Backend backend) {

        System.out.println("[INFO] Backends.delete(" + backend + ")");
        backends.remove(backend.getName());
        // Notify web
        messagingTemplate.convertAndSend("/topic/remove", backend);

        return new ArrayList<Backend>(backends.values());
    }


    @RequestMapping(method = RequestMethod.GET, value = "/", produces = "application/json")
    public List<Backend> getAll() {
        System.out.println("[INFO] Backends: getAll");

        return new ArrayList<Backend>(backends.values());
    }

}
