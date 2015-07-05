ssh srv-cibuilder@${deploymentManager}
/apps/scripts/deploy.sh -r $repositorySegment -g ${groupId} -a ${artifactId} -v ${version} -p ${packaging} -c ${contextRoot} --restart -t ${targetJVMName} -n ${cellName}
