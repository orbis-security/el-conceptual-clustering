#! /bin/bash

export ORE_PATH=./ore2015_sample/pool_sample/

for ont in `cat $ORE_PATH/el/instantiation/fileorder.txt`
do

    echo $ont

    /usr/bin/time -f "TOTAL TIME: %e" timeout 600 ./computeHierarchy.sh $ORE_PATH/files/$ont 2 10 &> $ont.log
    mv output.owl $ont.simGraph.owl
    mv clustering-result.owl $ont.clustering.owl
done
