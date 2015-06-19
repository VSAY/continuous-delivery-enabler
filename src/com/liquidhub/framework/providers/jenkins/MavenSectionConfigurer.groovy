package com.liquidhub.framework.providers.jenkins

import groovy.lang.Closure

import com.liquidhub.framework.JobSectionConfigurer;
import com.liquidhub.framework.ci.model.JobGenerationContext;
import com.liquidhub.framework.config.model.JobConfig;

class MavenSectionConfigurer implements JobSectionConfigurer {

	@Override
	public Closure configure(JobGenerationContext context, JobConfig jobConfig) {
		
		def mvn = context.buildToolConfig.maven
		def action = jobConfig.goals
		def commandProperties = jobConfig.goalArgs
		def pomFile = 'pom.xml'
		
		return { maven ->
			mavenInstallation(mvn.name)
			providedSettings(mvn.settings)
			goals(action)
			rootPOM(pomFile)
			if(commandProperties){
				properties(commandProperties)
			}
		}
	}
}
