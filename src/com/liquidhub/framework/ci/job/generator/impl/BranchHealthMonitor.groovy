package com.liquidhub.framework.ci.job.generator.impl

import com.liquidhub.framework.config.model.Configuration;

class BranchHealthMonitor extends ContinuousIntegrationJobGenerator{

	/**
	 * Return the configuration element against which the CI job configuration is made
	 */
	def getJobConfig(Configuration configuration){
		configuration.healthConfig
	}

}
