package com.openshift.evg.roadshow.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openshift.evg.roadshow.rest.gateway.helpers.EndpointRegistrar;
import com.openshift.evg.roadshow.rest.gateway.helpers.EndpointWatcher;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;

public abstract class AbstractResourceWatcher<T extends HasMetadata> implements Watcher<T> {
  private static final Logger logger = LoggerFactory.getLogger(AbstractResourceWatcher.class);

  private String currentNamespace = null;

  private Watch watch;

  private OpenShiftClient client = new DefaultOpenShiftClient();;

  private EndpointRegistrar endpointRegistrar;

  private Map<String, EndpointWatcher> endpointsWatchers = new HashMap<String, EndpointWatcher>();

  @Override
  public void eventReceived(Action action, T t) {
    logger.info("Action: {}, Resource: {}", action, t);

    String resourceName = t.getMetadata().getName();
    // }
    if (action == Action.ADDED) {
      logger.info("Resource {} added", resourceName);
      EndpointWatcher epW = endpointsWatchers.get(resourceName);
      if (epW == null) {
        epW = new EndpointWatcher(endpointRegistrar, client, currentNamespace, resourceName);
        endpointsWatchers.put(resourceName, epW);
      }
    } else if (action == Action.DELETED) {
      logger.info("Resource {} deleted", resourceName);
      EndpointWatcher epW = endpointsWatchers.get(resourceName);
      if (epW != null) {
        epW.close();
        endpointRegistrar.unregister(resourceName);
        endpointsWatchers.remove(resourceName);
      }
    } else if (action == Action.MODIFIED) {
      // TODO: Modification of a resource is cumbersome. Look into how to
      // best implement this
      EndpointWatcher epW = endpointsWatchers.get(resourceName);
      endpointsWatchers.remove(resourceName);
      endpointRegistrar.unregister(resourceName);
      epW = new EndpointWatcher(endpointRegistrar, client, currentNamespace, resourceName);
      endpointsWatchers.put(resourceName, epW);

    } else if (action == Action.ERROR) {
      logger.error("Resource ERRORED");
      EndpointWatcher epW = endpointsWatchers.get(resourceName);
      endpointsWatchers.remove(resourceName);
      epW = new EndpointWatcher(endpointRegistrar, client, currentNamespace, resourceName);
      endpointsWatchers.put(resourceName, epW);
    }
  }

  /**
   * This will get notified when Kubernetes client is closed
   *
   * @param e
   */
  @Override
  public void onClose(KubernetesClientException e) {
    if (e != null) {
      // This is when the client is closed
      logger.error("[ERROR] There was an error in the client {}", e.getMessage());
      init(endpointRegistrar);
    } else {
      logger.info("Closing this Watcher");
    }
  }

  private void cleanUp() {
    if (watch != null)
      watch.close();
    // If this watch has been closed, we create complete set of new watches
    for (EndpointWatcher epWatcher : endpointsWatchers.values()) {
      epWatcher.close();
    }
    endpointsWatchers.clear();
  }

  public void init(EndpointRegistrar endpointRegistrar) {
    cleanUp();

    // BackendsController
    if (this.endpointRegistrar == null) {
      this.endpointRegistrar = endpointRegistrar;
    }

    if (currentNamespace == null) {
      currentNamespace = client.getNamespace();
    }

    logger.info("[INFO] {} is watching for resources started in namespace {} ", this.getClass().getName(),
        currentNamespace);

    try {
      List<T> resources = listWatchedResources();
      for (T resource : resources) {
        String resourceName = resource.getMetadata().getName();
        EndpointWatcher endpointWatcher = endpointsWatchers.get(resourceName);
        if (endpointWatcher == null) {
          endpointWatcher = new EndpointWatcher(endpointRegistrar, client, currentNamespace, resourceName);
          endpointsWatchers.put(resourceName, endpointWatcher);
        }
      }

      watch = doInit();
    } catch (KubernetesClientException e) {
      // If there is no proper permission, don't fail misserably
      logger.error(
          "Error initialiting application. Probably you need the appropriate permissions to view this namespace {}. {}",
          currentNamespace, e.getMessage());
    }
  }

  protected OpenShiftClient getOpenShiftClient() {
    return client;
  }

  protected String getNamespace() {
    return currentNamespace;
  }

  protected abstract List<T> listWatchedResources();

  protected abstract Watch doInit();

  protected abstract String getUrl(String resourceName);
}
