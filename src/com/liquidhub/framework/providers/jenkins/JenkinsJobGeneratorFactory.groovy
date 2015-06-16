package com.liquidhub.framework.providers.jenkins

import groovy.lang.Closure;

import com.liquidhub.framework.ci.JobGeneratorFactory
import com.liquidhub.framework.model.JobGenerationContext

class JenkinsJobGeneratorFactory implements JobGeneratorFactory {

	private def jenkinsDSLFactory

	JenkinsJobGeneratorFactory(dslFactory){
		this.jenkinsDSLFactory = dslFactory
	}


	@Override
	def job(name, Closure jobConfig) {
	    jenkinsDSLFactory.freeStyleJob(name, jobConfig)
	}
	
	
}
