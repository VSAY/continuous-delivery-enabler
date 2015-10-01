ssh -i /opt/apps/jenkins/.ssh/id_rsa_jenkssh jenkssh@${deploymentHost} 'sudo su - liferay -c "/opt/apps/scripts/deploy.sh --deployDir ${deployDir} --groupId ${groupId} --artifactId ${artifactId} --version ${version}"'

