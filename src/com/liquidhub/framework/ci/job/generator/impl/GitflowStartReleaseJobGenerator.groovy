package com.liquidhub.framework.ci.job.generator.impl



import com.liquidhub.framework.ci.model.BuildEnvironmentVariables
import com.liquidhub.framework.ci.model.GeneratedJobParameters
import com.liquidhub.framework.ci.model.JobGenerationContext
import com.liquidhub.framework.config.model.Configuration
import com.liquidhub.framework.config.model.JobConfig



class GitflowStartReleaseJobGenerator extends BaseGitflowJobGenerationTemplateSupport {

	private static final String MILESTONE_NAMING_STRATEGY='m'

	def getJobConfig(Configuration configuration){
		return configuration.gitflowFeatureBranchConfig.startConfig
	}


	protected def configureJobParameterExtensions(JobGenerationContext context, JobConfig jobConfig){

		def releaseFrom = context.viewHelper.createSimpleTextBox(GeneratedJobParameters.RELEASE_FROM_BRANCH, 'This is the branch from which the release is initiated. This value cannot be edited',"'develop'",true)

		def releaseVersion = context.viewHelper.createSimpleTextBox(GeneratedJobParameters.RELEASE_VERSION, 'This is version which will be assigned to your final release artifact, this will also be the name of your release branch. Follow the maven versioning scheme','',false)

		def developmentVersion = addChoiceToLaunchConfiguredBranchJob(GeneratedJobParameters.DEVELOPMENT_VERSION, 'This is version which will be assigned to your develop branch, so if your release version is 0.1.14, this version SHOULD be 0.1.15-SNAPSHOT to avoid version clashes across branches','',false)

		def configureBranchJobs = addChoiceToLaunchConfiguredBranchJob(context)
		
		releaseFrom >> releaseVersion >> developmentVersion >> configureBranchJobs
	}


	protected def configureSteps(JobGenerationContext ctx,JobConfig jobConfig){
		return {
			shell ('git checkout develop')
			maven ctx.configurers('maven').configure(ctx, jobConfig)
		} >> addConditionalStepsForDownstreamJobLaunch(ctx, jobConfig)
	}

	protected def preparePropertiesForDownstreamJobLaunch(JobGenerationContext context){
		[gitRepoUrl: context.scmRepository.repoUrl, repoBranchName: 'release/${releaseVersion}']
	}

	

	protected def determineEmailSubject(ctx, jobConfig){

		'Release # ${PROJECT_VERSION} start '+ BuildEnvironmentVariables.BUILD_STATUS.paramValue+'!'
	}
}
