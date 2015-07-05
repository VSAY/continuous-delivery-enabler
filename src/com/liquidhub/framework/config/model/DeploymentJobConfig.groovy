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

	def deploymentScriptPath //The location of the deployment script which can be used to trigger deployments

	def artifactRepositoryUrl //The URL from where the deployment artifacts can be downloaded

	def releaseVersionCountToDisplay //The number of release artifacts to display when making a deployment choice

	def snapshotVersionCountToDisplay //The number of snapshot artifacts to display when making a deployment choice

	def appConfigurationArtifactIdentificationPattern //The pattern by which we identify the configuration artifact for the deployable

	def targetClusterName,targetCellName, appContextRoot

	private DeploymentJobConfig parentConfig //A reference to the parent deployment configuration

	DeploymentJobConfig [] environments //An array of environments

	boolean enforceRoleBasedAccess=true //By default all environment deployments have restricted access unless overriden

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

	/**
	 * Sets the environment specific deployment configurations into this parent configuration.
	 * Allows us to set the parent-child relationship of deployment configurations
	 * 
	 * @param environments
	 * 
	 * @return
	 */
	public def setEnvironments(DeploymentJobConfig[] environments){
		environments.each{ it.parentConfig = this}
		this.environments=environments
	}

	/**
	 * Generic interceptor for all property access.
	 * 
	 * Since a deployment configuration is a nested hierarchy, this interception allows us to look up the property through the hierarchy if the property 
	 * is not specified at a child level
	 * 
	 * @param name the name of the property being accessed
	 * 
	 * @return the value of the property being accessed
	 */
	public def getProperty(String name){
		def propertyName = name.capitalize()
		def propertyValue = "get$propertyName"()
		propertyValue ?: parentConfig ? parentConfig[name]:null
	}




}
