package com.liquidhub.framework.config.model

import groovy.transform.ToString

/**
 * Represents a standard configuration for a gitflow branch creation job
 * 
 * @author Rahul Mishra, LiquidHub
 *
 */
@ToString(includeNames=true)
class GitflowBranchingConfig  {

	JobConfig startConfig, finishConfig //The gitflow configuration is made of regular old core job configurations, so we merely delegate

	def generatorClass, regularEmailRecipients,escalationEmailRecipients
	

	def merge(GitflowBranchingConfig otherConfig){

		this.startConfig.merge(otherConfig.startConfig)
		this.finishConfig.merge(otherConfig.finishConfig)
		
		
	
		
		
	}
}
