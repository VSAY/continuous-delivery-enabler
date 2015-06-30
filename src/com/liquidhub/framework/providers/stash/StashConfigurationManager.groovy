package com.liquidhub.framework.providers.stash

import groovyx.net.http.HttpResponseException

import com.liquidhub.framework.ci.logger.Logger
import com.liquidhub.framework.ci.model.JobGenerationContext

public class StashConfigurationManager {

	private def  repositoryAPIClient
	
	private static Logger logger

	def configure(JobGenerationContext context){

		this.repositoryAPIClient = context.scmAPIClient
		
		this.logger =  context.logger

		def repositoryAPIConfiguration = context.scmRepositoryConfigurationInstructions

		//Find all API's which are configured to be auto invoked and grab their names
		def autoInvokableAPIs = repositoryAPIConfiguration.grep{it?.autoInvoke?.toBoolean() }.collect{it.apiName}

		logger.debug 'Preparing to invoke the following API\'s '+autoInvokableAPIs

		autoInvokableAPIs.each{invokeOnRepository(it) }
	}


	def invokeOnRepository(api){

		try{
			logger.debug 'Now invoking API '+api

			def response = repositoryAPIClient."$api"()

			if(response && response.success){//Any status code >= 100 and < 400
				logger.info 'Successfully invoked API '+api+' with HTTP Status Code '+response.statusLine.statusCode
			}else{
				logger.error 'No response recieved for API '+api
			}

		}catch(HttpResponseException ex){

			StringBuilder builder = new StringBuilder(512)
			ex.response.data.errors.each{
				builder.append(it.message+'. ')				
			}

			logger.warn 'API returned with HTTP Code '+ex.response?.status
			logger.warn 'API invocation failed for '+api+'. Server returned " '+builder.toString()+' "'
			logger.warn 'The above is not necessarily a problem and may occur if the API is not idempotent, please verify against the HTTP code to confirm outcome'
			
		}


	}


}
