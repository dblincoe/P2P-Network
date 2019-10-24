#!/bin/bash

SERVER_LIST=`cat ./servers.txt`
if [ "$1" == "remote_pull" ]
then
	for server in $SERVER_LIST
	do
		echo -e "\nConnecting to ${server}"
		ssh $server "cd ./p2p; rm -rf ./src" > /dev/null 2>&1
		scp -r ./src/ $server:/home/drb133/p2p
		echo "Finished copying sources to ${server}"
	done

elif [ "$1" == "remote_compile" ]
then
	for server in $SERVER_LIST
	do
		echo -e "\nConnecting to ${server}"
		ssh $server "cd ./p2p; javac -d ./ ./src/*.java" > /dev/null 2>&1
		echo "Finished Compiling on ${server}"
	done
elif [ "$1" == "help" ] || [ -z "$1" ]
then
	echo -e ''
	echo -e 'General Commands: '
	echo -e '\t help: load the help page'
	echo -e ''

	echo 'Remote Commands: '
	echo -e '\t remote_pull: pulls the latest code remotely'
	echo -e '\t remote_compile: compiles code remotely'
	echo -e ''
fi
