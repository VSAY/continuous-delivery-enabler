package com.liquidhub.framework.ci.job.generator.impl


import com.liquidhub.framework.ci.job.deployment.TomcatDeploymentTemplate
import com.liquidhub.framework.ci.job.deployment.WebSphereDeploymentTemplate
import com.liquidhub.framework.ci.job.generator.JobGenerator
import com.liquidhub.framework.ci.logger.Logger
import com.liquidhub.framework.ci.model.JobGenerationContext
import com.liquidhub.framework.config.model.DeploymentJobConfig
import com.liquidhub.framework.model.MavenArtifact


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

			switch(thisEnvironmentDeploymentConfig.serverTemplate){

				case 'WEBSPHERE':
					deploymentJobs << new WebSphereDeploymentTemplate(thisEnvironmentDeploymentConfig).generateJob(ctx)
					break

				case 'TOMCAT':
					deploymentJobs << new TomcatDeploymentTemplate(thisEnvironmentDeploymentConfig).generateJob(ctx)
					break
					
			    default:
				 logger.warn('The framework does not support '+thisEnvironmentDeploymentConfig.serverTemplate+' deployment template yet')
				 break
			}
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
}
