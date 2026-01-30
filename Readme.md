

# Requirements

- GCC 11.4
- Java 17
- Maven
- Python 3.12 (for analyzing the experiment data)


# Compiling

### 1. Compile the main code in Java

  - Make sure to have Maven installed
  - Go into the bisimulation folder
  - Execute 
```bash
	mvn package
```
  - Move the file `bisimulation-graphs-1.0-SNAPSHOT-jar-with-dependencies.jar` from the `target` subdirectory to the root folder

### 2. Compile the summarization component
  - Create a folder `lod_summarization/external`
  - Go into `lod_summarization/setup` an execute 
```bash
	./setup_experiments.sh -y
```
  - Note down the ID in the created folder as in `lod_summarization/d9a8fda5352cb6f8499e02d615b03d5a2831d51d`

### 3. Adaptations
  - Replace the ID in line 7 in the `computeHierarchy.sh` script
  - In `lod_summarization/ID/scripts/preprocessor.config`, ensure that "`types_to_predicates=true`"

# Computing Concept Hierarchies

To compute a conceptual hierarchy from an ABox stored inside an OWL file, use the following command

```bash
    ./computeHierarchy OWL_FILE MAX_DEPTH ITERATIONS
```

Here, `OWL_FILE` is the path to the ontology file, `MAX_DEPTH` the maximal role depth considered in the concepts, and `ITERATIONS` the number of iterations of computing products for generating the concepts, as described in the paper.

In the generated owl file, the new concepts use the following naming scheme:

```
    C[number]_[iteration] - [count] - [utility]
```

where `[number]` is the ID of the cluster, and indicates when it was created, `[iteration]` indicates in which iteration the cluster was created (through how many products), `[count]` gives the number of individuals that satisfy this concept, and `utility` the utility value of the cluster.

# Reproducing the Experiments

1. Download the ORE 2015 repository from Zenodo (https://zenodo.org/records/18578) and place it in the source code folder
2. Run the script "`./run_all_ore.sh`" to compute concept hierarchies for each ABox. This will create the ontologies together with a bunch of log files.
3. To generate a CSV file with experimental results as was used for the paper, execute the script "`./analyze-log-files.py`".

# Experimenting with EMBER2018-based PE Malware Datasets

1. Get the datasets (almost 7.9 GiB):
    ```bash
    ./get-pe-malware-onto.sh
    ```
2. Run the clustering on all datasets of a given `SAMPLE_SIZE` (`1000`, `10000`, `100000`, `800000`) or, optionally, just selected `DATASET`s (`1` to `10`)*:

    ```bash
    ./run-pe-malware.sh MAX_DEPTH MAX_ITERATIONS TIMEOUT_MINUTES SAMPLE_SIZE [DATASET [DATASET …]]
    ```

    * <small>`[]` indicates optional arguments</small>

