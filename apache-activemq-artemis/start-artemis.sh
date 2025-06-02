#!/bin/bash

set -e

echo "*******************ARTEMIS_USERNAME is: $ARTEMIS_USERNAME"
echo "*******************ARTEMIS_PASSWORD is: $ARTEMIS_PASSWORD"

#if [ ! -f /var/lib/artemis/etc/artemis.profile ]; then
  echo "Creating Artemis broker instance..."
  artemis create /var/lib/artemis \
    --user "$ARTEMIS_USERNAME" \
    --password "$ARTEMIS_PASSWORD" \
    --silent
#fi



echo "*******************ARTEMIS_USERNAME is: $ARTEMIS_USERNAME"
echo "*******************ARTEMIS_PASSWORD is: $ARTEMIS_PASSWORD"

echo "Starting Artemis broker..."
exec /var/lib/artemis/bin/artemis run