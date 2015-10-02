package com.liquidhub.framework.ci.job.generator.impl

import static com.liquidhub.framework.ci.model.JobPermissions.ItemBuild
import static com.liquidhub.framework.ci.model.JobPermissions.ItemCancel
import static com.liquidhub.framework.ci.model.JobPermissions.ItemConfigure
import static com.liquidhub.framework.ci.model.JobPermissions.ItemDiscover
import static com.liquidhub.framework.ci.model.JobPermissions.ItemRead
import static com.liquidhub.framework.ci.model.JobPermissions.ItemWorkspace
import static com.liquidhub.framework.ci.model.JobPermissions.RunDelete
import static com.liquidhub.framework.ci.model.JobPermissions.RunUpdate

import com.liquidhub.framework.ci.EmbeddedScriptProvider
import com.liquidhub.framework.ci.JobNameBuilder
import com.liquidhub.framework.ci.job.generator.JobGenerator
import com.liquidhub.framework.ci.logger.Logger
import com.liquidhub.framework.ci.model.BuildEnvironmentVariables
import com.liquidhub.framework.ci.model.Email
import com.liquidhub.framework.ci.model.EmailNotificationContext
import com.liquidhub.framework.ci.model.JobGenerationContext
import com.liquidhub.framework.ci.model.JobPermissions
import com.liquidhub.framework.ci.model.SeedJobParameters
import com.liquidhub.framework.config.model.Configuration
import com.liquidhub.framework.config.model.JobConfig
import com.liquidhub.framework.config.model.RoleConfig
import com.liquidhub.framework.providers.jenkins.MavenPOMVersionExtractionScriptProvider


/**
 * A base template for generating jobs. The template can be partially or fully overriden by sub classes
 *
 * Base Assumptions(for now):
 *
 *   1. All projects use JAVA
 *   2. All projects use maven (can be overriden with any other build tool if its set up on the build node and referenced in the build step)
 *
 *
 * @author Rahul Mishra,LiquidHub
 *
 */
abstract class BaseJobGenerationTemplate implements JobGenerator{

	EmbeddedScriptProvider mavenPOMVersionExtractionScript = new MavenPOMVersionExtractionScriptProvider([:])


	/**
	 * Outlines the job definition template, the template can be overriden completely or partly based on requirements.
	 *
	 */
	@Override
	public def generateJob(JobGenerationContext ctx) {

		Logger logger = JobGenerationContext.logger

		Configuration masterConfig = ctx.configuration

		JobConfig jobConfig = getJobConfig(masterConfig)

		def jobName = determineJobName(ctx, jobConfig)

		logger.info('Now fabricating '+jobName)

		ctx.generateJob(jobName){

			description configureDescription(ctx, jobConfig)

			jdk ctx.buildToolConfig.languageRuntime.name

			logRotator(
					masterConfig.buildDiscardPolicy['daysToKeep'] as Integer,
					masterConfig.buildDiscardPolicy['numToKeep'] as Integer,
					masterConfig.buildDiscardPolicy['artifactDaysToKeep'] as Integer,
					masterConfig.buildDiscardPolicy['artifactNumToKeep'] as Integer
					)


			if(requiresSCMConfiguration()){
				scm  ctx.configurers('scm').configure(ctx, jobConfig, identifySCMBranchForBuild(ctx), ignoreCommitNotifications())
			}

			if(requiresTriggerConfiguration()){
				triggers ctx.configurers('trigger').configure(ctx, jobConfig)
			}
			steps  configureSteps(ctx, jobConfig)

			publishers configurePublishers(ctx, jobConfig)

			def jobParameters = configureParameters(ctx, jobConfig)

			if(jobParameters){
				parameters jobParameters
			}

			createPermissionRoleMappings(ctx){ grantedPermission, allowedRole ->
				authorization { permission(grantedPermission, allowedRole) }
			}

			wrappers configureWrappers()

			configure {
				configureExtensions(delegate, ctx, jobConfig)
			}
		}

		ctx.logger.info('Finished fabricating '+jobName)
	}


	/**
	 * @return the key configuration for which the job is being generated, the configured is stored inside this master config
	 *
	 * Most Implementations will simply extract the appropriate nested object
	 *
	 */
	abstract def getJobConfig(Configuration configuration);

	/**
	 * Configure the description for this job, the extension allows for styling description more dynamically.
	 * This base implementation sets the job description based on a configuration file.
	 *
	 * @param ctx
	 * @param jobConfig
	 *
	 * @return
	 */
	protected def configureDescription(ctx, JobConfig jobConfig){
		return jobConfig?.description ? jobConfig.description : 'No description provided'
	}

	protected def requiresSCMConfiguration(){
		true
	}

	protected def requiresTriggerConfiguration(){
		true
	}


	/**
	 * Configure the build steps for this job, by default we assume a maven step and directly use the goals configured
	 *
	 *
	 * @param ctx
	 * @param jobConfig
	 *
	 * @return
	 */
	protected def configureSteps(JobGenerationContext ctx, JobConfig jobConfig){
		return {
			maven ctx.configurers('maven').configure(ctx, jobConfig)
		}
	}

	/**
	 * Creates a default Role-Permission Mapping for this context. This mappings helps restrict access previleges
	 *
	 * @param ctx
	 * @param authorizationClosure
	 *
	 * @return
	 */
	protected def createPermissionRoleMappings(JobGenerationContext ctx, Closure authorizationClosure){

		RoleConfig roleConfig = ctx.configuration.roleConfig

		def mappings = [

			(roleConfig.developerRole):[ItemRead, ItemDiscover],
			(roleConfig.projectAdminRole): [ItemRead, ItemDiscover]]


		def additionalMappings = grantAdditionalPermissions(ctx, roleConfig)

		additionalMappings.each{ role,permissions->
			def existingMappings = mappings[role] ?: []
			existingMappings.addAll(additionalMappings[role])
			mappings[role] = existingMappings
		}


		mappings.each{ rolePermissionMapping ->

			def allowedRole = rolePermissionMapping.key, permissions = rolePermissionMapping.value

			permissions.each{ JobPermissions permission->
				authorizationClosure(permission.longForm, allowedRole)
			}
		}
	}


	/**
	 * Implemented by subclasses when they want to add job specific permissions. The permissions returned are in ADDITION to the
	 * permissions which the base template provides
	 *
	 * @param ctx
	 * @return
	 */
	protected Map grantAdditionalPermissions(JobGenerationContext ctx, RoleConfig roleConfig){
		[:]
	}


	/**
	 * @return the name of the branch which should be used to build the source code
	 */
	protected def identifySCMBranchForBuild(JobGenerationContext ctx){
		ctx.repositoryBranchName
	}


	/**
	 * @return true if the job being generated should be oblivious to commit notifications.
	 */
	protected def ignoreCommitNotifications(){
		true
	}

	/**
	 * Extension point for subclasses to add job specific parameters
	 *
	 * @param ctx
	 * @param jobConfig
	 * @return
	 */
	protected def configureParameters(JobGenerationContext ctx,JobConfig jobConfig){
	}


	/**
	 * Adds post build steps/publishers to the job definition
	 *
	 * @param ctx
	 * @param jobConfig
	 * @return
	 */
	protected def configurePublishers(JobGenerationContext ctx, JobConfig jobConfig){

		EmailNotificationContext email = configureEmail(ctx, jobConfig)


		def mavenPOMVersionExtractorScript = mavenPOMVersionExtractionScript.getScript()

		if(extractPOMVersionAfterBuild()){
			return configureAdditionalPublishers(ctx, jobConfig) >> { groovyPostBuild(mavenPOMVersionExtractorScript) } >> ctx.configurers('email').configure(ctx, jobConfig, email)
		} else {
			return configureAdditionalPublishers(ctx, jobConfig) >> ctx.configurers('email').configure(ctx, jobConfig, email)
		}
	}
	
	protected def extractPOMVersionAfterBuild(){
		true
	}


	protected def configureEmail(JobGenerationContext ctx, JobConfig jobConfig){

		def regularEmailRecipients = determineRegularEmailRecipients(ctx, jobConfig)

		def emailSubject = determineRegularEmailSubject(ctx, jobConfig)

		EmailNotificationContext defaultContext = new EmailNotificationContext(recipientList: regularEmailRecipients, subjectTemplate: emailSubject, contentTemplate: jobConfig.emailContent)

		def escalationEmails = determineEscalationEmailRecipients(ctx, jobConfig)

		Email successEmail = new Email(sendToDevelopers: true,sendToRequestor: true, sendToRecipientList:true ,subject:determineRegularEmailSubject(ctx, jobConfig))

		defaultContext.addEmailForTrigger('Success', successEmail)

		def failureEmailSubject = determineFailureEmailSubject(ctx, jobConfig)

		Email firstFailureEmail = new Email(includeCulprits: true,sendToDevelopers: true,sendToRequestor: true, subject:failureEmailSubject)

		defaultContext.addEmailForTrigger('FirstFailure', firstFailureEmail)

		Email failureEmail = new Email(includeCulprits: true,sendToDevelopers: true,sendToRequestor: true, sendToRecipientList:true , subject:failureEmailSubject, recipientList: [regularEmailRecipients, escalationEmails].join(","))

		defaultContext.addEmailForTrigger('Failure', failureEmail)

		//Any configuration registrations here will override what has been done above
		registerEmailConfigurationForTrigger(ctx, defaultContext, jobConfig)


		return defaultContext
	}

	protected def registerEmailConfigurationForTrigger(JobGenerationContext ctx, EmailNotificationContext emailContext, JobConfig jobConfig){
	}


	protected def determineRegularEmailRecipients(JobGenerationContext ctx, JobConfig jobConfig){

		def notificationConfig = ctx.configuration.notificationConfig

		def recipientEmail = ctx.getVariable(SeedJobParameters.RECIPIENT_EMAIL)

		//ctx.logger.debug 'Recipient Emails [bindVariable: '+recipientEmail+', jobConfig: '+jobConfig.regularEmailRecipients+', notificationConfig: '+notificationConfig?.regularEmailRecipients+', default: '+ctx.defaultRegularEmailRecipients+']'

		//These are our options to find regular email recipients, ranked by preference, we break on first not null result
		[recipientEmail, jobConfig.regularEmailRecipients, notificationConfig?.regularEmailRecipients, ctx.defaultRegularEmailRecipients].findResult {it?.trim() ? it:null}

	}



	protected def determineEscalationEmailRecipients(JobGenerationContext ctx, JobConfig jobConfig){

		def escalationEmail = ctx.getVariable(SeedJobParameters.ESCALATION_EMAIL)

		def notificationConfig = ctx.configuration.notificationConfig

		//ctx.logger.debug 'Escalation Emails [bindVariable: '+escalationEmail+', jobConfig: '+jobConfig.escalationEmailRecipients+', notificationConfig: '+notificationConfig?.escalationEmailRecipients+', default: '+ctx.defaultEscalationEmailRecipients+']'

		//These are our options to find regular email recipients, ranked by preference, we break on first not null result
		[escalationEmail, jobConfig.escalationEmailRecipients, notificationConfig?.escalationEmailRecipients, ctx.defaultEscalationEmailRecipients].findResult {it?.trim() ? it:null}

	}


	protected def determineRegularEmailSubject(JobGenerationContext ctx, JobConfig jobConfig){
		//Resort to default email subject if one is not provided
		BuildEnvironmentVariables.PROJECT_NAME.paramValue+' - Build # '+BuildEnvironmentVariables.BUILD_NUMBER.paramValue+' - '+BuildEnvironmentVariables.BUILD_STATUS.paramValue+'!'
	}

	protected def determineFailureEmailSubject(JobGenerationContext ctx, JobConfig jobConfig){
		'Fix Required!!!'+determineRegularEmailSubject(ctx, jobConfig)
	}

	/**
	 * Extension point for subclasses to add additional publishers (besides the email publisher provided by this base template)
	 *
	 * @param ctx
	 * @param jobConfig
	 * @return
	 */
	protected def configureAdditionalPublishers(JobGenerationContext ctx, JobConfig jobConfig){
		return {}
	}

	protected def configureWrappers(){

		return {
			preBuildCleanup() //Requires Workspace Clean up Plugin
			timestamps() //Add timestamps to the log . Requires Timestamper Plugin
			maskPasswords() //Requires Mask Password Plugin
			injectPasswords()//Requires the Env Inject Plugin
		}
	}


	/**
	 * Non extensible job naming pattern. The pattern is frozen but the name of a job can be altered by tweaking its components.
	 * 
	 * This a final API because the framework makes assumptions about the naming pattern
	 *
	 * The job name is based on a pattern of ${jobPrefix}{baseName}${jobSuffix}
	 *
	 * The job prefix and suffic can be provided via configuration. Also see 'determineJobBaseName'
	 *
	 * @param ctx
	 * @param jobConfig
	 *
	 * @return
	 *
	 *
	 */
	final def determineJobName(JobGenerationContext ctx, JobConfig jobConfig){

		ctx.jobNameCreator.createJobName(ctx.repositoryName, ctx.scmRepository.branchType, ctx.repositoryBranchName, jobConfig, supportsGitflow())

	}


	/**
	 *  @return true if the generator is a gitflow supporting generator. Defaults to false
	 */
	boolean supportsGitflow(){
		false
	}


	protected def configureExtensions(delegationContext, JobGenerationContext context, JobConfig jobConfig){

		def parameterExtensions = configureJobParameterExtensions(context, jobConfig)

		delegationContext.with{

			if(parameterExtensions){
				root / 'properties' / 'hudson.model.ParametersDefinitionProperty' / 'parameterDefinitions'(parameterExtensions)
			}

		}

	}

	protected def configureJobParameterExtensions(JobGenerationContext context, JobConfig jobCofig){

	}


}