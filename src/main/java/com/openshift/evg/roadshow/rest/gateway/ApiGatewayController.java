package com.openshift.evg.roadshow.rest.gateway;

import com.openshift.evg.roadshow.rest.gateway.api.BackendServiceRemote;
import com.openshift.evg.roadshow.rest.gateway.helpers.CustomErrorDecoder;
import com.openshift.evg.roadshow.rest.gateway.model.Backend;

import feign.Feign;
import feign.Retryer;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.jaxrs.JAXRSContract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * API Gateway. It will dispatch connections to the appropriate backend
 * <p>
 * Created by jmorales on 24/08/16.
 */
@Controller
public class ApiGatewayController {

  private static final Logger logger = LoggerFactory.getLogger(ApiGatewayController.class);

  private Map<String, BackendServiceRemote> remoteServices = new HashMap<String, BackendServiceRemote>();

  /**
   *
   *
   * @param backendId
   * @param url
   */
  public final void add(String backendId, String url) {
    if (remoteServices.get(backendId) == null) {
      remoteServices.put(backendId, Feign.builder().client(CustomFeignClient.getClient()).contract(new JAXRSContract()).encoder(new JacksonEncoder())
          .decoder(new JacksonDecoder()).target(BackendServiceRemote.class, url));
      logger.info("Backend ({}) added to the API Gateway", backendId);
    } else {
      logger.error("This backend ({}) did already exist in the API Gateway", backendId);
    }
  }

  /**
   *
   *
   * @param backendId
   */
  public final void remove(String backendId) {
    if (remoteServices.get(backendId) != null) {
      remoteServices.remove(backendId);
      logger.info("Backend ({}) removed from the API Gateway", backendId);
    } else {
      logger.error("This backend ({}) did NOT exist in the API Gateway", backendId);
    }
  }

  /**
   *
   *
   * @param backendId
   * @return
   */
  public Backend getFromLocal(String backendId) {
    BackendServiceRemote backend = null;
    if ((backend = remoteServices.get(backendId)) != null) {
      logger.info("Calling remote service {}", backendId);
      try {
        return backend.get();
      } catch (Exception e) {
        logger.error("Error connecting to backend server {}", e.getMessage());
      }
    }
    return null;
  }

  /**
   *
   *
   * @param remoteURL
   * @return
   */
  public Backend getFromRemote(String remoteURL) {
    logger.info("Calling remote service at {}", remoteURL);
    try {
      return Feign.builder().client(CustomFeignClient.getClient()).contract(new JAXRSContract()).encoder(new JacksonEncoder()).decoder(new JacksonDecoder())
          .retryer(new Retryer.Default(200, SECONDS.toMillis(1), 5)).errorDecoder(new CustomErrorDecoder())
          .target(BackendServiceRemote.class, remoteURL).get();
    } catch (Exception e) {
      logger.error("Error connecting to backend server {}", e.getMessage());
      logger.error("Error message",e);
    }
    return null;
  }

}
