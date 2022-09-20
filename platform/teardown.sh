#!/bin/bash

START=$SECONDS

# launch docker containers
echo ""
echo "******************************************************************"
echo "Stopping docker containers"
echo "******************************************************************"
echo ""
docker-compose down

# cleanup volumes
echo ""
echo "******************************************************************"
echo "Cleaning up volumes"
echo "******************************************************************"
echo ""
./cleanup.sh

DURATION=$(( SECONDS - START ))
echo ""
echo "Finished tearing down environment $ENV in $DURATION secs"
exit 0