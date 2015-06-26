package com.liquidhub.framework.scm


class ReleaseChoiceOptionsScriptProvider extends VersionDeterminationScriptProvider {

	@Override
	def getVersionChoicesScript(gitRepoUrl, authorizedUserDigest, releaseNamingStrategy){

		"""
           |import com.liquidhub.framework.git.ReleaseOptionsProvider
           |ReleaseOptionsProvider.proposePostReleaseDevelopmentMilestone('${gitRepoUrl}', '${authorizedUserDigest}', ${releaseNamingStrategy})
 
        """.stripMargin()
	}
	
	public static void main(String[] args){
		MilestoneVersionDeterminationScriptProvider provider = new MilestoneVersionDeterminationScriptProvider()
		println provider.getScript(['requestParam':[gitRepoUrl:'http://stash.ibx.com/scm/rca/roam-web.git',authorizedUserDigest: 'YWRtaW46YWRtaW4=','versionNamingStrategy':'m']])
	}
}
