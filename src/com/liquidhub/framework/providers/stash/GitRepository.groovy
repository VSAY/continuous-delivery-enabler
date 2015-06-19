package com.liquidhub.framework.providers.stash


import groovy.transform.ToString

import com.liquidhub.framework.ci.model.SeedJobParameters
import com.liquidhub.framework.scm.model.GitFlowBranchTypes
import com.liquidhub.framework.scm.model.SCMRepository

@ToString(includeNames=true, includeFields=true)
class GitRepository implements SCMRepository {

	private def repoUrl,baseUrl,repoBranchName,projectKey,repositorySlug,authorizedUserDigest,changeSetUrl,branchType
	
	GitRepository(Map bindingVariables){

		def repoBranchName = bindingVariables[SeedJobParameters.REPO_BRANCH_NAME.bindingName]
		def repoUrl = bindingVariables[SeedJobParameters.SCM_REPO_URL.bindingName]
		def repositoryUserCredentials = bindingVariables[SeedJobParameters.REPO_ACCESS_CREDENTIALS.bindingName]
		def repositoryType = bindingVariables[SeedJobParameters.REPO_IMPLEMENTATION.bindingName] ?: 'STASH'//By default, we assume its a stash repository
		
		def gitRepoURLPattern = GitRepositories.valueOf(repositoryType.toUpperCase()).urlPattern

		def matcher = ( repoUrl =~  gitRepoURLPattern )//Git URL Repo Pattern

		if(!matcher.matches()){
			throw new RuntimeException('Not a Standard Git URL. The URL provided is'+repoUrl+'. The provided URL did not match the configure pattern '+gitRepoURLPattern);
		}

		if(matcher[0].size() != 5){ //We expect to find five matches - the whole pattern, the transport scheme, the base url, the project key and the repository slug
			throw new RuntimeException("Could not parse the git url to create a stash client. The git repo url must be of the format 'http://stash.ibx.com/scm/projectKey/repoName.git'");
		}


		def transportScheme = matcher[0][1]
		this.baseUrl = matcher[0][1]+matcher[0][2]
		this.repoBranchName = repoBranchName
		this.projectKey = matcher[0][3]
		this.repositorySlug = matcher[0][4]
		this.authorizedUserDigest= repositoryUserCredentials.bytes.encodeBase64()
		this.repoUrl = repoUrl
		this.changeSetUrl = createChangeSetUrl(baseUrl, projectKey, repositorySlug)
		this.branchType = GitFlowBranchTypes.type(repoBranchName)
	
	}

	protected def createChangeSetUrl(baseUrl, projectKey, repositorySlug){
		return "${baseUrl}/projects/${projectKey}/repos/${repositorySlug}"
	}

	@Override
	public def getRepoUrl() {
		this.repoUrl
	}

	@Override
	public def getBaseUrl() {
		this.baseUrl
	}

	@Override
	public def getRepoBranchName() {
		this.repoBranchName
	}

	@Override
	public def getProjectKey() {
		this.projectKey
	}

	@Override
	public def getRepositorySlug() {
		this.repositorySlug
	}

	@Override
	public def getAuthorizedUserDigest() {
		this.authorizedUserDigest
	}

	@Override
	public def getChangeSetUrl() {
		this.changeSetUrl
	}
	
	@Override
	public def getBranchType() {
		this.branchType
	}
	
	@Override
	public def getCredentialsId() {
		// TODO Auto-generated method stub
		return null;
	}


	private enum GitRepositories {


		STASH('(http://)(.*)/scm/([A-z0-9]+)/(.*).git')
		

		public GitRepositories(urlPattern){
			this.urlPattern = urlPattern
		}

		def urlPattern


	}


	

}
