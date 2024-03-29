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
import com.liquidhub.framework.scm.model.SCMRemoteRefListingRequest
import com.liquidhub.framework.scm.model.SCMRepository

class GitflowFinishHotfixJobGenerator extends GitflowFinishReleaseJobGenerator{

	@Override
	def getJobConfig(Configuration configuration){
		configuration.gitflowHotfixBranchConfig.finishConfig
	}

	@Override
	def defineJobParameters(JobGenerationContext context, JobConfig jobConfig){

		def parameters = []

		SCMRepository repository = context.scmRepository

		def repoUrl = repository.repoUrl, authDigest = repository.authorizedUserDigest

		SCMRemoteRefListingRequest request = new SCMRemoteRefListingRequest(targetUrl: repoUrl, authorizedUserDigest: authDigest, branchFilterText:  'hotfix/')

		request.listFullRefNames=true
		def descriptionScript = branchNamesListingProvider.getScript(['requestParam':request])

		request.listFullRefNames=false
		def valueScript = branchNamesListingProvider.getScript(['requestParam':request])

		parameters << new GitflowJobParameter(name: HOTFIX_BRANCH,
		valueListingScript: new ParameterListingScript(text:valueScript),
		labelListingScript: new ParameterListingScript(text:descriptionScript),
		elementType: ViewElementTypes.SINGLE_SELECT_CHOICES)

		parameters << new GitflowJobParameter(name: DEVELOPMENT_VERSION,description : 'What do you want the next development version to be(post hotfix merge)?', elementType:TEXT)
		parameters << new GitflowJobParameter(name: KEEP_HOTFIX_BRANCH,elementType:READ_ONLY_BOOLEAN_CHOICE, defaultValue:true)//Has to be a read only flag because if the branch is not kept the job fails.Need to investigate
		parameters << new GitflowJobParameter(name: SQUASH_COMMITS, elementType:BOOLEAN_CHOICE, defaultValue:false)

		parameters << [ALLOW_SNAPSHOTS_WHILE_FINISHING_HOTFIX].collect{
			new GitflowJobParameter(name: it, elementType: READ_ONLY_BOOLEAN_CHOICE, defaultValue: false)
		}
	}


	@Override
	def configureBuildSteps(JobGenerationContext ctx, JobConfig jobConfig){

		def mvnConfigurer = ctx.configurers('maven')

		def configureMavenCommand = {goals ->

			def config = goals instanceof JobConfig ? goals :  ['goals': goals, 'activeMavenProfiles':jobConfig?.activeMavenProfiles] as JobConfig

			mvnConfigurer.configure(ctx, config)
		}


		return{
			
			
			ctx.generatingOnWindows ? batchFile(adapt(CHECK_OUT_DEVELOP)) : shell(CHECK_OUT_DEVELOP)
			maven configureMavenCommand(UPGRADE_DEVELOPMENT_VERSION)
			ctx.generatingOnWindows ? batchFile(adapt(COMMIT_DEVELOP_POM)) : shell(COMMIT_DEVELOP_POM)
			ctx.generatingOnWindows ? batchFile(adapt(CHECK_OUT_HOTFIX)) : shell(CHECK_OUT_HOTFIX)
			maven configureMavenCommand(jobConfig)
			ctx.generatingOnWindows ? batchFile(adapt(WRITE_MERGE_COMMITS_TO_FILE)) : shell(WRITE_MERGE_COMMITS_TO_FILE)
			environmentVariables{
				propertiesFile('finishhotfix_env_properties') //This is the file we create in the previous step
				envs(['SCM_CHANGESET_URL': ctx.scmRepository.changeSetUrl])
			}
			ctx.generatingOnWindows ? batchFile(adapt(CHECK_OUT_MASTER)) : shell(CHECK_OUT_MASTER)
			maven configureMavenCommand('deploy')
			
		}
	}

	@Override
	protected def determineRegularEmailSubject(JobGenerationContext ctx, JobConfig jobConfig){
		'hotfix/${ENV, var="hotfixBranch"}  on '+ctx.repositoryName+' repository has merged to master and develop'
	}

	@Override
	protected def determineFailureEmailSubject(JobGenerationContext ctx, JobConfig jobConfig){
		'Action Required !!! Failed to finish hotfix branch ${ENV, var="hotfixBranch"} on '+ctx.repositoryName
	}

	/**
	 * @return the name of the branch which should be used to build the source code
	 */
	protected def identifySCMBranchForBuild(JobGenerationContext ctx){
		'hotfix/${hotfixBranch}'
	}


	/*
	 * Hotfix branch merges to master first, so the first merge commit in history is the merge of MASTER and RELEASE branch, so we skip 1 and request the second latest merge commit
	 *  Hotfix branch merges to develop/release branch next, so the next merge commit in history is the merge of MASTER and DEVELOP branch, so we skip none and request the latest merge commit
	 */
	private static final String WRITE_MERGE_COMMITS_TO_FILE = 
	   '''
	   	  echo MASTER_MERGE_COMMIT=$(echo `git log --max-count=1 --skip=1 --merges --pretty=format:%h`) > finishhotfix_env_properties
	   	  echo DEVELOP_MERGE_COMMIT=$(echo `git log --max-count=1 --skip=0 --merges --pretty=format:%h`) >> finishhotfix_env_properties'''
	
			 
	static final def UPGRADE_DEVELOPMENT_VERSION='versions:set -DnewVersion=${developmentVersion} versions:commit'
	
	static final def COMMIT_DEVELOP_POM = 'git commit pom.xml -m "Updated develop pom version during hotfix finish"'
	
			 

	private static final def CHECK_OUT_HOTFIX = 'git checkout hotfix/${hotfixBranch}'
	
	private static final def CHECK_OUT_DEVELOP = 'git checkout develop'
	
	private static final def CHECK_OUT_MASTER = 'git checkout master'
}
