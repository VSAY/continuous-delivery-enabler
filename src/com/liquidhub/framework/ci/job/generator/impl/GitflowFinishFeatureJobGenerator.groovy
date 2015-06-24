package com.liquidhub.framework.ci.job.generator.impl

import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.FEATURE_NAME
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.KEEP_FEATURE_BRANCH
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.SKIP_FEATURE_MERGE_TO_DEVELOP
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.SQUASH_COMMITS

import com.liquidhub.framework.ci.model.BuildEnvironmentVariables
import com.liquidhub.framework.ci.model.GitflowJobParameter
import com.liquidhub.framework.ci.model.JobGenerationContext
import com.liquidhub.framework.ci.model.ParameterListingScript
import com.liquidhub.framework.ci.view.ViewElementTypes
import com.liquidhub.framework.config.model.Configuration
import com.liquidhub.framework.config.model.JobConfig
import com.liquidhub.framework.scm.model.SCMRemoteRefListingRequest
import com.liquidhub.framework.scm.model.SCMRepository


class GitflowFinishFeatureJobGenerator extends BaseGitflowJobGenerationTemplateSupport {


	@Override
	public def getJobConfig(Configuration configuration) {
		return configuration.gitflowFeatureBranchConfig.finishConfig
	}


	@Override
	def configureBuildSteps(JobGenerationContext ctx, JobConfig jobConfig){

		return {
			ctx.cmdBuildStepConfigurer().configure(ctx, jobConfig, CHECK_OUT_FEATURE)
			maven ctx.mavenBuildStepConfigurer().configure(ctx, jobConfig)
		}
	}

	@Override
	protected def determineEmailSubject(JobGenerationContext ctx,JobConfig jobConfig){
		'Feature # ${PROJECT_VERSION} finish '+ BuildEnvironmentVariables.BUILD_STATUS.paramValue+'!'
	}


	protected def defineJobParameters(JobGenerationContext context, JobConfig jobConfig){

		SCMRepository repository = context.scmRepository

		SCMRemoteRefListingRequest request = new SCMRemoteRefListingRequest(
				targetUrl: repository.repoUrl,
				authorizedUserDigest: repository.authorizedUserDigest,
				branchFilterText:  'feature/',
				displayLabel: 'id'
				)

		def descriptionScript = descriptionListingProvider.getScript(['requestParam':request])

		def valueScript = valueListingProvider.getScript(['requestParam':request])


		def parameters = []

		parameters << new GitflowJobParameter(
				name: FEATURE_NAME,
				description:  'Select the feature you intend to finish',
				elementType: ViewElementTypes.SINGLE_SELECT_CHOICES,
				valueListingScript: new ParameterListingScript(text: valueScript),
				labelListingScript: new ParameterListingScript(text: descriptionScript)
				)

		parameters << new GitflowJobParameter(
				name: KEEP_FEATURE_BRANCH,
				elementType: ViewElementTypes.BOOLEAN_CHOICE,
				editable:false,
				defaultValue:true
				)

		parameters << new GitflowJobParameter(
				name: SKIP_FEATURE_MERGE_TO_DEVELOP,
				elementType: ViewElementTypes.BOOLEAN_CHOICE,
				editable:false,
				valueListingScript: new ParameterListingScript(text: false)
				)


		parameters << new GitflowJobParameter(
				name: SQUASH_COMMITS,
				elementType: ViewElementTypes.BOOLEAN_CHOICE,
				editable:true,
				defaultValue:false
				)
	}

	private static final String CHECK_OUT_FEATURE = 'git checkout feature/${featureName}'
}
