package com.openshift.evg.roadshow.rest.gateway.helpers;

import com.openshift.evg.roadshow.rest.BackendsController;
import io.fabric8.kubernetes.api.model.Endpoints;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * Created by jmorales on 04/10/16.
 */
public class EndpointWatcher implements Watcher<Endpoints> {

    private static final Logger logger = LoggerFactory.getLogger(EndpointWatcher.class);

    private Watch watch;

    private String namespace;

    private String serviceName;

    private KubernetesClient client;

    private AtomicInteger endpointsAvailable = new AtomicInteger(0);

    private BackendsController callback;

    public EndpointWatcher(BackendsController callback, KubernetesClient client, String namespace, String serviceName){
        this.client = client;
        this.namespace = namespace;
        this.serviceName = serviceName;
        this.callback = callback;
        logger.info("EndpointWatcher created for: enpoints/{} -n {}", serviceName, namespace);

        if (hasEndpoints()){
            callback.register(serviceName);
        }else{
            callback.unregister(serviceName);
        }
        // Create the watch
        watch = client.endpoints().inNamespace(namespace).withName(serviceName).watch(this);

    }


    private boolean hasEndpoints(){
        return hasEndpoints(client.endpoints().inNamespace(namespace).withName(serviceName).get());
    }
    private boolean hasEndpoints(Endpoints endpoints){
        int size = getEndpointsAddressSize(endpoints);
        if (size>0) {
            endpointsAvailable.set(size);
            return size > 0;
        }else
            return false;

    }

    private int getEndpointsAddressSize(Endpoints endpoints){
        if (endpoints.getSubsets().size()>0)
            return endpoints.getSubsets().get(0).getAddresses().size();
        else
            return 0;
    }

    @Override
    public void eventReceived(Action action, Endpoints endpoints) {
        int current = getEndpointsAddressSize(endpoints);
        int previous = endpointsAvailable.getAndSet(current);
        if (previous!=current) {
            logger.info("Endpoints changed, from {} to {}", previous, current);
            if (previous == 0) {
                if (current > 0) {
                    logger.info("There's endpoints for service {} available. Registering", serviceName);
                    callback.register(serviceName);
                }
            }
            if (current == 0) {
                if (previous > 0) {
                    logger.info("There's no endpoints for service {}. Unregistering", serviceName);
                    callback.unregister(serviceName);
                }
            }
        }else{
            logger.info("Endpoints change not affecting the services");
        }
    }

    @Override
    public void onClose(KubernetesClientException e) {
        callback.reinitWatches();
    }

    public void close(){
        if (watch!=null) watch.close();
    }
}
