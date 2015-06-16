package com.liquidhub.framework.config.model

import groovy.transform.ToString

/**
 * Represents a base job configuration elements. Specialized jobs should extend/aggregate this core to add more features
 * 
 * @author Rahul Mishra, LiquidHub
 *
 */
@ToString(includeNames=true, includePackage=false)
class JobConfig {

	def
	disabled, //Indicates if the configuration element is disabled, if this value is true, this job is ignored
	jobPrefix, 
	jobSuffix,
	steps,
	goals,
	description,
	generatorClass,
	goalArgs,
	pollSchedule, //The schedule with which the job polls the SCM for changes
	projectDescriptionTemplatePath, //The path to a HTML template which adds a description for the project
	regularEmailRecipients,  // The list of people who need to be informed about the activities of this job - success,failures
	escalationEmailRecipients // The escalation email list 

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

		[
			'disabled',
			'jobPrefix',
			'jobSuffix',
			'steps',
			'goals',
			'description',
			'generatorClass',
			'pollSchedule',
			'projectDescriptionTemplatePath',
			'regularEmailRecipients',
			'escalationEmailREcipients'
		].each{property ->
			this[property] = otherJobConfig[property] ?:  this[property]
		}

		return this
	}
	
	
	
}
