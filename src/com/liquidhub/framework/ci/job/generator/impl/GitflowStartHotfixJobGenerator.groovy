package com.liquidhub.framework.ci.job.generator.impl

import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.ALLOW_SNAPSHOTS_WHILE_CREATING_HOTFIX
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.PUSH_HOTFIXES
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.RELEASE_VERSION
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.START_COMMIT
import static com.liquidhub.framework.providers.jenkins.OperatingSystemCommandAdapter.adapt

import static com.liquidhub.framework.ci.view.ViewElementTypes.READ_ONLY_BOOLEAN_CHOICE
import static com.liquidhub.framework.ci.view.ViewElementTypes.TEXT

import com.liquidhub.framework.ci.model.BuildEnvironmentVariables
import com.liquidhub.framework.ci.model.GitflowJobParameter
import com.liquidhub.framework.ci.model.JobGenerationContext
import com.liquidhub.framework.ci.view.ViewElementTypes
import com.liquidhub.framework.config.model.Configuration
import com.liquidhub.framework.config.model.JobConfig

class GitflowStartHotfixJobGenerator extends BaseGitflowJobGenerationTemplateSupport{


	@Override
	def getJobConfig(Configuration configuration){
		configuration.gitflowHotfixBranchConfig.startConfig
	}

	protected def defineJobParameters(JobGenerationContext context, JobConfig jobConfig){

		def parameters = []

		def startCommitDescription = generateCommitDescription(context.scmRepository.changeSetUrl)
		parameters << new GitflowJobParameter(name:START_COMMIT,description:startCommitDescription, elementType: TEXT)
		parameters << new GitflowJobParameter(name:RELEASE_VERSION, description: 'Default version to use when starting a hotfix.', elementType: TEXT)
		parameters << new GitflowJobParameter(name:ALLOW_SNAPSHOTS_WHILE_CREATING_HOTFIX, defaultValue:true,elementType: READ_ONLY_BOOLEAN_CHOICE)
		parameters << new GitflowJobParameter(name:PUSH_HOTFIXES, defaultValue:true, elementType: READ_ONLY_BOOLEAN_CHOICE)
	}


	protected def determineEmailSubject(JobGenerationContext ctx,JobConfig jobConfig){

		'Hotfix # ${PROJECT_VERSION} start '+ BuildEnvironmentVariables.BUILD_STATUS.paramValue+'!'
	}


	def configureBuildSteps(JobGenerationContext ctx, JobConfig jobConfig){

		return {
			ctx.generatingOnWindows ? batchFile(CHECK_OUT_DEVELOP) : shell(CHECK_OUT_DEVELOP)
			maven ctx.mavenBuildStepConfigurer().configure(ctx, jobConfig)
		}
	}

	protected boolean configuresBranchInitiatingJob() {
		true
	}

	protected def preparePropertiesForDownstreamJobLaunch(JobGenerationContext context){
		[gitRepoUrl: context.scmRepository.repoUrl, repoBranchName: 'hotfix/${releaseVersion}']
	}

	protected def generateCommitDescription(gitRepoChangeSetUrl){
		"""
           |Your branch diverges/begins at this point. Enter a commit(SHA/short SHA) on a tag in the master branch or enter the branch name 'master' 
		   |(without the quotes) to branch off the latest master copy. You can copy the desired short SHA (from the commits column)
		   |at <a href='${gitRepoChangeSetUrl}/commits' target='_blank'>List of Commits on Master branch </a>.Avoid typing the commit, prefer copy and paste.
		""".stripMargin()
	}

	private static final String CHECK_OUT_DEVELOP = 'git checkout develop'
}
