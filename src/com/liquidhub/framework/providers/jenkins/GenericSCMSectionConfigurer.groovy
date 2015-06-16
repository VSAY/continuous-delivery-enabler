package com.liquidhub.framework.providers.jenkins

import com.liquidhub.framework.JobSectionConfigurer
import com.liquidhub.framework.config.model.JobConfig
import com.liquidhub.framework.model.JobGenerationContext
import com.liquidhub.framework.scm.model.SCMRepository

class GenericSCMSectionConfigurer implements JobSectionConfigurer{


	Closure configure(JobGenerationContext context, JobConfig jobConfig, boolean ignoreCommitNotifications=false){
		
		SCMRepository scmRepository = context.scmRepository

		return { scm ->
			git {
				remote{
					url(scmRepository.repoUrl)
					credentials(scmRepository.credentialsId) //This should point to an existing Credential Description
				}
				branch('*/'+scmRepository.repoBranchName) //Align to the ref spec
				browser {
					stash(scmRepository.changeSetUrl) // URL to the Stash repository, optional
				}
				configure { node ->
					node/'extensions' << 'hudson.plugins.git.extensions.impl.CleanBeforeCheckout'()

					if(ignoreCommitNotifications){
						node/'extensions' << 'hudson.plugins.git.extensions.impl.IgnoreNotifyCommit'()
					}
				}
			}
		}
	}
}
