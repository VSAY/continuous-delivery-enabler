package com.liquidhub.framework.providers.jenkins

import com.liquidhub.framework.ci.JobFactory
import com.liquidhub.framework.ci.view.JobViewFactory
import com.liquidhub.framework.config.JobGenerationWorkspaceUtils

/**
 * A Jenkins Job DSL implementation of all three facets of job generation, view generation and workspace utilities
 * 
 * @author Rahul Mishra,LiquidHub
 * 
 */
class JenkinsJobFactory implements JobFactory,JobViewFactory,JobGenerationWorkspaceUtils {

	private def jenkinsDSLFactory

	JenkinsJobFactory(dslFactory){
		this.jenkinsDSLFactory = dslFactory
	}
	
	@Override
	def job(name, type='freeStyleJob', Closure jobConfig) {
		jenkinsDSLFactory."$type"(name, jobConfig)
	}

	@Override
	def view(name, type, Closure viewConfig) {
		this.jenkinsDSLFactory."${type}"(name, viewConfig)
	}

	def getImpl(){
		jenkinsDSLFactory
	}


	@Override
	public fileReader(filePath) {
		jenkinsDSLFactory.readFileFromWorkspace(filePath)
	}


	@Override
	public Object isRunningOnWindows() {
		windowsOS
	}

	private final boolean windowsOS = (System.getenv()['OS']?.toLowerCase() =~ /.*windows.*/).matches() //If the OS belongs to the windows family





}
