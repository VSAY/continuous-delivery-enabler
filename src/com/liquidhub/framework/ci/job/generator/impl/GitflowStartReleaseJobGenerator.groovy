package com.liquidhub.framework.ci.job.generator.impl

import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.ALLOW_SNAPSHOTS_WHILE_CREATING_RELEASE
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.DEVELOPMENT_VERSION
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.PUSH_RELEASES
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.RELEASE_BRANCH_VERSION_SUFFIX
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.RELEASE_VERSION
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.START_COMMIT

import com.liquidhub.framework.ci.model.BuildEnvironmentVariables
import com.liquidhub.framework.ci.model.GitflowJobParameter
import com.liquidhub.framework.ci.model.JobGenerationContext
import com.liquidhub.framework.ci.model.ParameterListingScript
import com.liquidhub.framework.ci.view.ViewElementTypes
import com.liquidhub.framework.config.model.Configuration
import com.liquidhub.framework.config.model.JobConfig
import static com.liquidhub.framework.providers.jenkins.OperatingSystemCommandAdapter.adapt


class GitflowStartReleaseJobGenerator extends BaseGitflowJobGenerationTemplateSupport {

	private static final String MILESTONE_NAMING_STRATEGY='m'

	def getJobConfig(Configuration configuration){
		configuration.gitflowReleaseBranchConfig.startConfig
	}


	protected def defineJobParameters(JobGenerationContext context, JobConfig jobConfig){

		def parameters = []

		parameters << new GitflowJobParameter(
				name: START_COMMIT,
				description: 'This is the starting point of the release branch.For now, this value cannot be edited',
				defaultValue: 'develop',
				editable: true,
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


		parameters << new GitflowJobParameter(
				name: RELEASE_BRANCH_VERSION_SUFFIX,
				elementType: ViewElementTypes.TEXT_FIELD
				)

		parameters << [ALLOW_SNAPSHOTS_WHILE_CREATING_RELEASE, PUSH_RELEASES].collect {
			new GitflowJobParameter(
					name: it,
					elementType: ViewElementTypes.BOOLEAN_CHOICE,
					editable:false,
					valueListingScript: new ParameterListingScript(text:true)
					)
		}
	}


	def configureBuildSteps(JobGenerationContext ctx,JobConfig jobConfig){
		return{
			ctx.generatingOnWindows ? batchFile(adapt(CHECK_OUT_DEVELOP)) : shell(CHECK_OUT_DEVELOP)
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

	final String CHECK_OUT_DEVELOP = 'git checkout develop'
}
