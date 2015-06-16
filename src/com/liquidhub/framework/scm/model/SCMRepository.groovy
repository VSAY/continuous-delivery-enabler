package com.liquidhub.framework.scm.model

interface SCMRepository {
	
	def getRepoUrl() // The original clone url
	def getBaseUrl()  //The base URL including the the protocol scheme,host name, and context root (if any)
	def getRepoBranchName() //The branch of the repository for which CI enablement has to be done
	def getProjectKey() //A logical and unique identifier for the aggregate collection to which this repository belongs
	def getRepositorySlug()//A semantic name for the repository which is embedded in the repo url
	def getAuthorizedUserDigest() //The a base 64 encoded digest of the username password style credentials
	def getChangeSetUrl() //The url which shows the diff for the repository
	def getBranchType()
	def getCredentialsId() //A reference to a pre configured credentials identifier on the system
	
	
}
