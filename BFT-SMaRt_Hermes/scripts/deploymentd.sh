#!/bin/bash
cp ../BFT-SMaRt.jar dist/BFT-SMaRt.jar
java -cp ../Hermes/dist/Hermes.jar hermes.deployment.DeploymentDaemon
