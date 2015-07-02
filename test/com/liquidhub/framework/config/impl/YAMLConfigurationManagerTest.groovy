package com.liquidhub.framework.config.impl;

import static org.junit.Assert.*

import org.junit.Test

import com.liquidhub.framework.ci.logger.PrintStreamLogger
import com.liquidhub.framework.config.ConfigurationManager
import com.liquidhub.framework.config.JobGenerationWorkspaceUtils
import com.liquidhub.framework.config.model.Configuration

class YAMLConfigurationManagerTest {

	@Test
	public void loadConfigurationForRepositoryBranch() {

		def workspaceUtils = {
			fileReader : {
				def absoluteFilePath = [System.getProperty('user.dir'), it].join(File.separator)
				
				new BufferedReader(new InputStreamReader(new FileInputStream(absoluteFilePath)))
			}
		} as JobGenerationWorkspaceUtils

		//println fileReader.readFile('core/default-project-settings.yml')

		def variables = [:]
		//variables[SeedJobParameters.FRAMEWORK_CONFIG_BASE_MOUNT.bindingName]='resources'

        YAMLConfigurationManager.logger = new PrintStreamLogger(System.out) 
		ConfigurationManager configurationManager = new YAMLConfigurationManager(workspaceUtils: workspaceUtils, buildEnvVars : variables)
		
		Configuration configuration = configurationManager.loadConfigurationForRepositoryBranch('parent-pom','master')
		
		println '-----'+configuration

	}

	@Test
	public void testLoadConfigFrom() {
		//	fail("Not yet implemented");
	}
}
