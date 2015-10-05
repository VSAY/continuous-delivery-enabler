package com.liquidhub.framework.config.model

import groovy.transform.ToString

import com.liquidhub.framework.ci.logger.Logger
import com.liquidhub.framework.ci.model.MavenArtifactRepositoryRegistry

/**
 * Represents the deployment specification as stated in configuration files. 
 * 
 * @author Rahul Mishra, LiquidHub
 *
 */
@ToString(includeNames=true)
class DeploymentJobConfig extends JobConfig {

	def name //A logical name to the deployment config, should be the environment name when used as environments
	
	def serverTemplate //the deployment job template to be used. Allows us to support different servers on a per environment basis for the same app

	def servers //An array of servers for the deployment environment

	def deploymentScriptPath //The location of the deployment script which can be used to trigger deployments

	def artifactRepository //The Artifact Repository - as supported by the framework. Any value configured here MUST be included in MavenArtifactRepositoryRegistry

	def artifactRepositoryUrl //The URL from where the deployment artifacts can be downloaded

	def releaseVersionCountToDisplay //The number of release artifacts to display when making a deployment choice

	def snapshotVersionCountToDisplay //The number of snapshot artifacts to display when making a deployment choice

	def appConfigurationArtifactIdentificationPattern //The pattern by which we identify the configuration artifact for the deployable

	def targetJVMName,targetCellName, appContextRoot,deploymentManager,deployDirPath,deploymentHost

	private DeploymentJobConfig parentConfig //A reference to the parent deployment configuration

	DeploymentJobConfig [] environments //An array of environments

	boolean enforceRoleBasedAccess=true //By default all environment deployments have restricted access unless overriden
	
	def applyFeatureVersionExclusionFilter //Determines if we should exclude displaying feature versions in the version listing

	static Logger logger


	/**
	 * Merges this deployment configuration with the specified configuration
	 * 
	 * @param thatDeploymentConfig The target deployment configuration
	 * 
	 * @return the merged configuration
	 */
	def merge(DeploymentJobConfig deploymentConfig){

		if(this.environments && deploymentConfig.environments){ //If we have preconfigured environment information and incoming environment configuration

			deploymentConfig.environments.each{environment ->

				if(this.environments[environment.name]){
					this.environments[environment.name].merge(deploymentConfig[environment.name]) //Merge corresponding environments
				}else{
					this.environments[environment.name]= deploymentConfig[environment.name] //The incoming environment IS this environment, no preexisting config
				}
			}

		}else if(deploymentConfig.environments){ //If we do not have any environment awareness yet, inherit all environments from the incoming configuration
			setEnvironments(deploymentConfig.environments)
		}

		super.merge(deploymentConfig)

		[
			//These are the properties which are specific to deployment configuration
			'name',
			'targetJVMName',
			'serverTemplate',
			'targetCellName',
			'appContextRoot',
			'deploymentManager',
			'deploymentScriptPath',
			'artifactRepositoryUrl',
			'releaseVersionCountToDisplay',
			'snapshotVersionCountToDisplay',
			'appConfigurationArtifactIdentificationPattern',
			'deployDirPath',
			'deploymentHost',
			'applyFeatureVersionExclusionFilter'
		].each{property ->
			this[property] = deploymentConfig[property] ?:  this[property]
		}

		return this
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


	public def getDeploymentArtifactListingProvider(){
		try{
			def mavenArtifactRepository = MavenArtifactRepositoryRegistry.valueOf(artifactRepository)
			mavenArtifactRepository.instance
		}catch(IllegalArgumentException iae){
			throw new RuntimeException('Illegal configuration value for #{deploymentConfig.artifactRepository}. Expected one of '+MavenArtifactRepositoryRegistry.values()+' are allowed but found '+artifactRepository)
		}
	}
}



