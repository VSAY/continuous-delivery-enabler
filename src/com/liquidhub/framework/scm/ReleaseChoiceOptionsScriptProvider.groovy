package com.liquidhub.framework.scm


class ReleaseChoiceOptionsScriptProvider extends VersionDeterminationScriptProvider {

	@Override
	def getVersionChoicesScript(gitRepoUrl, authorizedUserDigest, releaseNamingStrategy){

		"""
           |import com.liquidhub.framework.git.ReleaseOptionsProvider
           |ReleaseOptionsProvider.provideChoicesForNextRelease('${gitRepoUrl}', '${authorizedUserDigest}', ${releaseNamingStrategy})
 
        """.stripMargin()
	}
	
	public static void main(String[] args){
		ReleaseChoiceOptionsScriptProvider provider = new ReleaseChoiceOptionsScriptProvider()
		println provider.getScript(['requestParam':[gitRepoUrl:'http://stash.ibx.com/scm/rca/roam-web.git',authorizedUserDigest: '','versionNamingStrategy':'m']])
	}
}
