package com.liquidhub.framework.model

import com.liquidhub.framework.ci.JobGeneratorFactory
import com.liquidhub.framework.ci.logger.Logger
import com.liquidhub.framework.config.model.Configuration
import com.liquidhub.framework.providers.jenkins.JenkinsWorkspaceUtils
import com.liquidhub.framework.scm.model.SCMRepository

class JobGenerationContext {

	JobGeneratorFactory jobFactory

	JenkinsWorkspaceUtils workspaceUtils

	Configuration configuration

	private static Logger logger

	SCMRepository scmRepository

	public def generateJob(name, jobConfig){
		jobFactory.job(name, jobConfig)
	}


	public def projectName(){
		scmRepository?.projectKey
	}
	
	public def buildToolConfig(){
		configuration.buildConfig
	}
	
	
	public def configurers(providerKey){
		configuration.configurableSections.provider(providerKey)
	}
}
