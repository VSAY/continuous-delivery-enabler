package com.liquidhub.framework.scm

class MilestoneVersionDeterminationScriptProvider extends VersionDeterminationScriptProvider {


	@Override
	public Object getVersionChoicesScript(gitRepoUrl, authorizedUserDigest, milestoneNamingStrategy) {
		"""
           |import com.ibx.frontoffice.stash.utils.GitRepositoryReleaseOptionsProvider
           |def optionsProvider = new GitRepositoryReleaseOptionsProvider('${authorizedUserDigest}')
           |optionsProvider.proposePostReleaseDevelopmentMilestone('${gitRepoUrl}', ${milestoneNamingStrategy})
 
        """.stripMargin()
	}
}
