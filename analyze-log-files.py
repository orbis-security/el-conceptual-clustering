#! /usr/bin/python

from pathlib import Path

path = Path(".")

print("ontology abox-size individuals nodes products iterations complete runtime")

for logfile in path.glob("ore_ont*.log"):
    ontology = logfile.name[:-4]

    aboxSize = -1
    individuals = -1
    nodes = -1
    products = -1
    iterations = -1
    complete = False
    time = -1

    aboxKey = "ABOX SIZE:"
    individualsKey = "INDIVIDUALS:"
    nodesKey = "BOUNDED DEPTH NODES:"
    productsKey = "PRODUCTS:"
    iterationsKey = "ITERATIONS:"
    completeKey = "ALL PRODUCTS FOUND!"
    timeKey = "TOTAL TIME:"
    
    with open(logfile, 'r') as file:
        for line in file:
            if(line.startswith(aboxKey)):
                aboxSize = line[len(aboxKey)+1:-1]
            if(line.startswith(individualsKey)):
                individuals = line[len(individualsKey)+1:-1]
            if(line.startswith(nodesKey)):
                nodes = line[len(nodesKey)+1:-1]
            if(line.startswith(productsKey)):
                products = line[len(productsKey)+1:-1]
            if(line.startswith(iterationsKey)):
                iterations = line[len(iterationsKey)+1:-1]
            if(line.startswith(completeKey)):
                complete = True
            if(line.startswith(timeKey)):
                time = line[len(timeKey)+1:-1]

    print(ontology, aboxSize, individuals, nodes, products, iterations, complete, time)
