package com.liquidhub.framework.ci.job.generator.impl

import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.FEATURE_NAME
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.KEEP_FEATURE_BRANCH
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.SKIP_FEATURE_MERGE_TO_DEVELOP
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.SQUASH_COMMITS
import static com.liquidhub.framework.ci.model.GitflowJobParameterNames.UPDATE_TO_LATEST_VERSIONS

import static com.liquidhub.framework.ci.view.ViewElementTypes.BOOLEAN_CHOICE
import static com.liquidhub.framework.ci.view.ViewElementTypes.READ_ONLY_BOOLEAN_CHOICE
import static com.liquidhub.framework.ci.view.ViewElementTypes.TEXT
import static com.liquidhub.framework.ci.model.JobPermissions.ItemBuild
import static com.liquidhub.framework.ci.model.JobPermissions.ItemCancel
import static com.liquidhub.framework.ci.model.JobPermissions.ItemConfigure
import static com.liquidhub.framework.ci.model.JobPermissions.ItemDiscover
import static com.liquidhub.framework.ci.model.JobPermissions.ItemRead
import static com.liquidhub.framework.ci.model.JobPermissions.ItemWorkspace
import static com.liquidhub.framework.ci.model.JobPermissions.RunDelete
import static com.liquidhub.framework.ci.model.JobPermissions.RunUpdate


import static com.liquidhub.framework.providers.jenkins.OperatingSystemCommandAdapter.adapt

import java.util.Map;

import com.liquidhub.framework.ci.model.GitflowJobParameter
import com.liquidhub.framework.ci.model.JobGenerationContext
import com.liquidhub.framework.ci.model.ParameterListingScript
import com.liquidhub.framework.ci.view.ViewElementTypes
import com.liquidhub.framework.config.model.Configuration
import com.liquidhub.framework.config.model.JobConfig
import com.liquidhub.framework.config.model.RoleConfig;
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
		parameters << new GitflowJobParameter(name: UPDATE_TO_LATEST_VERSIONS,elementType: BOOLEAN_CHOICE, defaultValue:true)
	}

	@Override
	def configureBuildSteps(JobGenerationContext ctx, JobConfig jobConfig){

		def configureMavenCommand = {goals ->

			def config = goals instanceof JobConfig ? goals :  ['goals': goals] as JobConfig

			ctx.configurers('maven').configure(ctx, config)
		}

		return {

			//boolean condition expression which helps determine if we should upgrade internal dependencies automatically
			//The definition of internal is maintained in parent pom
			def upgradeInternalVersionsAutomatically = '${ENV,var="updateInternalPOMDependenciesToLatestVersions"}'

			conditionalSteps{
				//Run the use-latest-versions check only if the user made the selection
				condition{ booleanCondition(upgradeInternalVersionsAutomatically) }

				runner("DontRun") //For any other values, look at runner classes of Run Condition Plugin

				maven configureMavenCommand(BUMP_POM_DEPENDENCY_VERSIONS_IF_NEWER_EXIST)
				
				ctx.generatingOnWindows ? batchFile(adapt(COMMIT_DEPENDENCY_VERSION_UPGRADES_IF_ANY)) : shell(COMMIT_DEPENDENCY_VERSION_UPGRADES_IF_ANY)
			}


			maven configureMavenCommand(jobConfig)
			ctx.generatingOnWindows ? batchFile(adapt(WRITE_LAST_COMMIT_CODE_TO_FILE)) : shell(WRITE_LAST_COMMIT_CODE_TO_FILE)




			environmentVariables{
				propertiesFile('finishfeature_env_properties') //This is the file we create in the previous step
				envs(['SCM_CHANGESET_URL': ctx.scmRepository.changeSetUrl])
			}

		}
	}


	@Override
	protected def determineRegularEmailSubject(JobGenerationContext ctx, JobConfig jobConfig){
		'Feature branch [ ${ENV, var="featureName"} ]  on '+ctx.repositoryName+' repository merged to develop'
	}

	@Override
	protected def determineFailureEmailSubject(JobGenerationContext ctx, JobConfig jobConfig){
		'Action Required !!! Failed to finish feature branch ${ENV, var="featureName"} on '+ctx.repositoryName
	}

	/**
	 * @return the name of the branch which should be used to build the source code
	 */
	protected def identifySCMBranchForBuild(JobGenerationContext ctx){
		'feature/${featureName}'
	}

	@Override
	protected Map grantAdditionalPermissions(JobGenerationContext ctx,RoleConfig roleConfig){
		def parameters = [:]
		parameters.put(roleConfig.projectAdminRole, [ItemBuild, ItemCancel, ItemDiscover, ItemRead, RunUpdate, RunDelete, ItemWorkspace])
		return parameters
	}

	@Override
	protected def configureAdditionalPublishers(JobGenerationContext ctx, JobConfig jobConfig){

		//When feature finishes and the code is merged to develop, we trigger the develop branch ci job automatically
		def downstreamDevelopCIJobName = ctx.jobNameCreator.createJobName(ctx.repositoryName, GitFlowBranchTypes.DEVELOP, 'develop', ctx.configuration.continuousIntegrationConfig)

		final boolean triggerWithNoParameters = true

		return {
			downstreamParameterized {
				trigger(downstreamDevelopCIJobName, 'SUCCESS', triggerWithNoParameters)
			}
		}

	}


	private static final String BUMP_POM_DEPENDENCY_VERSIONS_IF_NEWER_EXIST = 'versions:use-latest-versions -DallowSnapshots=true versions:commit'

	private static final String COMMIT_DEPENDENCY_VERSION_UPGRADES_IF_ANY = '''

			if ! git diff --quiet HEAD ; then
			  git checkout feature/${featureName}
			  git commit pom.xml -m 'Automatically Upgrading internal dependency versions during feature finish' 
			fi
	'''

	private static final String WRITE_LAST_COMMIT_CODE_TO_FILE = 'echo MERGE_COMMIT=$(echo `git log -1 --merges --pretty=format:%h`) > finishfeature_env_properties'
}
