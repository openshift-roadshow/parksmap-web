package com.openshift.evg.roadshow.rest.gateway.helpers;

import feign.Response;
import feign.RetryableException;
import feign.Util;
import feign.codec.ErrorDecoder;

import static feign.FeignException.errorStatus;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by jmorales on 04/10/16.
 */
public class CustomErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String s, Response response) {
        if (response.status() == 503)
            return new RetryableException("Error 503 from server. Let's retry", null);
        else
            return errorStatus(s, response);
    }
}
