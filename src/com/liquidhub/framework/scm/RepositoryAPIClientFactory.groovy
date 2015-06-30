package com.liquidhub.framework.scm;

import com.liquidhub.framework.ci.model.JobGenerationContext


public class RepositoryAPIClientFactory {

	private static def logger

	private static def restAPIClient

	static def getInstance(JobGenerationContext context){

		if(!restAPIClient){
			context.logger.debug 'Initializing REST API Client'

			def proxy = new RepositoryAPIClientProxy(context)

			restAPIClient  = proxy.target
			context.logger.debug 'Initialized REST API Client'
		}else{
			context.logger.debug 'Reusing the API Client'
		}

		return restAPIClient
	}
}


