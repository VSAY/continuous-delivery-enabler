package com.liquidhub.framework.config

import groovy.transform.ToString

import com.liquidhub.framework.model.CoreJobConfig
import com.liquidhub.framework.model.GitflowBranchingConfig

@ToString(includeNames=true)
class Configuration {
	
	CoreJobConfig continuousIntegrationConfig

	GitflowBranchingConfig gitflowFeatureBranchConfig,gitflowReleaseBranchConfig,gitflowHotfixBranchConfig
	
	def buildEnvProperties = [:] //A mix of system environment variables and build enviroment variables.

	private ConfigurationLevel level

	private static logger
	
	
	def merge(Configuration thatConfig){

		CoreJobConfig.logger = logger

		//An array of all job related configuration
		def jobKeyConfigs = [
			'continuousIntegrationConfig',
			'gitflowFeatureBranchConfig',
			'gitflowReleaseBranchConfig',
			'gitflowHotfixBranchConfig'
		]

		jobKeyConfigs.each{key ->
			logger.debug('Now processing '+key+ 'from configuration level as '+thatConfig.level)

			def thisJobConfig = this[key],thatJobConfig = thatConfig[key]


			/*
			 * If the configuration against this key exists
			 *   1. Check if the incoming configuration for the same key exists, If it does merge them
			 *   2. if it doesn't, retain the original config
			 *   3. If the incoming configuration for the key does not exist, switch to target no matter what it is 
			 */
			this[key] = thisJobConfig ? (thatJobConfig ? thisJobConfig.merge(thatJobConfig) : thisJobConfig) : thatJobConfig

			logger.debug('Now processed '+key+ 'from configuration level as '+thatConfig.level)
			logger.debug 'updated setting for this key is '+this[key]
		}

	}
	

}
