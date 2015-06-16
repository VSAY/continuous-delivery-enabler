package com.liquidhub.framework.config.model

import com.liquidhub.framework.JobSectionConfigurer
import com.liquidhub.framework.providers.jenkins.ExtendedEmailNotificationSectionConfigurer;
import com.liquidhub.framework.providers.jenkins.GenericSCMSectionConfigurer
import com.liquidhub.framework.providers.jenkins.GenericSCMTriggerSectionConfigurer


/**
 * A registry of capability providers supported on the platform. All providers need to be registered here to be able to be used in this platform.
 * 
 * All providers must be stateless
 * 
 * 
 * @author Rahul Mishra, LiquidHub
 *
 */
enum JobSectionConfigurers {

	GENERIC_SCM_SECTION_CONFIGURER(new GenericSCMSectionConfigurer()),
	MAVEN_SECTION_CONFIGURER,
	GENERIC_SCM_TRIGGER_SECTION_CONFIGURER(new GenericSCMTriggerSectionConfigurer()),
	EXTENDED_EMAIL_SECTION_CONFIGURER(new ExtendedEmailNotificationSectionConfigurer())

	JobSectionConfigurers(JobSectionConfigurer provider){
		this.provider = provider
	}

	def provider
}
