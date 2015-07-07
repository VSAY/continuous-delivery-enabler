package com.liquidhub.framework.config

import com.liquidhub.framework.config.model.Configuration

/**
 * Loads the configuration required for the job generation framework. 
 * 
 * @author Rahul Mishra, LiquidHub
 *
 */

interface ConfigurationManager {

	Configuration loadConfigurationForRepositoryBranch(repositoryName, repositoryType, branchName)
}
