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
import com.liquidhub.framework.ci.model.GeneratedJobParameters
import com.liquidhub.framework.ci.model.JobGenerationContext
import com.liquidhub.framework.ci.model.SeedJobParameters
import com.liquidhub.framework.config.model.JobConfig
import com.liquidhub.framework.config.model.RoleConfig
import com.liquidhub.framework.scm.DescriptiveBranchNamesListingScriptProvider
import com.liquidhub.framework.scm.ShortBranchNamesListingScriptProvider

/**
 * A basic template to generate gitflow jobs. Provides extension points for subclasses.
 * 
 * Removes the boiler plate code typically required by gitflow generators
 * 
 * Implements the template method pattern allowing subclasses to override partial/complete implementation
 * 
 */
abstract class BaseGitflowJobGenerationTemplateSupport extends BaseJobGenerationTemplate{
	
	protected EmbeddedScriptProvider descriptionListingProvider = new DescriptiveBranchNamesListingScriptProvider()
	protected EmbeddedScriptProvider  valueListingProvider = new ShortBranchNamesListingScriptProvider()
	

	protected def configureDescription(JobGenerationContext ctx,JobConfig jobConfig){

		def templateParams =['repositoryName': ctx.repositoryName, 'pluginArgs': jobConfig.goalArgs]

		def workspaceRelativeTemplatePath = [
			ctx.getVariable(SeedJobParameters.FRAMEWORK_CONFIG_BASE_MOUNT),
			jobConfig.projectDescriptionTemplatePath
		].join(File.separator)

		ctx.templateEngine.withContentFromTemplate(workspaceRelativeTemplatePath, templateParams << jobConfig.goalArgs)
	}


	protected Map grantAdditionalPermissions(JobGenerationContext ctx,RoleConfig roleConfig){
	
		[:].put(roleConfig.projectAdminRole, [ItemBuild, ItemCancel, ItemDiscover, ItemRead, RunUpdate, RunDelete, ItemWorkspace])
	}
	
	
	protected def addConditionalStepsForDownstreamJobLaunch(JobGenerationContext ctx, JobConfig jobConfig){

		return {
			conditionalSteps{
				condition{ booleanCondition('${'+GeneratedJobParameters.CONFIGURE_BRANCH_JOBS.parameterName+'}') } //We do not want variable substitution before it is embedded into configuration
				runner("DontRun") //For any other values, look at runner classes of Run Condition Plugin
				downstreamParameterized{
					trigger(ctx.jobSeederName,'ALWAYS'){
						//TODO Investigate why this has to be 'ALWAYS', it should be SUCCESS but that value does not work
						predefinedProps(preparePropertiesForDownstreamJobLaunch(ctx))
					}
				}
			}
		}
	}


	protected def addChoiceToLaunchConfiguredBranchJob(JobGenerationContext ctx){
		def description = '''
					|Do you want to generate the jobs for this branch after the branch is created? You can always create them later
					|using our job seeder, just pick the repository and enter the branch name.
					'''.stripMargin()

		ctx.viewHelper.createSimpleBooleanChoice(GeneratedJobParameters.CONFIGURE_BRANCH_JOBS, description, true)
	}

	protected def preparePropertiesForDownstreamJobLaunch(JobGenerationContext context){
		throw new RuntimeException('Not Yet Implemented')
	}

}
