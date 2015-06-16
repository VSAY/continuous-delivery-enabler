package com.liquidhub.framework.providers.jenkins

import groovy.lang.Closure;

import com.liquidhub.framework.JobSectionConfigurer;
import com.liquidhub.framework.config.model.JobConfig;
import com.liquidhub.framework.model.JobGenerationContext;

class GenericSCMTriggerSectionConfigurer implements JobSectionConfigurer{

	@Override
	public Closure configure(JobGenerationContext context, JobConfig jobConfig) {

		if(jobConfig.pollSchedule){
			return { scm(jobConfig.pollSchedule)  }
		}else{
			return {scm('')} //Enable polling, but do not poll - This is required for us to trigger push notifications
		}
	}
}
