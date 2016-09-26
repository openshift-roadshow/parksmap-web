package com.openshift.evg.roadshow.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * API Gateway. It will dispatch connections to the appropriate backend
 *
 * Created by jmorales on 24/08/16.
 */
@RestController
@RequestMapping("/ws/api")
public class ApiGatewayController {

//    @Autowired
//    private FeignClientFactory feignClientFactory;

    @CrossOrigin
    @RequestMapping(method = RequestMethod.GET, value = "/", produces = "application/json")
    public List<String> api() {

        return new ArrayList<String>();
        /*
        return feignClientFactory.getFeignClients()
                .stream()
                .parallel()
                // We set the ServerSpan to each client to avoid loosing the tracking in a multi-thread invocation
                .map((feign) -> feign.invokeService(serverSpan))
                .collect(Collectors.toList());
                */
    }
}
