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
		return {
			stashNotifier()
		}
	}

	protected def determineEmailSubject(JobGenerationContext ctx,JobConfig jobConfig){
		//We know that project version is created by the post build script attached by the CI Job
		BuildEnvironmentVariables.PROJECT_NAME.paramValue+' - Version # ${PROJECT_VERSION} - '+ BuildEnvironmentVariables.BUILD_STATUS.paramValue+'!'
	}


	/**
	 * Return job specific permissions, since this is a CI job we want developers to have all sorts of access on the job (except the capability to change configuration) 
	 */
	protected Map grantAdditionalPermissions(JobGenerationContext ctx, RoleConfig roleConfig){
		[(roleConfig.developerRole):[
				ItemBuild,
				ItemCancel,
				RunUpdate,
				RunDelete,
				ItemWorkspace
			]]
	}
}