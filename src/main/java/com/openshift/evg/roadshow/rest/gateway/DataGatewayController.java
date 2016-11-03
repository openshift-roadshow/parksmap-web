package com.openshift.evg.roadshow.rest.gateway;

import com.openshift.evg.roadshow.rest.gateway.api.DataServiceRemote;
import com.openshift.evg.roadshow.rest.gateway.helpers.CustomErrorDecoder;
import com.openshift.evg.roadshow.rest.gateway.model.DataPoint;
import feign.Feign;
import feign.Retryer;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.jaxrs.JAXRSContract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * API Gateway. It will dispatch connections to the appropriate backend
 *
 * Created by jmorales on 24/08/16.
 */
@RestController
@RequestMapping("/ws/data")
public class DataGatewayController {

    private static final Logger logger = LoggerFactory.getLogger(DataGatewayController.class);
    
    private Map<String, DataServiceRemote> remoteServices = new HashMap<String, DataServiceRemote>();

    public DataGatewayController(){
    }

    /**
     *
     * @param backendId
     * @param url
     */
    public final void add(String backendId, String url){
        if (remoteServices.get(backendId)==null) {
            remoteServices.put(backendId, Feign.builder().contract(new JAXRSContract()).encoder(new JacksonEncoder())
                    .decoder(new JacksonDecoder())
                    .target(DataServiceRemote.class, url));
            logger.info("Backend ({}) added to the Data Gateway", backendId);
        }else{
            logger.error("This backend ({}) did already exist in the Data Gateway", backendId);
        }
    }

    /**
     *
     * @param backendId
     */
    public final void remove(String backendId){
        if (remoteServices.get(backendId)!=null) {
            remoteServices.remove(backendId);
            logger.info("Backend ({}) removed from the Data Gateway", backendId);
        }else{
            logger.error("This backend ({}) did NOT exist in the Data Gateway", backendId);
        }
    }


    @RequestMapping(method = RequestMethod.GET, value = "/all", produces = "application/json")
    public List<DataPoint> getAll(@RequestParam(value="service") String serviceURL) {
        DataServiceRemote remote = remoteServices.get(serviceURL);
        if (remote!=null){
            logger.info("[WEB-CALL] Calling remote service for {}", serviceURL);
            return remote.getAll();
        }
        else{
            logger.error("[WEB-CALL] No remote service for {}", serviceURL);
            return null;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/within", produces = "application/json")
    public List<DataPoint> getWithin(@RequestParam(value="service") String serviceURL,
                                     @RequestParam("lat1") float lat1,
                                     @RequestParam("lon1") float lon1,
                                     @RequestParam("lat2") float lat2,
                                     @RequestParam("lon2") float lon2) {
        DataServiceRemote remote = remoteServices.get(serviceURL);
        if (remote!=null){
            logger.info("[WEB-CALL] Calling remote service for {}", serviceURL);
            return remote.findWithin(lat1,lon1,lat2,lon2);
        }
        else{
            logger.error("[WEB-CALL] No remote service for {}", serviceURL);
            return null;
        }
    }


}
