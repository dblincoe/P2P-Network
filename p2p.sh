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
		ssh $server "cd ./P2P-Network; ./p2p.sh ${1}" > /dev/null 2>&1
		echo "Finished Running Commands on ${server}"
	done
}

if [ "$1" == "remote_pull" ]
then
	run_remote_commands "local_pull"
elif [ "$1" == "local_pull" ]
then
	git pull origin master > out.log
elif [ "$1" == "help" ] || [ -z "$1" ]
then
	echo -e '\n'
	echo -e 'To see this help page again the help command can be used!'
	echo -e '\n'

	echo 'Remote Commands: '
	echo -e '\t remote_pull: pulls the latest code remotely'
	echo -e '\t remote_compile: compiles code remotely'
	echo ''
	echo -e '\t remote_start: starts p2p network remotely'
	echo -e '\t remote_stop: stops p2p network remotely'
	echo -e '\n'

	echo 'Local Commands: '
	echo -e '\t local_pull: pulls the latest code on a local host'
	echo -e '\t local_compile: compiles code on a local host'
	echo ''
	echo -e '\t local_start: starts p2p network locally'
	echo -e '\t local_stop: stops p2p network locally'

	echo -e '\n'
fi
