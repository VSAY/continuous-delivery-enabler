package com.liquidhub.framework.ci.job.generator.impl

import static com.liquidhub.framework.ci.model.JobPermissions.ItemBuild
import static com.liquidhub.framework.ci.model.JobPermissions.ItemCancel
import static com.liquidhub.framework.ci.model.JobPermissions.ItemWorkspace
import static com.liquidhub.framework.ci.model.JobPermissions.RunDelete
import static com.liquidhub.framework.ci.model.JobPermissions.RunUpdate

import com.liquidhub.framework.ci.model.BuildEnvironmentVariables
import com.liquidhub.framework.ci.model.JobGenerationContext
import com.liquidhub.framework.config.model.Configuration
import com.liquidhub.framework.config.model.JobConfig
import com.liquidhub.framework.config.model.RoleConfig
import com.liquidhub.framework.scm.model.GitFlowBranchTypes

/**
 * Generates the continuous integration job for a Repository Branch
 * 
 * @author Rahul Mishra, LiquidHub
 *
 */
class ContinuousIntegrationJobGenerator extends BaseJobGenerationTemplate{

	/**
	 * Return the configuration element against which the CI job configuration is made
	 */
	def getJobConfig(Configuration configuration){
		configuration.continuousIntegrationConfig
	}




	/**
	 * Return false to indicate that this job must be fired on every commit/commit notifications MUST never be ignored
	 */

	protected def ignoreCommitNotifications(){
		false
	}

	protected def configureAdditionalPublishers(JobGenerationContext ctx, JobConfig jobConfig){

		JobConfig downstreamJobConfig
		JobConfig downstreamDeployConfig
		Configuration masterConfig = ctx.configuration

		/*
		 * The flow we are trying to build
		 * 
		 * feature ci > feature finish > develop ci > release start > release ci > release finish > master health
		 * 
		 */

		switch(ctx.scmRepository.branchType){

			case GitFlowBranchTypes.FEATURE:
				downstreamJobConfig = masterConfig.gitflowFeatureBranchConfig.finishConfig
				break

			case GitFlowBranchTypes.RELEASE:
				downstreamJobConfig=masterConfig.gitflowReleaseBranchConfig.finishConfig
				break

			case GitFlowBranchTypes.HOTFIX:
				downstreamJobConfig= masterConfig.gitflowHotfixBranchConfig.finishConfig
				break

			case GitFlowBranchTypes.DEVELOP:
				downstreamJobConfig= masterConfig.gitflowReleaseBranchConfig.startConfig
				break

			case GitFlowBranchTypes.MASTER://Breaking this connection for now, affects pipeline visualization
				//downstreamJobConfig= masterConfig.gitflowHotfixBranchConfig.startConfig
				break
		}




		return {
			stashNotifier()
			if(downstreamJobConfig){
				def downstreamJobName = ctx.jobNameCreator.createJobName(ctx.repositoryName, null, null, downstreamJobConfig, true)
				buildPipelineTrigger(downstreamJobName)
			}
		}
	}

	protected def determineRegularEmailSubject(JobGenerationContext ctx,JobConfig jobConfig){
		//We know that project version is created by the post build script attached by the CI Job
		BuildEnvironmentVariables.PROJECT_NAME.paramValue+' - Version # ${PROJECT_VERSION} - '+ BuildEnvironmentVariables.BUILD_STATUS.paramValue+'!'
	}


	/**
	 * Return job specific permissions, since this is a CI job we want developers to have all sorts of access on the job (except the capability to change configuration) 
	 */
	protected Map grantAdditionalPermissions(JobGenerationContext ctx, RoleConfig roleConfig){
		[(roleConfig.developerRole):[ItemBuild, ItemCancel, RunUpdate, RunDelete, ItemWorkspace]]
	}
}