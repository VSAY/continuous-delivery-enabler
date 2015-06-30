package com.liquidhub.framework.config.model

import groovy.transform.ToString

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

	JobConfig continuousIntegrationConfig, codeQualityConfig, milestoneReleaseConfig

	GitflowBranchingConfig gitflowFeatureBranchConfig,gitflowReleaseBranchConfig,gitflowHotfixBranchConfig
	
	ConfigurableJobSections configurableJobSections

	DeploymentJobConfig deploymentConfig

	RoleConfig roleConfig

	Map notificationConfig, viewConfig , buildDiscardPolicy
	
	BuildConfig buildConfig

	List buildPipelinePreferences

	def buildEnvProperties = [:] //A mix of system environment variables and build enviroment variables.

	private ConfigurationLevel level
	
	private String jobSeederName
	
	SCMRepositoryConfigurationInstruction[] scmRepositoryConfigurationInstructions //We won't merge instructions for now. TODO Re-evaluate in future

	private static logger


	def merge(Configuration thatConfig){

		JobConfig.logger = logger

		//An array of all configuration settings, these values below MUST sync with field names above
		[
			'roleConfig',
			'codeQualityConfig',
			'continuousIntegrationConfig',
			'milestoneReleaseConfig',
			'gitflowFeatureBranchConfig',
			'gitflowReleaseBranchConfig',
			'gitflowHotfixBranchConfig',
			'deploymentConfig',
			'notificationConfig',
			'viewConfig',
			'buildPipelinePreferences',
			'configurableJobSections',
			'buildConfig',
			'buildDiscardPolicy',
			'jobSeederName'
		].each{
	
			def src = this[it]
			def target = thatConfig[it]

			this[it] = merge(src, target)

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
				return src instanceof Map ? src << target : src.merge(target)
			}else{
				return src
			}

		}else{
			return target
		}

	}





}
