#! /bin/bash
set -x
botsFolder=/home/pi/espace/twinbots
logsToKeep=10
for folder in `ls ${botsFolder}`; do
	cd ${botsFolder}/${folder}/logs; (ls -t|head -n ${logsToKeep};ls)|sort|uniq -u|xargs --no-run-if-empty rm -rf
done
