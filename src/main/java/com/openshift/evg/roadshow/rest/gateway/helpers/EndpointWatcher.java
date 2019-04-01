package com.openshift.evg.roadshow.rest.gateway.helpers;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.api.model.Endpoints;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;

/**
 *
 * Created by jmorales on 04/10/16.
 */
public class EndpointWatcher implements Watcher<Endpoints> {

  private static final Logger logger = LoggerFactory.getLogger(EndpointWatcher.class);

  private Watch watch;

  private String namespace;

  private String endpointName;

  private KubernetesClient client;

  private AtomicInteger endpointsAvailable = new AtomicInteger(0);

  private EndpointRegistrar callback;

  public EndpointWatcher(EndpointRegistrar callback, KubernetesClient client, String namespace, String endpointName) {
    this.client = client;
    this.namespace = namespace;
    this.endpointName = endpointName;
    this.callback = callback;
    logger.info("EndpointWatcher created for: endpoints/{} -n {}", endpointName, namespace);

    if (hasEndpoints()) {
      callback.register(endpointName);
    } else {
      callback.unregister(endpointName);
    }
    // Create the watch
    watch = client.endpoints().inNamespace(namespace).withName(endpointName).watch(this);

  }

  private boolean hasEndpoints() {
    return hasEndpoints(client.endpoints().inNamespace(namespace).withName(endpointName).get());
  }

  private boolean hasEndpoints(Endpoints endpoints) {
    int size = getEndpointsAddressSize(endpoints);
    if (size > 0) {
      endpointsAvailable.set(size);
      return size > 0;
    } else
      return false;

  }

  private int getEndpointsAddressSize(Endpoints endpoints) {
    if (endpoints.getSubsets().size() > 0)
      return endpoints.getSubsets().get(0).getAddresses().size();
    else
      return 0;
  }

  @Override
  public void eventReceived(Action action, Endpoints endpoints) {
    int current = getEndpointsAddressSize(endpoints);
    int previous = endpointsAvailable.getAndSet(current);
    if (previous != current) {
      logger.info("Endpoints changed, from {} to {}", previous, current);
      if (previous == 0) {
        if (current > 0) {
          logger.info("There are endpoints for {} available. Registering", endpointName);
          callback.register(endpointName);
        }
      }
      if (current == 0) {
        if (previous > 0) {
          logger.info("There's no endpoints for {}. Unregistering", endpointName);
          callback.unregister(endpointName);
        }
      }
    } else {
      logger.info("Endpoints changes ignored");
    }
  }

  @Override
  public void onClose(KubernetesClientException e) {
    callback.init();
  }

  public void close() {
    if (watch != null)
      watch.close();
  }
}
