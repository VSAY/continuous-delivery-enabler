package com.liquidhub.framework.ci.job.generator.impl

import com.liquidhub.framework.ci.model.BuildEnvironmentVariables
import com.liquidhub.framework.ci.model.GeneratedJobParameters
import com.liquidhub.framework.ci.model.JobGenerationContext
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
	def configureSteps(JobGenerationContext ctx, JobConfig jobConfig){

		return {
			if(ctx.isGeneratingOnWindows()){
				batchFile('git checkout feature/%featureName%')
			}else{
				shell('git checkout feature/${featureName}')
			}

			maven ctx.configurers('maven').configure(ctx, jobConfig)
		}
	}

	@Override
	protected def determineEmailSubject(JobGenerationContext ctx,JobConfig jobConfig){
		'Feature # ${PROJECT_VERSION} finish '+ BuildEnvironmentVariables.BUILD_STATUS.paramValue+'!'
	}


	protected def configureJobParameterExtensions(JobGenerationContext context, JobConfig jobConfig){

		SCMRepository repository = context.scmRepository

		SCMRemoteRefListingRequest request = new SCMRemoteRefListingRequest(
				targetUrl: repository.repoUrl,
				authorizedUserDigest: repository.authorizedUserDigest,
				branchFilterText:  'feature/',
				displayLabel: 'id'
				)

		def descriptionScript = descriptionListingProvider.getScript(['requestParam':request])

		def valueScript = valueListingProvider.getScript(['requestParam':request])

		context.viewHelper.createChoiceOptionsView(GeneratedJobParameters.FEATURE_NAME , 'Select the feature you intend to finish', valueScript, descriptionScript,[:])
	}
}
