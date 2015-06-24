package com.liquidhub.framework.providers.jenkins

import com.liquidhub.framework.JobSectionConfigurer
import com.liquidhub.framework.ci.model.JobGenerationContext
import com.liquidhub.framework.config.model.JobConfig

class OperatingSystemCommandAdapter{

	private static final boolean windowsOS = (System.getenv()['OS']?.toLowerCase() =~ /.*windows.*/).matches() //If the OS belongs to the windows family
	
	
	public static String adapt(cmd, parameters=[:]){
		//Unix  substitution characters ${var} needs to be replaced by windows substitution characters %var%
		substituteParameters(cmd, parameters){command ->
			command = windowsOS ? command.replace('${','%').replace('}','%') : command
			
		}
	}


	private static def substituteParameters(command, parameters, adapter){
		parameters.each{ k,v ->
			command = command.replace("#${k}#", v)
		}
		return adapter(command)
	}

	public static void main(String[] args){

		def cmd = 'git checkout develop'

		def adapter = new OperatingSystemCommandAdapter()
		
		def writer = new StringWriter()
		def builder = new groovy.xml.MarkupBuilder(writer)
		
		def body = adapter.adapt(cmd)
		 println body
	}

}
