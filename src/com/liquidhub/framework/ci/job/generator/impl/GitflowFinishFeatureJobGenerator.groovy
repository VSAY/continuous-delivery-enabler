package com.liquidhub.framework.ci.job.generator.impl

import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.FEATURE_NAME
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.KEEP_FEATURE_BRANCH
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.SKIP_FEATURE_MERGE_TO_DEVELOP
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.SQUASH_COMMITS
import static com.liquidhub.framework.ci.view.ViewElementTypes.BOOLEAN_CHOICE
import static com.liquidhub.framework.ci.view.ViewElementTypes.READ_ONLY_BOOLEAN_CHOICE
import static com.liquidhub.framework.ci.view.ViewElementTypes.TEXT
import static com.liquidhub.framework.providers.jenkins.OperatingSystemCommandAdapter.adapt

import com.liquidhub.framework.ci.model.GitflowJobParameter
import com.liquidhub.framework.ci.model.JobGenerationContext
import com.liquidhub.framework.ci.model.ParameterListingScript
import com.liquidhub.framework.ci.view.ViewElementTypes
import com.liquidhub.framework.config.model.Configuration
import com.liquidhub.framework.config.model.JobConfig
import com.liquidhub.framework.scm.model.GitFlowBranchTypes
import com.liquidhub.framework.scm.model.SCMRemoteRefListingRequest
import com.liquidhub.framework.scm.model.SCMRepository

class GitflowFinishFeatureJobGenerator extends BaseGitflowJobGenerationTemplateSupport {


	@Override
	public def getJobConfig(Configuration configuration) {
		return configuration.gitflowFeatureBranchConfig.finishConfig
	}

	protected def defineJobParameters(JobGenerationContext context, JobConfig jobConfig){

		SCMRepository repository = context.scmRepository

		SCMRemoteRefListingRequest request = new SCMRemoteRefListingRequest(
				targetUrl: repository.repoUrl,
				authorizedUserDigest: repository.authorizedUserDigest,
				branchFilterText:  'feature/',
				)

		request.listFullRefNames=true
		def descriptionScript = branchNamesListingProvider.getScript(['requestParam':request])

		request.listFullRefNames=false
		def valueScript = branchNamesListingProvider.getScript(['requestParam':request])


		def parameters = []

		parameters << new GitflowJobParameter(
				name: FEATURE_NAME,
				description:  'Select the feature you intend to finish. If you see too many items, consider deleting the oldest completed features',
				elementType: ViewElementTypes.SINGLE_SELECT_CHOICES,
				valueListingScript: new ParameterListingScript(text: valueScript),
				labelListingScript: new ParameterListingScript(text: descriptionScript)
				)

		parameters << new GitflowJobParameter(name: KEEP_FEATURE_BRANCH,elementType: BOOLEAN_CHOICE, defaultValue:false)
		parameters << new GitflowJobParameter(name: SQUASH_COMMITS, elementType: BOOLEAN_CHOICE, defaultValue:false)
	}

	@Override
	def configureBuildSteps(JobGenerationContext ctx, JobConfig jobConfig){

		return {
			ctx.generatingOnWindows ? batchFile(adapt(CHECK_OUT_FEATURE)) : shell(CHECK_OUT_FEATURE)
			maven ctx.mavenBuildStepConfigurer().configure(ctx, jobConfig)
			ctx.generatingOnWindows ? batchFile(adapt(WRITE_LAST_COMMIT_CODE_TO_FILE)) : shell(WRITE_LAST_COMMIT_CODE_TO_FILE)
			environmentVariables{
				propertiesFile('finishfeature_env_properties') //This is the file we create in the previous step
				envs(['SCM_CHANGESET_URL': ctx.scmRepository.changeSetUrl])
			}
			
		}
	}


	@Override
	protected def determineRegularEmailSubject(JobGenerationContext ctx, JobConfig jobConfig){
		'Feature branch ${ENV, var="featureName"}  on '+ctx.repositoryName+' merged to develop'
	}

	@Override
	protected def determineFailureEmailSubject(JobGenerationContext ctx, JobConfig jobConfig){
		'Action Required !!! Failed to finish feature branch ${ENV, var="featureName"} on '+ctx.repositoryName
	}

	@Override
	protected def configureAdditionalPublishers(JobGenerationContext ctx, JobConfig jobConfig){

		//When feature finishes and the code is merged to develop, we trigger the develop branch ci job automatically
		def downstreamDevelopCIJobName = ctx.jobNameCreator.createJobName(ctx.repositoryName, GitFlowBranchTypes.DEVELOP, 'develop', ctx.configuration.continuousIntegrationConfig)
		
		return {
			downstreamParameterized {
				trigger(downstreamDevelopCIJobName, 'SUCCESS')
			}
		}

	}


		private static final String CHECK_OUT_FEATURE = 'git checkout feature/${featureName}'
		
		private static final String WRITE_LAST_COMMIT_CODE_TO_FILE = 'echo MERGE_COMMIT=$(echo `git log -1 --pretty=format:%h`) > finishfeature_env_properties'
	}
