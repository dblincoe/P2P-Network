#!/bin/bash

SERVER_LIST=`cat ./servers.txt`
run_remote_commands() {
	if [ -z "$1" ]
	then
		return
	fi

	echo "Will run commands:"
	echo "P2P Command: ${1}"

	for server in $SERVER_LIST
	do
		echo "Connecting to ${server}"
		ssh $server "cd ./P2P-Network; ./p2p.sh ${1}"> out.log
		echo "Finished Running Commands on ${server}"
	done
}

if [ "$1" == "remote_pull" ]
then
	run_remote_commands "local_pull"
elif [ "$1" == "local_pull" ]
then
	git pull origin master
fi
