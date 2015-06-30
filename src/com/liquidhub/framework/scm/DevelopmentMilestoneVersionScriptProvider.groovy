package com.liquidhub.framework.scm

class DevelopmentMilestoneVersionScriptProvider extends VersionDeterminationScriptProvider {


	@Override
	public Object getVersionDeterminationScript(gitRepoUrl, authorizedUserDigest, milestoneNamingStrategy) {
		"""
           |import com.liquidhub.framework.git.MilestoneReleaseVersionsProvider 
           |MilestoneReleaseVersionsProvider.determineNextMilestoneDevelopmentVersion('${gitRepoUrl}', '${authorizedUserDigest}', ${milestoneNamingStrategy})
 
        """.stripMargin()
	}

	public static void main(String[] args){
		DevelopmentMilestoneVersionScriptProvider provider = new DevelopmentMilestoneVersionScriptProvider()
		println provider.getScript(['requestParam':[gitRepoUrl:'http://stash.ibx.com/scm/rca/roam-web.git',authorizedUserDigest: '','versionNamingStrategy':'m']])
	}
}
