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

		def buildEnvVars = [:]
		buildEnvVars << context.configuration.buildEnvProperties << scmRepository.properties

		repositoryConfigurationInstructions.each{SCMRepositoryConfigurationInstruction apiConfiguration ->

			def uri = context.templateEngine.withTemplatedContent(apiConfiguration.uri, buildEnvVars)

			def args = [uri: baseGitUrl+'/'+uri,headers: authHeader]

			def httpMethod = apiConfiguration.httpMethod.toLowerCase()

			if('get' == httpMethod){
				args.query = apiConfiguration.queryParams
			}else{
				def parameterizedPayload = apiConfiguration?.payload?.replaceAll("\\s+","")
				def payload = parameterizedPayload ? context.templateEngine.withTemplatedContent(parameterizedPayload, buildEnvVars) : ''
				args.body =payload
			}

			def apiName = apiConfiguration.apiName

			//Create new API's on the REST client, so we can call them later
			RepositoryAPIClient.metaClass[apiName] << {
				->
				def response = restClient."${httpMethod}"(args)

			}

		}

		this.target = new RepositoryAPIClient(baseGitUrl, ContentType.JSON)





	}

	/**
	 * An empty wrapper which is enhanced dynamically, so we don't have to add methods to third party libraries (groovy net REST Client)
	 *
	 * @return
	 */

	protected class RepositoryAPIClient{

		public RepositoryAPIClient(baseUrl, contentType){
			restClient = new RESTClient(baseUrl, contentType)
		}

	}


}