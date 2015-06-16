package com.liquidhub.framework.config.model

import com.liquidhub.framework.ConfigurationSectionProvider

/**
 * An exhaustive list of configuration sections for which we allow provider configuration
 * 
 * 
 * @author Rahul Mishra, LiquidHub
 *
 */
class ConfigurationSections {
	
	private def scm,maven //The name MUST match the key in the configuration file
	
	ConfigurationSectionProvider provider(providerKey){
		
		//This is the value configured against the key in the confinguration file
		def providerName = this[providerKey]
		
		//This is the provider instance being returned
		ConfigurationSectionProviders.valueOf(providerName)
	}

}



