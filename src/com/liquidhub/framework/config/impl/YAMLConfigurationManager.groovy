package com.liquidhub.framework.config.impl

import com.esotericsoftware.yamlbeans.YamlReader
import com.liquidhub.framework.ci.logger.Logger
import com.liquidhub.framework.ci.model.SeedJobParameters
import com.liquidhub.framework.config.ConfigurationManager
import com.liquidhub.framework.config.JobGenerationWorkspaceUtils
import com.liquidhub.framework.config.model.Configuration
import com.liquidhub.framework.config.model.ConfigurationLevel
/***
 * YAML based implementation of configuration managament.
 *
 * The configuration files are managed with a '.yml' extension. Configuration files can be provided at three levels
 *
 *  - Default Project Level [This is provided by the framework, this file must exist for the framework to generate jobs properly]
 *  - Repository Level [Provided by application teams, not mandatory but can be helpful for repository level customization]
 *  - Branch Level [Provided by application teams, not mandatory but can be helpful for repository branch level customization]
 *
 * @author Rahul Mishra, LiquidHub
 *
 */
class YAMLConfigurationManager implements ConfigurationManager{

	static Logger logger

	static CONFIG_FILE_EXTN='.yml'

	JobGenerationWorkspaceUtils workspaceUtils

	Map buildEnvVars

	private Configuration masterConfiguration

	/**
	 * Loads the configuration for the specified project , branch combination
	 *
	 *
	 */

	//@Memoized
	public Configuration loadConfigurationForRepositoryBranch(repositoryName, repositoryType, branchName) {

		Configuration.logger = logger

		logger.debug 'Enter.loadConfigurationForRepositoryBranch() >> '

		masterConfiguration = new Configuration(buildEnvProperties: [:] << System.getenv() << buildEnvVars, level: ConfigurationLevel.DEFAULT)

		//The mount locations of core configuration and target app - relative to the job generation workspace
		def configBaseMount = buildEnvVars[SeedJobParameters.FRAMEWORK_CONFIG_BASE_MOUNT.bindingName]
		def targetAppBaseMount  = buildEnvVars[SeedJobParameters.TARGET_PROJECT_BASE_MOUNT.bindingName]

		logger.debug('configurations are mounted at "'+configBaseMount+'"')
		logger.debug('target app is mounted at "'+targetAppBaseMount+'"')

		//Load the base configuration

		def defaultConfigFilePath = [configBaseMount, ConfigFiles.DEFAULT_PROJECT_SETTINGS.filePath].findAll().join(File.separator)
		def applicationSettingsDir =  [configBaseMount, ConfigFiles.APPLICATION_SETTINGS_DIR.filePath, repositoryName].findAll().join(File.separator)
		def repositoryConfigFilePath = [applicationSettingsDir, repositoryName+CONFIG_FILE_EXTN].findAll().join(File.separator)

		def repoBranchName = branchName.replace("/","-") //If its a gitflow branch it will be named as feature/*, so convert it to a hyphenated name
		def branchConfigFilePath = [applicationSettingsDir, repositoryName+'-'+repoBranchName+CONFIG_FILE_EXTN].join(File.separator)

		updateMasterConfig(defaultConfigFilePath,ConfigurationLevel.DEFAULT, true) //Load default
		updateMasterConfig(repositoryConfigFilePath, ConfigurationLevel.REPOSITORY) //Override with repository level settings
		updateMasterConfig(branchConfigFilePath, ConfigurationLevel.BRANCH) //Override with branch level settings

		def repositorySettingsFilePath = [configBaseMount, ConfigFiles.REPOSITORY_SETTINGS.filePath].findAll().join(File.separator)

		masterConfiguration.scmRepositoryConfigurationInstructions = this.loadConfiguration(repositorySettingsFilePath).scmRepositoryConfigurationInstructions
       
		return masterConfiguration
       
	}




	protected updateMasterConfig(settingsFile, configurationLevel, failOnError=false){

		Configuration loadedConfiguration

		try{
			loadedConfiguration = loadConfiguration(settingsFile)
		}catch(Exception e){
			if(failOnError){
				logger.warn 'Missing MANDATORY configuration file "'+settingsFile+'"'
				//logger.warn e.message
				e.printStackTrace()
			}else{
				logger.debug 'Did not find configuration file "'+settingsFile+'". The file was not provided, will use higher level defaults'
			}
		}


		if(!loadedConfiguration){
			logger.debug 'No configuration found at '+configurationLevel+' configuration level'
			return
		}
		loadedConfiguration.level = configurationLevel
		masterConfiguration.merge(loadedConfiguration)


	}


	/**
	 * Loads the provided file into configuration into a YAML Beans Object
	 * 
	 * @param filePath
	 * 
	 * @return
	 */
	protected Configuration loadConfiguration(filePath){

		YamlReader reader = null
		try {

			logger.debug 'Trying to read settings from '+filePath

			def configFileStream = workspaceUtils.fileReader(filePath)

			reader = new YamlReader(configFileStream)

			Configuration loadedConfiguration = (Configuration)reader.read()

			logger.debug 'Loaded settings from '+filePath

			return loadedConfiguration
			//	}catch(Exception ex){
			//		ex.printStackTrace()
		}
		finally{
			reader?.close();
		}
	}



	enum ConfigFiles{

		DEFAULT_PROJECT_SETTINGS('resources/core/default-project-settings.yml'),
		APPLICATION_SETTINGS_DIR('resources'),
		REPOSITORY_SETTINGS('resources/core/default-repository-configuration-instructions.yml')

		def filePath

		ConfigFiles(filePath){
			this.filePath = filePath
		}
	}
}
