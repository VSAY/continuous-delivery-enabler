package com.liquidhub.framework.scm

import groovy.net.http.*
import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient

import com.liquidhub.framework.ci.model.JobGenerationContext
import com.liquidhub.framework.config.model.SCMRepositoryConfigurationInstruction
import com.liquidhub.framework.scm.model.SCMRepository


class RepositoryAPIClientProxy{

	private RESTClient restClient //This is the internal REST client we use

	private def authHeader, logger, projectKey,repositorySlug, target

	RepositoryAPIClientProxy(JobGenerationContext context){

		this.logger = logger

		SCMRepository scmRepository = context.scmRepository

		def baseGitUrl = scmRepository.baseUrl
		def repositoryConfigurationInstructions  = context.scmRepositoryConfigurationInstructions
		def authHeader = [Authorization: 'Basic '+scmRepository.authorizedUserDigest]

		def allParams = [:] << context.configuration.buildEnvProperties << scmRepository.properties
		
		restClient = new RESTClient(baseGitUrl, ContentType.JSON)

		RepositoryAPIClient.metaClass.methodMissing = {name, methodArgs ->

			methodArgs.each{it instanceof Map ? allParams << it : allParams}

			SCMRepositoryConfigurationInstruction apiConfiguration = repositoryConfigurationInstructions.findResult{it.apiName == name ? it : null}

			if(!apiConfiguration){
				throw new MissingMethodException(name, RepositoryAPIClient.class, methodArgs)
			}


			def uri = context.templateEngine.withTemplatedContent(apiConfiguration.uri, allParams)

			def httpArgs = [uri: baseGitUrl+'/'+uri,headers: authHeader]

			def httpMethod = apiConfiguration.httpMethod.toLowerCase()

			if('get' == httpMethod){
				httpArgs.query = apiConfiguration.queryParams
			}else{
				def parameterizedPayload = apiConfiguration?.payload?.replaceAll("\\s+","")
				def payload = parameterizedPayload ? context.templateEngine.withTemplatedContent(parameterizedPayload, allParams) : ''
				httpArgs.body =payload
			}

			def apiName = apiConfiguration.apiName

			restClient."${httpMethod}"(httpArgs)

		}

		this.target = new RepositoryAPIClient()

	}

	/**
	 * An empty wrapper which is enhanced dynamically, so we don't have to add methods to third party libraries (groovy net REST Client)
	 *
	 * @return
	 */

	protected class RepositoryAPIClient{






	}




}