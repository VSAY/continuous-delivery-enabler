package com.liquidhub.framework.providers.stash

import org.junit.Test

import com.liquidhub.framework.ci.model.SeedJobParameters

class GitRepositoryTest {


	@Test
	public void gitRepositorySetup() {

		def variables = [
			'repoBranchName' : 'master',
			'gitRepoUrl' : 'http://stash.test.ibx.com/scm/mvn/parent-pom.git',
			'repositoryUserCredentials' : 'STASH_ACCESS',
		]
		GitRepository gitRepository = new GitRepository(variables)
		println gitRepository
	}
}
