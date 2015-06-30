package com.liquidhub.framework.providers.jenkins

import groovy.lang.Closure;

import com.liquidhub.framework.ci.JobFactory
import com.liquidhub.framework.ci.view.JobViewFactory

class JenkinsJobFactory implements JobFactory,JobViewFactory {

	private def jenkinsDSLFactory

	JenkinsJobFactory(dslFactory){
		this.jenkinsDSLFactory = dslFactory
	}


	@Override
	def job(name, Closure jobConfig) {
	    jenkinsDSLFactory.freeStyleJob(name, jobConfig)
	}
	
	@Override
	def view(name, type, Closure viewConfig) {
		this.jenkinsDSLFactory."${type}"(name, viewConfig)
	}
	
	def getImpl(){
		jenkinsDSLFactory
	}


	
	
	
}
