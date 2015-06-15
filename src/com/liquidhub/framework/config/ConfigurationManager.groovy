package com.liquidhub.framework.config

import com.liquidhub.framework.config.model.Configuration

interface ConfigurationManager {

	Configuration loadConfigurationForRepositoryBranch(projectName, branchName)
}
