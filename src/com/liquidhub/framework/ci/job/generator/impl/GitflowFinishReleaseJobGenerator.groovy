package com.liquidhub.framework.ci.job.generator.impl


import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.ALLOW_SNAPSHOTS_WHILE_FINISHING_RELEASE
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.KEEP_RELEASE_BRANCH
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.RELEASE_BRANCH
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.RELEASE_TAG_MESSAGE
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.SKIP_RELEASE_BRANCH_MERGE
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.SKIP_RELEASE_TAGGING
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.SQUASH_COMMITS
import static com.liquidhub.framework.ci.view.ViewElementTypes.BOOLEAN_CHOICE
import static com.liquidhub.framework.ci.view.ViewElementTypes.READ_ONLY_BOOLEAN_CHOICE
import static com.liquidhub.framework.ci.view.ViewElementTypes.READ_ONLY_TEXT
import static com.liquidhub.framework.ci.view.ViewElementTypes.TEXT
import static com.liquidhub.framework.providers.jenkins.OperatingSystemCommandAdapter.adapt

import com.liquidhub.framework.ci.EmbeddedScriptProvider
import com.liquidhub.framework.ci.model.BuildEnvironmentVariables
import com.liquidhub.framework.ci.model.GitflowJobParameter
import com.liquidhub.framework.ci.model.JobGenerationContext
import com.liquidhub.framework.ci.model.ParameterListingScript
import com.liquidhub.framework.ci.view.ViewElementTypes
import com.liquidhub.framework.config.model.Configuration
import com.liquidhub.framework.config.model.JobConfig
import com.liquidhub.framework.scm.DevelopmentMilestoneVersionScriptProvider
import com.liquidhub.framework.scm.MilestoneReleaseVersionScriptProvider
import com.liquidhub.framework.scm.model.SCMRemoteRefListingRequest
import com.liquidhub.framework.scm.model.SCMRepository



class GitflowFinishReleaseJobGenerator extends BaseGitflowJobGenerationTemplateSupport {



	private EmbeddedScriptProvider releaseOptionsProvider = new MilestoneReleaseVersionScriptProvider()
	private EmbeddedScriptProvider nextMilestoneChoiceProvider = new DevelopmentMilestoneVersionScriptProvider()


	def getJobConfig(Configuration configuration){
		configuration.gitflowReleaseBranchConfig.finishConfig
	}


	protected def defineJobParameters(JobGenerationContext context, JobConfig jobConfig){

		def parameters = []

		SCMRepository repository = context.scmRepository

		def repoUrl = repository.repoUrl, authDigest = repository.authorizedUserDigest

		SCMRemoteRefListingRequest request = new SCMRemoteRefListingRequest(
				targetUrl: repoUrl,
				authorizedUserDigest: authDigest,
				branchFilterText:  'release/'
				)

		request.listFullRefNames = true
		def descriptionScript = branchNamesListingProvider.getScript(['requestParam':request])

		request.listFullRefNames = false
		def valueScript = branchNamesListingProvider.getScript(['requestParam':request])

		parameters << new GitflowJobParameter(
				name: RELEASE_BRANCH,
				description : 'The release you intend to finish',
				valueListingScript: new ParameterListingScript(text:valueScript),
				labelListingScript: new ParameterListingScript(text:descriptionScript),
				elementType: ViewElementTypes.SINGLE_SELECT_CHOICES
				)

		parameters << new GitflowJobParameter(name: ALLOW_SNAPSHOTS_WHILE_FINISHING_RELEASE, elementType: READ_ONLY_BOOLEAN_CHOICE, defaultValue:false)
		parameters << new GitflowJobParameter(name: KEEP_RELEASE_BRANCH, elementType: READ_ONLY_BOOLEAN_CHOICE, defaultValue:true)
		parameters << new GitflowJobParameter(name: SKIP_RELEASE_BRANCH_MERGE, elementType: BOOLEAN_CHOICE,defaultValue: false)
		parameters << new GitflowJobParameter(name: SQUASH_COMMITS, elementType: BOOLEAN_CHOICE, defaultValue:false)
		parameters << new GitflowJobParameter(name: SKIP_RELEASE_TAGGING, elementType: READ_ONLY_BOOLEAN_CHOICE, defaultValue:false)
		//parameters << new GitflowJobParameter(name: RELEASE_TAG_MESSAGE, elementType: TEXT)
	}



	@Override
	def configureBuildSteps(JobGenerationContext ctx, JobConfig jobConfig){
	

		return{

			ctx.generatingOnWindows ? batchFile(adapt(CHECKOUT_RELEASE_BRANCH)) : shell(CHECKOUT_RELEASE_BRANCH)

			maven ctx.configurers('maven').configure(ctx, jobConfig)

		}
	}

	protected def determineEmailSubject(ctx, jobConfig){

		'General Availability Release # ${PROJECT_VERSION} finish '+ BuildEnvironmentVariables.BUILD_STATUS.paramValue+'!'
	}


	
	static final def CHECKOUT_RELEASE_BRANCH = 'git checkout release/${releaseBranch}'
	
}
