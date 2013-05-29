#!/usr/bin/env python
import os
import sys
from subprocess import *
import string
import time
import shutil 

trials=16

if len(sys.argv) == 3:
    print "args: f=",sys.argv[1]," runs=",trials,"attack",sys.argv[2]
else:
    print "wrong number of args"


f = string.atoi(sys.argv[1])
f_str = sys.argv[1]
a = string.atoi(sys.argv[2])
a_str = sys.argv[2]
runNo = 0

config_hermes_src = "config/hermes.properties_"+f_str
config_hermes_dst = "config/hermes.properties"
config_bft_src = "config/system.config_"+f_str
config_bft_dst = "config/system.config"
print config_hermes_src,config_hermes_dst
#shutil.copyfile(src, dst)
shutil.copyfile(config_hermes_src,config_hermes_dst)
shutil.copyfile(config_bft_src,config_bft_dst)

ds = "./deploymentd_nohup.sh"
killall = "./killall.sh"
for i in range(16):
    print "\n\n\n RUN",i
    orch = "./server.sh "+f_str+" "+a_str+" "+str(i)
    print killall
    os.system(killall) 
    os.system(ds) 
    time.sleep(1.5)
    print orch
    os.system(orch) 

