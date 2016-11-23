package com.openshift.evg.roadshow.rest.gateway.helpers;

import java.util.List;

import com.openshift.evg.roadshow.rest.gateway.model.Backend;

public interface EndpointRegistrar {
	List<Backend> register(String endpointName);
	
	List<Backend> unregister(String endpointName);
	
	void init();
}
