package com.liquidhub.framework.ci.job.generator.impl

import static com.liquidhub.framework.ci.model.JobPermissions.ItemBuild
import static com.liquidhub.framework.ci.model.JobPermissions.ItemCancel
import static com.liquidhub.framework.ci.model.JobPermissions.ItemConfigure
import static com.liquidhub.framework.ci.model.JobPermissions.ItemDiscover
import static com.liquidhub.framework.ci.model.JobPermissions.ItemRead
import static com.liquidhub.framework.ci.model.JobPermissions.ItemWorkspace
import static com.liquidhub.framework.ci.model.JobPermissions.RunDelete
import static com.liquidhub.framework.ci.model.JobPermissions.RunUpdate
import static com.liquidhub.framework.ci.view.ViewElementTypes.READ_ONLY_TEXT

import com.liquidhub.framework.ci.EmbeddedScriptProvider
import com.liquidhub.framework.ci.job.generator.JobGenerator
import com.liquidhub.framework.ci.logger.Logger
import com.liquidhub.framework.ci.model.DeploymentJobParameters
import com.liquidhub.framework.ci.model.JobGenerationContext
import com.liquidhub.framework.ci.model.ParameterListingScript
import com.liquidhub.framework.ci.model.SeedJobParameters
import com.liquidhub.framework.config.model.Configuration
import com.liquidhub.framework.config.model.DeploymentJobConfig
import com.liquidhub.framework.config.model.JobConfig
import com.liquidhub.framework.config.model.RoleConfig
import com.liquidhub.framework.model.MavenArtifact
import com.liquidhub.framework.providers.jenkins.JenkinsJobViewSupport
import com.liquidhub.framework.providers.maven.NexusRepositoryArtifactVersionListingScriptProvider


class DeploymentJobGenerator implements JobGenerator{


	private static Logger logger

	private static final String TARGET_SERVER_NAME = 'targetServer', MAVEN_ARTIFACT_VERSION='artifactVersion'

	private static final String ACTIVE_ENV_CONFIG = 'env'

	@Override
	public def generateJob(JobGenerationContext ctx) {

		logger = ctx.logger

		def deploymentConfiguration = ctx.configuration.deploymentConfig

		logger.debug('deployment configuration is '+deploymentConfiguration)


		if(shouldSkipGeneratingDeploymentJobs(ctx)){
			return []
		}

		def deploymentJobs = []

		MavenArtifact applicationConfiguration = ctx.configurationArtifact

		if(!applicationConfiguration){
			logger.warn('Missing Application Configuration Artifact : Assuming that application configuration will be managed outside automated deployments, this is highly discouraged and should be fixed')
		}else{
			logger.debug('Going to use application configuration as '+applicationConfiguration)
		}

		DeploymentJobConfig[]  deploymentJobConfigs = deploymentConfiguration.environments

		def parent = super

		deploymentJobConfigs.each{DeploymentJobConfig thisEnvironmentDeploymentConfig ->

			def deploymentJob = new DeploymentJobTemplate(thisEnvironmentDeploymentConfig).generateJob(ctx)

			deploymentJobs.add(deploymentJob)
		}
		return deploymentJobs;
	}




	protected def shouldSkipGeneratingDeploymentJobs(JobGenerationContext context){

		if(!context.hasDeployable()){
			logger.warn 'Could not figure out deployable, assuming this project does not need deployment capabilities'
			return true
		}

		if(!context.configuration.deploymentConfig.environments){
			//If no deployment environments are specified, we can't build deployment jobs
			logger.warn('Deployment environments have not been specified but the project is deployable, will not generate deployment jobs.')
			return true
		}
	}

	@Override
	public boolean supportsGitflow() {
		false;
	}



	private class DeploymentJobTemplate extends BaseJobGenerationTemplate{

		private DeploymentJobConfig environmentConfig

		DeploymentJobTemplate(DeploymentJobConfig environmentConfig){
			this.environmentConfig = environmentConfig
		}

		@Override
		def getJobConfig(Configuration configuration){
			environmentConfig
		}


		@Override
		protected def determineJobName(JobGenerationContext ctx,JobConfig jobConfig){
			ctx.scmRepository.repositorySlug+'-deployTo'+environmentConfig.name.capitalize()
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

			def targetClusterName = environmentConfig.targetClusterName
			def targetCellName=environmentConfig.targetCellName
			def contextRoot=environmentConfig.appContextRoot
			
			EmbeddedScriptProvider scriptProvider = deploymentConfig.getDeploymentArtifactListingProvider()

			def artifactVersionDescription = 'Pick the artifact version you intend to deploy. By default only the last few versions are displayed. If the drop down list is empty,  it implies that we could not find any released versions for the artifact you intend to deploy. Please contact the release manager'
			def mavenMetadataDownloadScript = scriptProvider.getScript([baseRepositoryUrl: deploymentConfig.artifactRepositoryUrl,groupId: mvnGroupId,artifactId: mvnArtifactId])
			def scriptBindings = [releaseVersionCountToDisplay: environmentConfig.releaseVersionCountToDisplay, snapshotVersionCountToDisplay: environmentConfig.snapshotVersionCountToDisplay]

			def deploymentJobParameters = [
				DeploymentJobParameters.GROUP_ID.properties << [defaultValue:  "'${mvnGroupId}'"],
				DeploymentJobParameters.ARTIFACT_ID.properties << [defaultValue: "'${mvnArtifactId}'"],
				DeploymentJobParameters.PACKAGING.properties << [defaultValue: "'${packaging}'"],
				DeploymentJobParameters.ARTIFACT_VERSION.properties << [valueListingScript: new ParameterListingScript(text: mavenMetadataDownloadScript, bindings: scriptBindings)],
				DeploymentJobParameters.TARGET_CLUSTER_NAME.properties << [defaultValue: "'${targetClusterName}'"],
				DeploymentJobParameters.TARGET_CELL_NAME.properties << [defaultValue: "'${targetCellName}'"],
				DeploymentJobParameters.APP_CONTEXT_ROOT.properties << [defaultValue: "'${contextRoot}'"],
				DeploymentJobParameters.REPLACEMENT_VERSION.properties
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
			
			return {
				shell(deploymentScript)
			}
		}
	}
}
