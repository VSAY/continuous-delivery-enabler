package com.liquidhub.framework.providers.jenkins

import com.liquidhub.framework.ci.OSCommandAdapter

class JenkinsOSCommandAdapter implements OSCommandAdapter{

	private final boolean windowsOS = (System.getenv()['OS']?.toLowerCase() =~ /.*windows.*/).matches() //If the OS belongs to the windows family


	def adapt(cmd, parameters=[:]){

		//Unix  substitution characters ${var} needs to be replaced by windows substitution characters %var%
		substituteParameters(cmd, parameters){command ->
			command = windowsOS ? command.replace('${','%').replace('}','%') : command
			println command
        	windowsOS ? { batchFile(command) } : { shell(command) }
		}
	}


	protected def substituteParameters(command, parameters, adapter){
		parameters.each{ k,v ->
			command = command.replace("#${k}#", v)
		}
		return adapter(command)
	}

	public static void main(String[] args){

		def cmd = '''
			|git tag ${releaseVersion}
			|git push #releasePushUrl#  HEAD:release/${releaseBranch}
			|git push #releasePushUrl# ${releaseVersion}
		'''.stripMargin()

		Map parameters = ['releasePushUrl' : 'yahoo']

		def adapter = new JenkinsOSCommandAdapter()

		println adapter.adapt(cmd, parameters)
	}

}
