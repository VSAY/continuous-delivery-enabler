package com.liquidhub.framework.ci.job.generator.impl

import static com.liquidhub.framework.ci.model.JobPermissions.ItemBuild
import static com.liquidhub.framework.ci.model.JobPermissions.ItemCancel
import static com.liquidhub.framework.ci.model.JobPermissions.ItemWorkspace
import static com.liquidhub.framework.ci.model.JobPermissions.RunDelete
import static com.liquidhub.framework.ci.model.JobPermissions.RunUpdate

import com.liquidhub.framework.ci.model.BuildEnvironmentVariables
import com.liquidhub.framework.ci.model.JobGenerationContext
import com.liquidhub.framework.config.model.Configuration
import com.liquidhub.framework.config.model.JobConfig
import com.liquidhub.framework.config.model.RoleConfig
import com.liquidhub.framework.scm.model.GitFlowBranchTypes

/**
 * Generates the continuous integration job for a Repository Branch
 * 
 * @author Rahul Mishra, LiquidHub
 *
 */
class ContinuousIntegrationJobGenerator extends BaseJobGenerationTemplate{

	/**
	 * Return the configuration element against which the CI job configuration is made
	 */
	def getJobConfig(Configuration configuration){
		configuration.continuousIntegrationConfig
	}




	/**
	 * Return false to indicate that this job must be fired on every commit/commit notifications MUST never be ignored
	 */

	protected def ignoreCommitNotifications(){
		false
	}

	protected def configureAdditionalPublishers(JobGenerationContext ctx, JobConfig jobConfig){

		JobConfig downstreamJobConfig
		JobConfig downstreamDeployConfig
		Configuration masterConfig = ctx.configuration

		/*
		 * The flow we are trying to build
		 * 
		 * feature ci > feature finish > develop ci > release start > release ci > release finish > master health
		 * 
		 */

		switch(ctx.scmRepository.branchType){

			case GitFlowBranchTypes.FEATURE:
				downstreamJobConfig = masterConfig.gitflowFeatureBranchConfig.finishConfig
				break

			case GitFlowBranchTypes.RELEASE:
				downstreamJobConfig=masterConfig.gitflowReleaseBranchConfig.finishConfig
				break

			case GitFlowBranchTypes.HOTFIX:
				downstreamJobConfig= masterConfig.gitflowHotfixBranchConfig.finishConfig
				break

			case GitFlowBranchTypes.DEVELOP:
				downstreamJobConfig= masterConfig.gitflowReleaseBranchConfig.startConfig
				break

			case GitFlowBranchTypes.MASTER://Breaking this connection for now, affects pipeline visualization
			//downstreamJobConfig= masterConfig.gitflowHotfixBranchConfig.startConfig
				break
		}




		return {
			stashNotifier()
			if(downstreamJobConfig){
				def downstreamJobName = ctx.jobNameCreator.createJobName(ctx.repositoryName, null, null, downstreamJobConfig, true)
				buildPipelineTrigger(downstreamJobName)
			}
		}
	}

	protected def determineRegularEmailSubject(JobGenerationContext ctx,JobConfig jobConfig){
		//We know that project version is created by the post build script attached by the CI Job
		BuildEnvironmentVariables.PROJECT_NAME.paramValue+' - Version # ${PROJECT_VERSION} - '+ BuildEnvironmentVariables.BUILD_STATUS.paramValue+'!'
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
	@Override
	protected def configureSteps(JobGenerationContext ctx, JobConfig jobConfig){

		def deploymentConfig = ctx.configuration.deploymentConfig.environments.findResult {it.name =~ 'Dev|dev' ? it: null}

		if(GitFlowBranchTypes.DEVELOP.equals(ctx.scmRepository.branchType) && deploymentConfig!=null){
			return linkBuildToDevDeployment(ctx, jobConfig, deploymentConfig) << {
				maven ctx.configurers('maven').configure(ctx, jobConfig)
				systemGroovyCommand(mavenPOMVersionExtractionScript.getScript())
			} 
		}else{
			return super.configureSteps(ctx, jobConfig)
		}
	}

	protected def linkBuildToDevDeployment(JobGenerationContext ctx, JobConfig jobConfig, deploymentConfig){

		return	{
			conditionalSteps{
				condition{ shell(ContinuousIntegrationJobGenerator.CHECK_FOR_DEPLOYMENT_INSTRUCTION) }
				runner(ContinuousIntegrationJobGenerator.DO_NOT_RUN_IF_CONDITION_NOT_MET) //For any other values, look at runner classes of Run Condition Plugin. Basically means, do not run if condition is not met
				downstreamParameterized{
					def downstreamJobName = ctx.jobNameCreator.createJobName(ctx.repositoryName, null, null, deploymentConfig)
					trigger(downstreamJobName,'ALWAYS'){
						//TODO Investigate why this has to be 'ALWAYS', it should be SUCCESS but that value does not work
						predefinedProps(['version':'${PROJECT_VERSION}'])
					}
				}
			}
		}

	}

	protected def extractPOMVersionAfterBuild(){
		false
	}


	/**
	 * Return job specific permissions, since this is a CI job we want developers to have all sorts of access on the job (except the capability to change configuration) 
	 */
	protected Map grantAdditionalPermissions(JobGenerationContext ctx, RoleConfig roleConfig){
		[(roleConfig.developerRole):[ItemBuild, ItemCancel, RunUpdate, RunDelete, ItemWorkspace]]
	}

	//List the last commit, search for text 'CI Deploy'
	private static final String CHECK_FOR_DEPLOYMENT_INSTRUCTION='git rev-list $GIT_COMMIT -1 --oneline |grep "ci:deploy" > /dev/null'

	private static final String DO_NOT_RUN_IF_CONDITION_NOT_MET='DontRun'
}