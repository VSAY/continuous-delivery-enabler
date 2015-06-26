package com.liquidhub.framework.scm

class MilestoneVersionDeterminationScriptProvider extends VersionDeterminationScriptProvider {


	@Override
	public Object getVersionChoicesScript(gitRepoUrl, authorizedUserDigest, milestoneNamingStrategy) {
		"""
           |import com.liquidhub.framework.git.ReleaseOptionsProvider 
           |ReleaseOptionsProvider.proposePostReleaseDevelopmentMilestone('${gitRepoUrl}', '${authorizedUserDigest}', ${milestoneNamingStrategy})
 
        """.stripMargin()
	}

	public static void main(String[] args){
		MilestoneVersionDeterminationScriptProvider provider = new MilestoneVersionDeterminationScriptProvider()
		println provider.getScript(['requestParam':[gitRepoUrl:'http://stash.ibx.com/scm/rca/roam-web.git',authorizedUserDigest: '','versionNamingStrategy':'m']])
	}
}
