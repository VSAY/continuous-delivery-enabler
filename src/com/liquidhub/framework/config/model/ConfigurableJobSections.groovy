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
@ToString(includeNames=true)
class ConfigurableJobSections {
	
	def scm,maven,trigger,email,os //The name MUST match the key in the configuration file
	
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
		
		if(!providerName){
			throw new RuntimeException('No provider has been configured for the ['+sectionName+'] section. Add a JobSectionConfigurer for this section against the "configurableJobSections" property')
		}
		
		//This is the provider instance being returned
		JobSectionConfigurers.valueOf(providerName)?.provider
	}

}



