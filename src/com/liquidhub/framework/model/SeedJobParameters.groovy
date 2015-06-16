package com.liquidhub.framework.model

/**
 * Represents an enumeration of parameters (user specified or environment defined) which are provided to the seed job by the job
 * generation environment. This is the contract between the process dsl and the environment
 * in which the dsl executes. 
 * 
 * An explicit enumeration helps avoid reliance on random string parameter names
 *
 * @author Rahul Mishra,LiquidHub
 *
 */
enum SeedJobParameters {

	SCM_REPO_URL('gitRepoUrl'),
	REPO_BRANCH_NAME('repoBranchName'),
	REPO_ACCESS_CREDENTIALS('repositoryUserCredentials'),
	REPO_IMPLEMENTATION('repositoryImplementation'),
	RECIPIENT_EMAIL('recipientEmail'),
	ESCALATION_EMAIL('escalationEmail'),
	FRAMEWORK_CONFIG_BASE_MOUNT('JOB_SEEDER_MOUNT_DIR'),
	TARGET_PROJECT_BASE_MOUNT('TARGET_APP_MOUNT_DIR'),
	USE_GITFLOW('useGitflow'),
	LOGGER_OUTPUT_STREAM('out')

	def bindingName //The actual name with which the parameter is bound to its environment, the enumeration name is logical, the binding name is actual

	SeedJobParameters(bindingName){
		this.bindingName = bindingName
	}
}
