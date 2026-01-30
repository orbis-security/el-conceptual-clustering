#! /bin/bash

export ONTOLOGY=$1
export MAX_DEPTH=$2
export MAX_ITERATIONS=$3

export LOD_ID=d9a8fda5352cb6f8499e02d615b03d5a2831d51d

HEAPSIZE=32g

echo Using ontology $ONTOLOGY

# Step 1: extract N-triples from OWL-file

echo "[$(date --rfc-3339=seconds)] Step 1: extract N-triples from OWL-file"
java -Xmx$HEAPSIZE -cp bisimulation-graphs-1.0-SNAPSHOT-jar-with-dependencies.jar nl.vu.kai.bisimulations.cmd.ABox2NTriples "$ONTOLOGY"

export ONT_NAME=`basename "$ONTOLOGY"`

# Step 2: run summarization

echo "[$(date --rfc-3339=seconds)] Step 2: run summarization"
cd lod_summarization/$LOD_ID/scripts

./run_all.sh "../../../$ONT_NAME.nt" -y

cd ../../..

# Step 3: run conceptual clustering

echo "[$(date --rfc-3339=seconds)] Step 3: run conceptual clustering"
java -Xmx$HEAPSIZE -cp bisimulation-graphs-1.0-SNAPSHOT-jar-with-dependencies.jar nl.vu.kai.bisimulations.cmd.ExtractHierarchy "lod_summarization/$LOD_ID/$ONT_NAME/" "$ONTOLOGY" $MAX_DEPTH $MAX_ITERATIONS
