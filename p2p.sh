#!/bin/bash

SERVER_LIST=`cat ./servers.txt`
run_remote_commands() {
	if [ -z "$1" ]
	then
		return
	fi

	for server in $SERVER_LIST
	do
		echo -e "\nConnecting to ${server}"
		ssh $server "cd ./P2P-Network; ./p2p.sh ${1}" > /dev/null 2>&1
		echo "Finished Running Commands on ${server}"
	done
}

if [ "$1" == "remote_pull" ]
then
	echo "Pulling git repo..."
	run_remote_commands "local_pull"

elif [ "$1" == "remote_compile" ]
then
	echo "Compiling code remotely"
	run_remote_commands "local_compile"

elif [ "$1" == "local_pull" ]
then
	git pull origin master > out.log
	echo "Pulled local git repo"

elif [ "$1" == "local_compile" ]
then
	javac *.java > out.log
	echo "Compiled code locally"

elif [ "$1" == "help" ] || [ -z "$1" ]
then
	echo -e ''
	echo -e 'General Commands: '
	echo -e '\t help: load the help page'
	echo -e ''

	echo 'Remote Commands: '
	echo -e '\t remote_pull: pulls the latest code remotely'
	echo -e '\t remote_compile: compiles code remotely'
	echo -e '\t remote_start: starts p2p network remotely'
	echo -e '\t remote_stop: stops p2p network remotely'
	echo -e ''

	echo 'Local Commands: '
	echo -e '\t local_pull: pulls the latest code on a local host'
	echo -e '\t local_compile: compiles code on a local host'
	echo -e '\t local_start: starts p2p network locally'
	echo -e '\t local_stop: stops p2p network locally'

	echo -e ''
fi
