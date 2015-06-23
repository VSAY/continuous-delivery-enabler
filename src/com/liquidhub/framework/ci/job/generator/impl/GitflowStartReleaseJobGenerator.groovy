package com.liquidhub.framework.ci.job.generator.impl

import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.DEVELOPMENT_VERSION
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.RELEASE_FROM_BRANCH
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.RELEASE_VERSION

import com.liquidhub.framework.ci.model.BuildEnvironmentVariables
import com.liquidhub.framework.ci.model.GitflowJobParameter
import com.liquidhub.framework.ci.model.JobGenerationContext
import com.liquidhub.framework.ci.view.ViewElementTypes
import com.liquidhub.framework.config.model.Configuration
import com.liquidhub.framework.config.model.JobConfig

class GitflowStartReleaseJobGenerator extends BaseGitflowJobGenerationTemplateSupport {

	private static final String MILESTONE_NAMING_STRATEGY='m'

	def getJobConfig(Configuration configuration){
		configuration.gitflowReleaseBranchConfig.startConfig
	}


	protected def defineJobParameters(JobGenerationContext context, JobConfig jobConfig){

		def parameters = [] 

		parameters << new GitflowJobParameter(
				name: RELEASE_FROM_BRANCH,
				description: 'This is the branch from which the release is initiated. This value cannot be edited',
				defaultValue: 'develop',
				editable: false,
				elementType: ViewElementTypes.TEXT_FIELD
				)

		parameters << new GitflowJobParameter(
				name: RELEASE_VERSION,
				description: 'This is version which will be assigned to your final release artifact, this will also be the name of your release branch. Follow the maven versioning scheme',
				elementType: ViewElementTypes.TEXT_FIELD
				)

		parameters << new GitflowJobParameter(
				name: DEVELOPMENT_VERSION,
				description: 'This is version which will be assigned to your develop branch, so if your release version is 0.1.14, this version SHOULD be 0.1.15-SNAPSHOT to avoid version clashes across branches',
				elementType: ViewElementTypes.TEXT_FIELD
				)
	}


	def configureBuildSteps(JobGenerationContext ctx,JobConfig jobConfig){
		return {
			shell ('git checkout develop')
			maven ctx.configurers('maven').configure(ctx, jobConfig)
		}
	}

	protected def preparePropertiesForDownstreamJobLaunch(JobGenerationContext context){
		[gitRepoUrl: context.scmRepository.repoUrl, repoBranchName: 'release/${releaseVersion}']
	}



	protected def determineEmailSubject(ctx, jobConfig){

		'Release # ${PROJECT_VERSION} start '+ BuildEnvironmentVariables.BUILD_STATUS.paramValue+'!'
	}

	protected boolean configuresBranchInitiatingJob(){
		true
	}
}
