package com.liquidhub.framework.config.model

import groovy.transform.ToString

@ToString(includeNames=true, includePackage=false)
class RoleConfig {

	def developerRole, releaseManagerRole,deploymentManagerRole, projectAdminRole, qaAdminRole


	def merge(RoleConfig otherRoleConfig){
		
		if(!otherRoleConfig)return this
		
		[
			'developerRole',
			'releaseManagerRole',
			'deploymentManagerRole',
			'projectAdminRole',
			'qaAdminRole'
		].each{property ->
			this[property] = otherRoleConfig[property] ?:  this[property]
		}
	}
}
