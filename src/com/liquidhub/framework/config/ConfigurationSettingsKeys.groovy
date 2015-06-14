package com.liquidhub.framework.config

import java.util.concurrent.ConcurrentSkipListMap.Values;

enum ConfigurationSettingsKeys {

	CONTINUOUS_INTEGRATION('continuousIntegrationConfig',HashMap.class),
	RELEASE('releaseConfig',HashMap.class),
	DEPLOYMENT('deploymentConfig',HashMap.class, true),
	CODE_QUALITY('codeQualityConfig',HashMap.class),
	NOTIFICATION_CONFIG('notificationConfig',HashMap.class),
	INTEGRATION_TESTS('integrationTestsConfig',HashMap.class),
	GITFLOW_FEATURE_BRANCH_CONFIG('gitflowFeatureBranchConfig', HashMap.class),
	GITFLOW_RELEASE_BRANCH_CONFIG('gitflowReleaseBranchConfig', HashMap.class),
	GITFLOW_HOTFIX_BRANCH_CONFIG('gitflowHotfixBranchConfig',HashMap.class),
	ROLE_CONFIG('roleConfig', HashMap.class),
	BUILD_CONFIG('buildConfig', HashMap.class),
	VIEW_CONFIG('viewConfig', HashMap.class)
	//BUILD_PIPELINE_PREFERENCES('buildPipelinePreferences', HashMap.class)


	static {
		values().each{configurationSetting->
			configKeyEnumMapping[configurationSetting.configName] = configurationSetting
		}

	}

	public ConfigurationSettingsKeys(configName, configurationClass, retainConfigurationLevel=false){
		this.configName = configName
		this.backingBean = configurationClass
		this.retainConfigurationLevel=retainConfigurationLevel
	}

	public static def fromConfigKey(configKey){
		configKeyEnumMapping[configKey];
	}

	def configName //The name used to index the configuration properties
	def backingBean
	def retainConfigurationLevel
	private static def configKeyEnumMapping=[:]
}
