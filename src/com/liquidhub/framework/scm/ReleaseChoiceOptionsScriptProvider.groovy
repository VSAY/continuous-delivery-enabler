package com.liquidhub.framework.scm


class ReleaseChoiceOptionsScriptProvider extends VersionDeterminationScriptProvider {

	@Override
	def getVersionChoicesScript(gitRepoUrl, authorizedUserDigest, releaseNamingStrategy){

		"""
           |import com.ibx.frontoffice.stash.utils.GitRepositoryReleaseOptionsProvider
           |def optionsProvider = new GitRepositoryReleaseOptionsProvider('${authorizedUserDigest}')
           |optionsProvider.provideChoicesForNextRelease('${gitRepoUrl}', ${releaseNamingStrategy})
 
        """.stripMargin()
	}
}
