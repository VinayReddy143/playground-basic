package com.fhir.querying;
import java.io.IOException;
import java.util.List;

import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import ca.uhn.fhir.rest.client.api.IHttpResponse;

public class ClientInterceptorImpl implements IClientInterceptor{
	
	private List<Long> responseTimes;
	
	

	public List<Long> getResponseTimes() {
		return responseTimes;
	}

	public void setResponseTimes(List<Long> responseTimes) {
		this.responseTimes = responseTimes;
	}

	@Override
	public void interceptRequest(IHttpRequest theRequest) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void interceptResponse(IHttpResponse theResponse) throws IOException {
		long responseTime = theResponse.getRequestStopWatch().getMillis();
		this.getResponseTimes().add(responseTime);
		
	}

}
