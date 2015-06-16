package com.liquidhub.framework.config.model

import groovy.transform.ToString

import com.liquidhub.framework.JobSectionConfigurer

/**
 * An exhaustive list of configuration sections for which we allow provider configuration
 * 
 * 
 * @author Rahul Mishra, LiquidHub
 *
 */
@ToString(includeNames=true, includePackage=false)
class ConfigurableJobSections {
	
	def scm,maven,trigger,email //The name MUST match the key in the configuration file
	
	/**
	 * Returns the provider instance for the specified section
	 * 
	 * @param sectionName
	 * 
	 * @return
	 */
	JobSectionConfigurer provider(sectionName){
		
		//This is the value configured against the key in the confinguration file
		def providerName = this[sectionName]
		
		//This is the provider instance being returned
		JobSectionConfigurers.valueOf(sectionName)
	}

}



