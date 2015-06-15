package com.liquidhub.framework.config

import groovy.transform.ToString

import com.liquidhub.framework.model.CoreJobConfig
import com.liquidhub.framework.model.DeploymentJobConfig
import com.liquidhub.framework.model.GitflowBranchingConfig
import com.liquidhub.framework.model.RoleConfig

@ToString(includeNames=true)
/**
 * Represents the job generation related configuration elements. 
 * 
 * @author Rahul Mishra, LiquidHub
 *
 */
class Configuration {
	
	/*
	 * Developer Note: All the property names below MUST correspond to the key in the corresponding configuration file.
	 * 
	 * Changes on both sides MUST be made in sync otherwise properties will not load correctly
	 * 
	 */

	CoreJobConfig continuousIntegrationConfig, codeQualityConfig

	GitflowBranchingConfig gitflowFeatureBranchConfig,gitflowReleaseBranchConfig,gitflowHotfixBranchConfig

	DeploymentJobConfig deploymentConfig

	RoleConfig roleConfig

	private Map notificationConfig, viewConfig, buildConfig
	
	private List buildPipelinePreferences

	def buildEnvProperties = [:] //A mix of system environment variables and build enviroment variables.

	private ConfigurationLevel level

	private static logger


	def merge(Configuration thatConfig){

		CoreJobConfig.logger = logger

		//An array of all configuration settings, these values below MUST sync with field names above
		[
			'roleConfig',
			'codeQualityConfig',
			'continuousIntegrationConfig',
			'gitflowFeatureBranchConfig',
			'gitflowReleaseBranchConfig',
			'gitflowHotfixBranchConfig',
			'deploymentConfig',
			'notificationConfig',
			'viewConfig',
			'buildPipelinePreferences'
		].each{
			this[it] = merge(this[it], thatConfig[it])

			logger.debug it +' @ '+ thatConfig.level+': '+this[it]
		}

	}

	protected def merge(src, target){

		/*
		 * If the src exists
		 *   1. Check if the target exists, If it does merge them. When it's a map push, when its an object 'expect' a merge API
		 *   2. if it doesn't, retain the original config
		 *   3. If the src does not exist, switch to target no matter what it is
		 */

		if(src){
			if(target){
				src instanceof Map ? src << target : src.merge(target)
			}else{
				src
			}

		}else{
			target
		}

	}

	

}
