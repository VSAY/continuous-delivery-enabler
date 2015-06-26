package com.liquidhub.framework.ci.model

enum GitflowJobParameterNames {

	CONFIGURE_BRANCH_JOBS('configureBranchJobs', CONFIGURE_BRANCH_JOB_PARAM_DESCRIPTION),
	FEATURE_NAME('featureName'),
	START_COMMIT('startCommit'),
	RELEASE_FROM_BRANCH('releaseFromBranch'),
	RELEASE_BRANCH('releaseBranch'),
	RELEASE_VERSION('releaseVersion','Default version to use when starting the branch'),
	DEVELOPMENT_VERSION('developmentVersion', 'Default development version to use when finishing the branch'),
	NEXT_MILESTONE_DEV_VERSION('nextMilestoneDevelopmentVersion'),

	
	HOTFIX_BRANCH('hotfixBranch','The hotfix you intend to finish'),
	HOTFIX_VERSION('hotfixVersion'),
	
	
	
	ALLOW_SNAPSHOTS_WHILE_CREATING_FEATURE('allowSnapshots','Should we proceed with creating feature branches even if there are snapshot depenendencies in develop ? '),
	ALLOW_SNAPSHOTS_WHILE_CREATING_HOTFIX('allowSnapshots','Should we proceed with creating hotfix branches even if there are snapshot depenendencies ? '),
	ALLOW_SNAPSHOTS_WHILE_CREATING_RELEASE('allowSnapshots','Should we proceed with creating the release branch even if there are snapshot depenendencies? '),
	ALLOW_SNAPSHOTS_WHILE_FINISHING_RELEASE('allowSnapshots','Should we proceed with finishing the release branch even if there are snapshot depenendencies? '),
	ALLOW_SNAPSHOTS_WHILE_FINISHING_HOTFIX('allowSnapshots','Should we proceed with finishing the hotfix branch even if there are snapshot depenendencies? '),
	PUSH_RELEASES('pushReleases','Push release branches to the remote upstream'),
	RELEASE_BRANCH_VERSION_SUFFIX('releaseBranchVersionSuffix','Suffix to append to versions on the release branch.'),
	ENABLE_FEATURE_VERSIONS('enableFeatureVersions', 'Append the feature name to the maven pom version on the feature branch.'),
	PUSH_FEATURES('pushFeatures', 'Push the feature branch to the remote upstream once it is created'),
	PUSH_HOTFIXES('pushHotfixes', 'Push this hotfix branch to repository'),
	KEEP_FEATURE_BRANCH('keepBranch', 'Keep the feature branch after finishing the feature/merging it to develop'),
	KEEP_RELEASE_BRANCH('keepBranch', 'Keep the release branch after finishing the release. You MUST delete it manually before you can create the next release branch'),
	KEEP_HOTFIX_BRANCH('keepBranch', 'Keep the hotfix branch after finishing the hotfix. You MUST delete it manually before you can create the next hotfix branch'),
	SKIP_FEATURE_MERGE_TO_DEVELOP('noFeatureMerge','Turn off merging changes from this feature branch to develop'),
	SQUASH_COMMITS('squashCommits','Squash all commits made on this branch into a single commit before merge'),
	SKIP_RELEASE_BRANCH_MERGE('noReleaseMerge', 'Turn off merging changes from the release branch to master and develop'),
	SKIP_RELEASE_TAGGING('noTag', 'Turn off tagging the release in git'),
	RELEASE_TAG_MESSAGE('tagMessage','Commit message to use when tagging the release.'),
	NO_DEPLOY('noDeploy','Turn off maven deployment. If false the "deploy" goal is called. If true the "install" goal is called'),
	SKIP_HOTFIX_TAGGING('noTag', 'Turn off tagging the hotfix in git'),
	

	GitflowJobParameterNames(parameterName, description=''){
		this.parameterName = parameterName
		this.description = description
	}

	def parameterName, description


	private static final CONFIGURE_BRANCH_JOB_PARAM_DESCRIPTION ='''
					|Do you want to generate the jobs for this branch after the branch is created? You can always create them later
					|using our job seeder, just pick the repository and enter the branch name.
					'''.stripMargin()
}
