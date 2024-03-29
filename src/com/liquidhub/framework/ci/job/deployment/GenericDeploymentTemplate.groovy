package com.liquidhub.framework.ci.job.deployment

import static com.liquidhub.framework.ci.model.JobPermissions.ItemBuild
import static com.liquidhub.framework.ci.model.JobPermissions.ItemCancel
import static com.liquidhub.framework.ci.model.JobPermissions.ItemConfigure
import static com.liquidhub.framework.ci.model.JobPermissions.ItemDiscover
import static com.liquidhub.framework.ci.model.JobPermissions.ItemRead
import static com.liquidhub.framework.ci.model.JobPermissions.ItemWorkspace
import static com.liquidhub.framework.ci.model.JobPermissions.RunDelete
import static com.liquidhub.framework.ci.model.JobPermissions.RunUpdate
import static com.liquidhub.framework.ci.view.ViewElementTypes.READ_ONLY_TEXT

import com.liquidhub.framework.ci.job.generator.impl.BaseJobGenerationTemplate
import com.liquidhub.framework.ci.model.JobGenerationContext
import com.liquidhub.framework.ci.model.SeedJobParameters
import com.liquidhub.framework.config.model.Configuration
import com.liquidhub.framework.config.model.DeploymentJobConfig
import com.liquidhub.framework.config.model.JobConfig
import com.liquidhub.framework.config.model.RoleConfig

class GenericDeploymentTemplate extends BaseJobGenerationTemplate{

	protected DeploymentJobConfig environmentConfig
	
	@Override
	def getJobConfig(Configuration configuration){
		environmentConfig
	}


	@Override
	protected def requiresSCMConfiguration(){
		false
	}

	@Override
	protected def requiresTriggerConfiguration(){
		false
	}


	/**
	 * Grant the deployment manager all sort of permissions on the deployment job, except the permission to configure it
	 */
	protected Map grantAdditionalPermissions(JobGenerationContext ctx,RoleConfig roleConfig){
		if(environmentConfig.enforceRoleBasedAccess){
			[(roleConfig.deploymentManagerRole):  [ItemBuild, ItemCancel, ItemDiscover, ItemRead, RunUpdate, ItemWorkspace]]
		}
	}


	@Override
	protected def determineRegularEmailSubject(JobGenerationContext ctx, JobConfig jobConfig){
		'${ENV, var="artifactId"} Version ${ENV, var="version"} deployed to '+this.environmentConfig.name.toUpperCase()+' environment'
	}

	@Override
	protected def determineFailureEmailSubject(JobGenerationContext ctx, JobConfig jobConfig){
		'Action Required !!! Failed to deploy ${ENV, var="artifactId"} Version ${ENV, var="version"} to '+this.environmentConfig.name.toUpperCase()+' environment'
	}


	protected def configureDescription(JobGenerationContext ctx,JobConfig jobConfig){

		def templateArgs = [:] << ctx.deployable.properties << ['envName':environmentConfig.name]

		if(environmentConfig.projectDescriptionTemplatePath){
			ctx.templateEngine.withContentFromTemplatePath(environmentConfig.projectDescriptionTemplatePath, templateArgs)
		}
	}
	
	/**
	 * Configure the build steps for this job, by default we assume a maven step and directly use the goals configured
	 *
	 *
	 * @param ctx
	 * @param jobConfig
	 *
	 * @return
	 */
	@Override
	protected def configureSteps(JobGenerationContext ctx, JobConfig jobConfig){

		def deploymentScriptPath = [ctx.getVariable(SeedJobParameters.FRAMEWORK_CONFIG_BASE_MOUNT), environmentConfig.deploymentScriptPath].join(File.separator)

		def deploymentScript = ctx.workspaceUtils.fileReader(deploymentScriptPath)

		return { shell(deploymentScript) }
	}

	protected def extractPOMVersionAfterBuild(){
		false
	}
}
