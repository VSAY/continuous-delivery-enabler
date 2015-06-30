package com.liquidhub.framework.scm


class MilestoneReleaseVersionScriptProvider extends VersionDeterminationScriptProvider {

	@Override
	def getVersionDeterminationScript(gitRepoUrl, authorizedUserDigest, releaseNamingStrategy){

		"""
           |import com.liquidhub.framework.git.MilestoneReleaseVersionsProvider
           |MilestoneReleaseVersionsProvider.determineNextMilestoneReleaseVersion('${gitRepoUrl}', '${authorizedUserDigest}', ${releaseNamingStrategy})
 
        """.stripMargin()
	}
	
	public static void main(String[] args){
		MilestoneReleaseVersionScriptProvider provider = new MilestoneReleaseVersionScriptProvider()
		println provider.getScript(['requestParam':[gitRepoUrl:'http://stash.ibx.com/scm/rca/roam-web.git',authorizedUserDigest: '','versionNamingStrategy':'m']])
	}
}
