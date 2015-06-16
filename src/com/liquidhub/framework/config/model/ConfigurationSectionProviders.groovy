package com.liquidhub.framework.config.model

import com.liquidhub.framework.ConfigurationSectionProvider
import com.liquidhub.framework.scm.GenericSCMSectionConfigurer

/**
 * A registry of capability providers supported on the platform. All providers need to be registered here to be able to be used in this platform.
 * 
 * All providers must be stateless
 * 
 * 
 * @author Rahul Mishra, LiquidHub
 *
 */
enum ConfigurationSectionProviders {
	
	GENERIC_SCM_CONFIGURATION_PROVIDER(new GenericSCMSectionConfigurer()),
	MAVEN_SECTION_CONFIGURER
	
	ConfigurationSectionProviders(ConfigurationSectionProvider provider){
		this.provider = provider
	}

	def provider
}
