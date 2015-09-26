#! /bin/bash

echo "starting the twitter win bot"

# Declaring a bunch of bot variables
botName=$1
botsFolder=/home/pi/espace/twinbots
botFolder=${botsFolder}/${botName}
gitSource=/home/pi/espace/projects/twttwinbot
jarName=TwitterBot-1.0-SNAPSHOT.jar
logFile=${botFolder}/logs/`date +%Y%m%d-%H%M`.log

# Checking that the bot is correct and that its folder exists
if [ -z ${botName} ] || [ ! -d ${botFolder} ]; then
	echo "Unknown bot folder: ${botFolder}"
	exit 1
fi

# Checking that the bot folder is a valid folder
if [ ! -f ${botFolder}/scheduling.properties ] || [ ! -f ${botFolder}/bot.properties ]; then
	echo "Bot folder ${botFolder} is missing properties"
	exit 1
fi

# Git check for updates and build if necessary
cd ${gitSource}
if [ ! -z "`git diff-index --quiet HEAD --`" ]; then
	echo "Updating bot to latest version"
	cd ${gitSource}
	git reset --hard HEAD |& tee -a ${logFile}
	git pull |& tee -a ${logFile}

	# Build
	echo "Building up-to-date bot version"
	cd ${gitSource}
	mvn clean install -Dmaven.test.skip |& tee -a ${logFile}
fi

# Setting bot executable
if [ ! -L ${botFolder}/${jarName} ]; then
	ln -s ${gitSource}/target/${jarName} ${botFolder}/${jarName} |& tee -a ${logFile}
fi

# Run the bot
cd ${botFolder}; java -jar ${botFolder}/${jarName} |& tee -a ${logFile}
