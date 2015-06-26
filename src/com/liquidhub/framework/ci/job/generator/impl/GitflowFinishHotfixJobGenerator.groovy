package com.liquidhub.framework.ci.job.generator.impl

import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.DEVELOPMENT_VERSION
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.ALLOW_SNAPSHOTS_WHILE_FINISHING_HOTFIX
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.DEVELOPMENT_VERSION
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.HOTFIX_BRANCH
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.HOTFIX_VERSION
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.KEEP_HOTFIX_BRANCH
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.NO_DEPLOY
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.SKIP_HOTFIX_TAGGING
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.SQUASH_COMMITS

import com.liquidhub.framework.ci.model.GitflowJobParameter
import com.liquidhub.framework.ci.model.JobGenerationContext
import com.liquidhub.framework.ci.model.ParameterListingScript
import com.liquidhub.framework.config.model.Configuration
import com.liquidhub.framework.config.model.JobConfig
import com.liquidhub.framework.scm.model.SCMRemoteRefListingRequest
import com.liquidhub.framework.scm.model.SCMRepository

class GitflowFinishHotfixJobGenerator extends BaseGitflowJobGenerationTemplateSupport{

	@Override
	def getJobConfig(Configuration configuration){
		configuration.gitflowHotfixBranchConfig.finishConfig
	}


	def defineParameters(JobGenerationContext context, JobConfig jobConfig){

		def parameters = []

		SCMRepository repository = context.scmRepository

		def repoUrl = repository.repoUrl, authDigest = repository.authorizedUserDigest

		SCMRemoteRefListingRequest request = new SCMRemoteRefListingRequest(
				targetUrl: repoUrl,
				authorizedUserDigest: authDigest,
				branchFilterText:  'hotfix/'
				)

		def descriptionScript = descriptionListingProvider.getScript(['requestParam':request])

		def valueScript = valueListingProvider.getScript(['requestParam':request])

		parameters << new GitflowJobParameter(
				name: HOTFIX_BRANCH,
				valueScript: new ParameterListingScript(text:valueScript),
				descriptionScript: new ParameterListingScript(text:descriptionScript)
				)

		parameters << new GitflowJobParameter(
				name: DEVELOPMENT_VERSION,
				description : 'What do you want the next development version to be?'
				)
	}

	def configureBuildSteps(JobGenerationContext context, JobConfig jobConfig){

		return{
			context.configurers('os').configure(ctx, jobConfig,'git checkout hotfix/${hotfixVersion}')
			maven context.configurers('maven').configure(context, jobConfig)
		}
	}
}
