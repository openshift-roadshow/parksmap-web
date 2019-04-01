package com.openshift.evg.roadshow.rest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.Watch;

@Component
public class ServiceWatcher extends AbstractResourceWatcher<Service> {
  private static final Logger logger = LoggerFactory.getLogger(ServiceWatcher.class);

  private static final String PARKSMAP_BACKEND_LABEL = "type=parksmap-backend";

  @Override
  protected List<Service> listWatchedResources() {
    return getOpenShiftClient().services().inNamespace(getNamespace()).withLabel(PARKSMAP_BACKEND_LABEL).list()
        .getItems();
  }

  @Override
  protected Watch doInit() {
    return getOpenShiftClient().services().inNamespace(getNamespace()).withLabel(PARKSMAP_BACKEND_LABEL).watch(this);
  }

  @Override
  protected String getUrl(String serviceName) {
    List<Service> services = getOpenShiftClient().services().inNamespace(getNamespace())
        .withLabel(PARKSMAP_BACKEND_LABEL).withField("metadata.name", serviceName).list().getItems();
    if (services.isEmpty()) {
      return null;
    }

    Service service = services.get(0);
    String serviceURL = "";
    int port = 8080;
    try {
      port = service.getSpec().getPorts().get(0).getPort();
    } catch (Exception e) {
      logger.error("Service {} does not have a port assigned", serviceName);
    }

    serviceURL = "http://" + serviceName + ":" + port;

    logger.info("[INFO] Computed service URL: {}", serviceURL);
    return serviceURL;
  }
}
