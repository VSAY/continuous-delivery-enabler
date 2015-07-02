package com.liquidhub.framework.model

import groovy.transform.ToString

/**
 * Represents a maven artifact which contains all the coordinates required to uniquely identify it in a maven compliant repository
 * 
 * @author Rahul Mishra,LiquidHub
 *
 */
@ToString(includeNames=true)
class MavenArtifact {

	def groupId, artifactId, artifactVersion, packaging

}
