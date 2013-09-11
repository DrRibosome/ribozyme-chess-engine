#!/bin/sh
# host chess server
# requires ncat (installed via nmap)

# create pipe

pipe=/tmp/chess-sever-pipe

if [[ ! -p $pipe ]]; then
	mkfifo $pipe
fi

# host sever

cat $pipe | ncat --keep-open -l -p 8888 | java -jar ribozyme.jar > $pipe

# clean up afterwards
rm $pipe
