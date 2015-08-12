package com.liquidhub.framework.providers.jenkins

import com.liquidhub.framework.JobSectionConfigurer
import com.liquidhub.framework.ci.model.JobGenerationContext
import com.liquidhub.framework.config.model.JobConfig
import com.liquidhub.framework.scm.model.SCMRepository

class GenericSCMSectionConfigurer implements JobSectionConfigurer{


	Closure configure(JobGenerationContext context, JobConfig jobConfig, String branchToBuild=null, boolean ignoreCommitNotifications=false){
		
		SCMRepository scmRepository = context.scmRepository

		return { scm ->
			git {
				remote{
					url(scmRepository.repoUrl)
					credentials(context.scmCredentialsId) //This should point to an existing Credential Description
				}
				branchToBuild = branchToBuild ?: scmRepository.repoBranchName
				branch('*/'+branchToBuild) //Align to the ref spec
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
