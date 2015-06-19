package com.liquidhub.framework.config.model

import groovy.transform.ToString

/**
 * Represents the deployment specification as stated in configuration files. 
 * 
 * @author Rahul Mishra, LiquidHub
 *
 */
@ToString(includeNames=true)
class DeploymentJobConfig extends JobConfig {

	def name //A logical name to the deployment config, should be the environment name when used as environments

	def servers //An array of servers for the deployment environment

	private def deploymentScript //The location of the deployment script which can be used to trigger deployments

	private def artifactRepositoryUrl //The URL from where the deployment artifacts can be downloaded

	private def releaseVersionCountToDisplay //The number of release artifacts to display when making a deployment choice

	private def snapshotVersionCountToDisplay //The number of snapshot artifacts to display when making a deployment choice

	private def appConfigurationArtifactIdentificationPattern //The pattern by which we identify the configuration artifact for the deployable

	private DeploymentJobConfig parentConfig //A reference to the parent deployment configuration

	DeploymentJobConfig [] environments //An array of environments 

	/**
	 * Merges this deployment configuration with the specified configuration
	 * 
	 * @param thatDeploymentConfig The target deployment configuration
	 * 
	 * @return the merged configuration
	 */
	def merge(DeploymentJobConfig thatDeploymentConfig){

		if(thatDeploymentConfig.environments){
			environments.each{environment ->
				environment.merge(thatDeploymentConfig[environment.name]) //Merge environment information as required
				environment.parentConfig = this
			}
		}

		super.merge(thatDeploymentConfig)

		[
			//These are the properties which are specific to deployment configuration
			'name',
			'servers',
			'deploymentScript',
			'artifactRepositoryUrl',
			'releaseVersionCountToDisplay',
			'snapshotVersionCountToDisplay',
			'appConfigurationArtifactIdentificationPattern'
		].each{property ->
			this[property] = thatDeploymentConfig[property] ?:  this[property]
		}

	}

	def deploymentScript(){
		inheritFromParentIfNecessary deploymentScript
	}

	def artifactRepositoryUrl(){
		inheritFromParentIfNecessary artifactRepositoryUrl
	}

	def releaseVersionCountToDisplay(){
		inheritFromParentIfNecessary releaseVersionCountToDisplay
	}

	def snapshotVersionCountToDisplay(){
		inheritFromParentIfNecessary snapshotVersionCountToDisplay
	}

	def appConfigurationArtifactIdentificationPattern(){
		inheritFromParentIfNecessary appConfigurationArtifactIdentificationPattern
	}

	protected def inheritFromParentIfNecessary(property){
		return this[property] ?: parentConfig ? this.parentConfig[property] : null
	}


}
