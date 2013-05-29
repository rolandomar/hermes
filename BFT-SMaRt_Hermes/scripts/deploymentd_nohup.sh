#!/bin/bash
cp ../BFT-SMaRt.jar dist/BFT-SMaRt.jar
nohup java -cp ../Hermes/dist/Hermes.jar hermes.deployment.DeploymentDaemon 0&
