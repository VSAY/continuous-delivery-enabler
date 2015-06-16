package com.liquidhub.framework.ci.job.generator.impl

import static com.liquidhub.framework.model.JobPermissions.ItemBuild
import static com.liquidhub.framework.model.JobPermissions.ItemCancel
import static com.liquidhub.framework.model.JobPermissions.ItemConfigure
import static com.liquidhub.framework.model.JobPermissions.ItemDiscover
import static com.liquidhub.framework.model.JobPermissions.ItemRead
import static com.liquidhub.framework.model.JobPermissions.ItemWorkspace
import static com.liquidhub.framework.model.JobPermissions.RunDelete
import static com.liquidhub.framework.model.JobPermissions.RunUpdate

import com.liquidhub.framework.ci.job.generator.JobGenerator
import com.liquidhub.framework.ci.logger.Logger
import com.liquidhub.framework.config.model.BuildEnvironmentVariables
import com.liquidhub.framework.config.model.Configuration
import com.liquidhub.framework.config.model.JobConfig
import com.liquidhub.framework.config.model.RoleConfig
import com.liquidhub.framework.model.JobGenerationContext
import com.liquidhub.framework.model.SeedJobParameters


/**
 * A base template for generating jobs for front office projects
 * 
 * Base Assumptions:
 *  
 *   1. All projects use JAVA
 *   2. All projects use maven (can be overriden with any other build tool if its set up on the build node and referenced in the build step)
 * 
 * 
 * 
 * @author Rahul Mishra,LiquidHub
 *
 */
abstract class BaseJobGenerationTemplate implements JobGenerator{

	/**
	 * Primary API which is invoked by job dsl factory to generate a job
	 */

	@Override
	public Object generateJob(JobGenerationContext ctx) {

		Logger logger = JobGenerationContext.logger

		Configuration masterConfig = ctx.configuration

		JobConfig jobConfig = getJobConfig(masterConfig)

		def jobName = determineJobName(ctx, jobConfig)

		logger.info('Now fabricating '+jobName)

		ctx.generateJob(jobName){

			description configureDescription(ctx, jobConfig)

			jdk ctx.buildToolConfig.compilerLanguage.name

			logRotator(
					2,//daysToKeep
					7,//numToKeep
					1,//artifactDaysToKeep
					2)//artifactNumToKeep

			scm  ctx.configurers('scm').configure(ctx, jobConfig, ignoreCommitNotifications())

			triggers ctx.configurers('trigger').configure(ctx, jobConfig)

			steps  configureSteps(ctx, jobConfig)

			publishers configurePublishers(ctx, jobConfig)

			def jobParameters = configureParameters(ctx, jobConfig)

			if(jobParameters){
				parameters jobParameters
			}

			def rolePermissionMappings = createPermissionRoleMappings(ctx)

			rolePermissionMappings.each{rolePermissionMapping ->

				def allowedRole = rolePermissionMapping.key
				def permissions = rolePermissionMapping.value

				permissions.each{grantedPermission->
					authorization{
						permission(grantedPermission.longForm, allowedRole)
					}

				}
			}

			wrappers configureWrappers()

		}

		ctx.logger.info('Finished fabricating '+job.name)

		return job
	}


	/**
	 * @return the key against which the configuration for this job is stored
	 */
	abstract def getJobConfig(Configuration configuration);

	/**
	 * Configure the description for this job, the extension allows for styling description more dynamically.
	 * This base implementation sets the job description based on a configuration file.
	 * 
	 * @param ctx
	 * @param jobConfig
	 * @return
	 */
	protected def configureDescription(ctx, JobConfig jobConfig){
		return jobConfig?.description ? jobConfig.description : 'No description provided'
	}


	protected def configureSteps(JobGenerationContext ctx, JobConfig jobConfig){
		return {
			maven configureMavenCommand(ctx.mvn, jobConfig.goals)
		}
	}

	protected def createPermissionRoleMappings(JobGenerationContext ctx){
		
		RoleConfig roleConfig = ctx.configuration.roleConfig

		def mappings = [

			(roleConfig.developerRole):[ItemRead, ItemDiscover],
			(roleConfig.projectAdminRole): [ItemRead, ItemDiscover, ItemConfigure]]


		def additionalMappings = grantAdditionalPermissions(ctx, roleConfig)

		additionalMappings.each{role,permissions->
			def existingMappings = mappings[role] ?: []
			existingMappings.addAll(additionalMappings[role])
		}
		return mappings

	}


	protected Map grantAdditionalPermissions(JobGenerationContext ctx){
		[:]
	}


	protected def configureSCM(JobGenerationContext ctx){

	}

	protected def ignoreCommitNotifications(){
		true
	}

	protected def configureParameters(JobGenerationContext ctx,JobConfig jobConfig){

	}


	protected def configurePublishers(JobGenerationContext ctx, JobConfig jobConfig){

		def regularEmailRecipients = determineRegularEmailRecipients(ctx, jobConfig)

		def escalationEmails = determineEscalationEmailRecipients(ctx, jobConfig)

		def emailSubject = determineEmailSubject(ctx, jobConfig)
		
		def emailDefnExt = ['recipientEmails': regularEmailRecipients, 'escalationEmails':escalationEmails,'emailSubject': emailSubject]

		return configureAdditionalPublishers(ctx, jobConfig) >>
				ctx.configurers('email').configure(ctx, jobConfig, emailDefnExtn)



	}


	protected def determineRegularEmailRecipients(JobGenerationContext ctx, JobConfig jobConfig){
		
		def notificationConfig = ctx.configuration.notificationConfig
	
		def recipientEmail = ctx.configuration.buildEnvProperties[SeedJobParameters.RECIPIENT_EMAIL.bindingName]
		
		//These are our options to find regular email recipients, ranked by preference, we break on first not null result
		[recipientEmail, jobConfig.regularEmailRecipients, notificationConfig?.regularEmailRecipients, ctx.getDefaultRegularEmailRecipients()].findResult {it!=null}

	}



	protected def determineEscalationEmailRecipients(JobGenerationContext ctx, JobConfig jobConfig){
		
		def escalationEmail = ctx.configuration.buildEnvProperties[SeedJobParameters.ESCALATION_EMAIL.bindingName]
		
		//These are our options to find regular email recipients, ranked by preference, we break on first not null result
		[escalationEmail, jobConfig.escalationEmailRecipients, ctx.configuration.notificationConfig?.escalationEmailRecipients, ctx.getDefaultEscalationEmailRecipients()].findResult {it!=null}

	}

	protected def determineEmailSubject(JobGenerationContext ctx, JobConfig jobConfig){
		//Resort to default email subject if one is not provided
		BuildEnvironmentVariables.PROJECT_NAME.paramValue+' - Build # '+BuildEnvironmentVariables.BUILD_NUMBER.paramValue+' - '+BuildEnvironmentVariables.BUILD_STATUS.paramValue+'!'
	}

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



	protected def determineJobName(JobGenerationContext ctx, JobConfig jobConfig){

		def baseName= determineJobBaseName(ctx)

		[jobConfig?.jobPrefix, baseName, jobConfig?.jobSuffix].findAll().join('')


	}

	protected def determineJobBaseName(JobGenerationContext ctx){
		ctx.projectName
	}
	
	
	boolean supportsGitflow(){
		false
	}


}