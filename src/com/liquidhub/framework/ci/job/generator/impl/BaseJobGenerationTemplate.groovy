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
import com.liquidhub.framework.ci.job.generator.JobGenerator
import com.liquidhub.framework.ci.logger.Logger
import com.liquidhub.framework.ci.model.BuildEnvironmentVariables
import com.liquidhub.framework.ci.model.Email
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
	
	private EmbeddedScriptProvider mavenPOMVersionExtractionScript = new MavenPOMVersionExtractionScriptProvider([:])
	

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

			scm  ctx.configurers('scm').configure(ctx, jobConfig, ignoreCommitNotifications())

			triggers ctx.configurers('trigger').configure(ctx, jobConfig)

			steps  configureSteps(ctx, jobConfig)

			publishers configurePublishers(ctx, jobConfig)

			def jobParameters = configureParameters(ctx, jobConfig)
			
			if(jobParameters){
				parameters jobParameters
			}

			createPermissionRoleMappings(ctx){grantedPermission, allowedRole ->
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
			(roleConfig.projectAdminRole): [ItemRead, ItemDiscover, ItemConfigure]]


		def additionalMappings = grantAdditionalPermissions(ctx, roleConfig)

		additionalMappings.each{role,permissions->
			def existingMappings = mappings[role] ?: []
			existingMappings.addAll(additionalMappings[role])
		}


		mappings.each{rolePermissionMapping ->

			def allowedRole = rolePermissionMapping.key, permissions = rolePermissionMapping.value

			permissions.each{JobPermissions permission->
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

		def regularEmailRecipients = determineRegularEmailRecipients(ctx, jobConfig)

		def escalationEmails = determineEscalationEmailRecipients(ctx, jobConfig)

		def emailSubject = determineEmailSubject(ctx, jobConfig)

		def email = new Email(sendTo: regularEmailRecipients, escalateTo: escalationEmails, subject: emailSubject)
		
		def mavenPOMVersionExtractorScript = mavenPOMVersionExtractionScript.getScript()

		return configureAdditionalPublishers(ctx, jobConfig) >>
				 { groovyPostBuild(mavenPOMVersionExtractorScript) } >> ctx.configurers('email').configure(ctx, jobConfig, email)
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

	protected def determineEmailSubject(JobGenerationContext ctx, JobConfig jobConfig){
		//Resort to default email subject if one is not provided
		BuildEnvironmentVariables.PROJECT_NAME.paramValue+' - Build # '+BuildEnvironmentVariables.BUILD_NUMBER.paramValue+' - '+BuildEnvironmentVariables.BUILD_STATUS.paramValue+'!'
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
	 * Extension point for subclasses to control the pattern for the job name.
	 * A pattern is utilized if an explicit job name is not provided by configuration
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
	protected def determineJobName(JobGenerationContext ctx, JobConfig jobConfig){

		def baseName= jobConfig?.jobName ?: determineJobBaseName(ctx, jobConfig)

		[jobConfig?.jobPrefix, baseName, jobConfig?.jobSuffix].findAll().join('')


	}

	/**
	 * Extension point for subclasses to determine the base name of the job generated
	 *
	 * We first look if the job configuration specifies a name, if it doesn't we resort to using the one
	 *
	 * @param ctx
	 * @param jobConfig
	 *
	 * @return
	 *
	 *
	 */
	protected def determineJobBaseName(JobGenerationContext ctx, JobConfig jobConfig){
		
		/*
		 * We use the branch name to create the project name, but some branches can contain a prefix  which makes the job name unnecessarily long, so we shorten it
		 * 
		 * Here are the transformation samples:
		 * 
		 *  feature/ssoIntegration -> ssoIntegration
		 *  release/1.0.0 -> 1.0.0
		 * 
		 */
		def shortenedBranchName = ctx.repositoryBranchName.replace("(^feature/)", "")
		shortenedBranchName = ctx.repositoryBranchName.replace("(^hotfix/)", "hotfix-")
		shortenedBranchName = ctx.repositoryBranchName.replace("(^release/)", "release-")
		
		ctx.repositoryName+'-'+shortenedBranchName
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