package com.liquidhub.framework.model

import groovy.transform.ToString

/**
 * Represents a base job configuration elements. Specialized jobs should extend/aggregate this core to add more features
 * 
 * 
 * @author Rahul Mishra, LiquidHub
 *
 */
@ToString(includeNames=true)
class CoreJobConfig {

	def jobPrefix, jobSuffix, steps, goals,description,generatorClass, goalArgs, pollSchedule,projectDescriptionTemplatePath

	private static def logger
	
	
    /**
     * Encapsulates the merge algorithm when this source config is merged with another incoming job configuration
     * 
     * @param otherJobConfig The job configuration which needs to be merged
     * 
     * @return the updated job configuration
     */
	def merge(otherJobConfig){
		
		if(!otherJobConfig)return this
		
		logger.debug 'invoked with '+otherJobConfig
		['jobPrefix', 'jobSuffix', 'steps', 'goals','description','generatorClass', 'pollSchedule','projectDescriptionTemplatePath'].each{property ->			
			this[property] = otherJobConfig[property] ?:  this[property]
		}
		
		return this
	}
	
	
}
