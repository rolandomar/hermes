rm config/currentView
cp ..//BFT-SMaRt.jar dist//BFT-SMaRt.jar
java -cp ../Hermes/dist/Hermes.jar:dist/BFT-SMaRt.jar:lib/slf4j-api-1.5.8.jar:lib/slf4j-jdk14-1.5.8.jar:lib/netty-3.1.1.GA.jar:lib/commons-codec-1.5.jar:/home/rmartins/aspectj1.7/lib/aspectjrt.jar:/home/rmartins/aspectj1.7/lib/org.aspectj.matcher.jar $1 $2 $3 $4 $5 $6 $7 $8 $9
