package com.liquidhub.framework.providers.jenkins

import com.liquidhub.framework.ci.JobFactory

class JenkinsJobFactory implements JobFactory {

	private def jenkinsDSLFactory

	JenkinsJobFactory(dslFactory){
		this.jenkinsDSLFactory = dslFactory
	}


	@Override
	def job(name, Closure jobConfig) {
	    jenkinsDSLFactory.freeStyleJob(name, jobConfig)
	}
	
	
}
