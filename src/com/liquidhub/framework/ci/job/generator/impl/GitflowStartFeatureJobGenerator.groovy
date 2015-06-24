package com.liquidhub.framework.ci.job.generator.impl

import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.ALLOW_SNAPSHOTS_WHILE_CREATING_FEATURE
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.ENABLE_FEATURE_VERSIONS
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.FEATURE_NAME
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.PUSH_FEATURES
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.START_COMMIT

import com.liquidhub.framework.ci.model.BuildEnvironmentVariables
import com.liquidhub.framework.ci.model.GitflowJobParameter
import com.liquidhub.framework.ci.model.JobGenerationContext
import com.liquidhub.framework.ci.model.ParameterListingScript
import com.liquidhub.framework.ci.view.ViewElementTypes
import com.liquidhub.framework.config.model.Configuration
import com.liquidhub.framework.config.model.JobConfig
import com.liquidhub.framework.scm.model.SCMRepository

class GitflowStartFeatureJobGenerator extends BaseGitflowJobGenerationTemplateSupport {


	def getJobConfig(Configuration configuration){
		return configuration.gitflowFeatureBranchConfig.startConfig
	}

	protected def defineJobParameters(JobGenerationContext context, JobConfig jobConfig){

		def parameters = []

		SCMRepository scmRepository = context.scmRepository

		parameters << new GitflowJobParameter(
				name: FEATURE_NAME,
				description: 'The name of the feature you intend to start.Please do not prefix feature/  .It is done automatically',
				elementType: ViewElementTypes.TEXT_FIELD
				)

		parameters << new GitflowJobParameter(
				name: START_COMMIT,
				description: generateCommitDescription(scmRepository.changeSetUrl),
				elementType: ViewElementTypes.TEXT_FIELD
				)


		parameters << [ALLOW_SNAPSHOTS_WHILE_CREATING_FEATURE, ENABLE_FEATURE_VERSIONS, PUSH_FEATURES].collect{
			new GitflowJobParameter(
					name: it,
					elementType: ViewElementTypes.BOOLEAN_CHOICE,
					editable:false,
					valueListingScript:new ParameterListingScript(text: true)
					)
		}
	}


	protected def determineEmailSubject(JobGenerationContext ctx,JobConfig jobConfig){

		'Feature # ${PROJECT_VERSION} start '+ BuildEnvironmentVariables.BUILD_STATUS.paramValue+'!'
	}


	def configureBuildSteps(JobGenerationContext ctx, JobConfig jobConfig){

		ctx.cmdBuildStepConfigurer().configure(ctx, jobConfig, 'git checkout develop') >> {
			maven ctx.mavenBuildStepConfigurer().configure(ctx, jobConfig)
		}
	}


	protected def preparePropertiesForDownstreamJobLaunch(JobGenerationContext context){
		[gitRepoUrl: context.scmRepository.repoUrl, repoBranchName: 'feature/${featureName}']
	}

	protected def generateCommitDescription(gitRepoChangeSetUrl){
		"""
           |Your branch diverges/begins at this point. Enter a commit(SHA/short SHA) on the develop branch or enter the branch name 'develop' 
		   |(without the quotes) to branch off the latest development copy. You can copy the desired short SHA (from the commits column)
		   |at <a href='${gitRepoChangeSetUrl}/commits' target='_blank'>List of Commits on develop branch </a>.Avoid typing the commit, prefer copy and paste.
		""".stripMargin()
	}

	protected boolean configuresBranchInitiatingJob() {
		true
	};
}

