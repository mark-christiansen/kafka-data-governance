#!/bin/bash

START=$SECONDS

# cleanup volumes
echo ""
echo "******************************************************************"
echo "Cleaning up volumes"
echo "******************************************************************"
echo ""
./cleanup.sh

# launch docker containers
echo ""
echo "******************************************************************"
echo "Starting docker containers"
echo "******************************************************************"
echo ""
docker-compose up -d

DURATION=$(( SECONDS - START ))
echo ""
echo "Finished setup of environment $ENV in $DURATION secs"
exit 0