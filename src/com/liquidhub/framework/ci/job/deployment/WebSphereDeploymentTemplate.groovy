package com.liquidhub.framework.ci.job.deployment

import com.liquidhub.framework.ci.EmbeddedScriptProvider
import com.liquidhub.framework.ci.job.generator.impl.BaseJobGenerationTemplate
import com.liquidhub.framework.ci.model.WebsphereDeploymentJobParameters
import com.liquidhub.framework.ci.model.JobGenerationContext
import com.liquidhub.framework.ci.model.ParameterListingScript
import com.liquidhub.framework.ci.model.SeedJobParameters
import com.liquidhub.framework.config.model.Configuration
import com.liquidhub.framework.config.model.DeploymentJobConfig
import com.liquidhub.framework.config.model.JobConfig
import com.liquidhub.framework.config.model.RoleConfig
import com.liquidhub.framework.providers.jenkins.JenkinsJobViewSupport
import static com.liquidhub.framework.ci.model.JobPermissions.ItemBuild
import static com.liquidhub.framework.ci.model.JobPermissions.ItemCancel
import static com.liquidhub.framework.ci.model.JobPermissions.ItemConfigure
import static com.liquidhub.framework.ci.model.JobPermissions.ItemDiscover
import static com.liquidhub.framework.ci.model.JobPermissions.ItemRead
import static com.liquidhub.framework.ci.model.JobPermissions.ItemWorkspace
import static com.liquidhub.framework.ci.model.JobPermissions.RunDelete
import static com.liquidhub.framework.ci.model.JobPermissions.RunUpdate
import static com.liquidhub.framework.ci.view.ViewElementTypes.READ_ONLY_TEXT


class WebSphereDeploymentTemplate extends BaseJobGenerationTemplate{


	private DeploymentJobConfig environmentConfig

	WebSphereDeploymentTemplate(DeploymentJobConfig environmentConfig){
		this.environmentConfig = environmentConfig
	}

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


	protected def configureDescription(JobGenerationContext ctx,JobConfig jobConfig){

		def templateArgs = [:] << ctx.deployable.properties << ['envName':environmentConfig.name]

		if(environmentConfig.projectDescriptionTemplatePath){
			ctx.templateEngine.withContentFromTemplatePath(environmentConfig.projectDescriptionTemplatePath, templateArgs)
		}
	}


	protected def configureJobParameterExtensions(JobGenerationContext ctx, JobConfig jobConfig){

		def parameters = []

		def mvnGroupId = ctx.deployable.groupId
		def mvnArtifactId = ctx.deployable.artifactId
		def packaging = ctx.deployable.packaging

		JenkinsJobViewSupport.logger = ctx.logger


		DeploymentJobConfig deploymentConfig = ctx.configuration.deploymentConfig

		ctx.logger.debug 'environment config is '+environmentConfig

		def targetJVMName = environmentConfig.targetJVMName
		def targetCellName=environmentConfig.targetCellName
		def contextRoot=environmentConfig.appContextRoot
		def deploymentManager = environmentConfig.deploymentManager

		EmbeddedScriptProvider scriptProvider = deploymentConfig.getDeploymentArtifactListingProvider()

		def artifactVersionDescription = 'Pick the artifact version you intend to deploy. By default only the last few versions are displayed. If the drop down list is empty,  it implies that we could not find any released versions for the artifact you intend to deploy. Please contact the release manager'
		def mavenMetadataDownloadScript = scriptProvider.getScript([baseRepositoryUrl: deploymentConfig.artifactRepositoryUrl,groupId: mvnGroupId,artifactId: mvnArtifactId])
		def scriptBindings = [releaseVersionCountToDisplay: environmentConfig.releaseVersionCountToDisplay, snapshotVersionCountToDisplay: environmentConfig.snapshotVersionCountToDisplay]

		def deploymentJobParameters = [
			WebsphereDeploymentJobParameters.GROUP_ID.properties << [defaultValue:  "'${mvnGroupId}'"],
			WebsphereDeploymentJobParameters.ARTIFACT_ID.properties << [defaultValue: "'${mvnArtifactId}'"],
			WebsphereDeploymentJobParameters.PACKAGING.properties << [defaultValue: "'${packaging}'"],
			WebsphereDeploymentJobParameters.ARTIFACT_VERSION.properties << [valueListingScript: new ParameterListingScript(text: mavenMetadataDownloadScript, bindings: scriptBindings)],
			WebsphereDeploymentJobParameters.DEPLOYMENT_MANAGER.properties <<  [defaultValue: "'${deploymentManager}'"],
			WebsphereDeploymentJobParameters.TARGET_JVM_NAME.properties << [defaultValue: "'${targetJVMName}'"],
			WebsphereDeploymentJobParameters.TARGET_CELL_NAME.properties << [defaultValue: "'${targetCellName}'"],
			WebsphereDeploymentJobParameters.APP_CONTEXT_ROOT.properties << [defaultValue: "'${contextRoot}'"],
			WebsphereDeploymentJobParameters.RESTART.properties << [defaultValue:true]
		]

		def parameterDefinitions = {}
		deploymentJobParameters.reverse().each{ parameterDefinitions = parameterDefinitions <<  ctx.viewHelper.defineParameter(it) }

		return parameterDefinitions
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
}
