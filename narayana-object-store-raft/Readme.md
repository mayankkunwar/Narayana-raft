# Narayana-raft



## Requirements

To add nodes to cluster edit [src/resources/narayana-raft.xml](https://github.com/mayankkunwar/narayana-raft/blob/master/narayana-object-store-raft/src/resources/narayana-raft.xml#L26) file

> <raft.RAFT members="node1,node2" raft_id="${raft_id:undefined}"/>

You can add as many nodes as you want before starting the raft.


## Building Naryana-raft

> mvn clean install

## Running Naryana-raft

Here node_name is the Node that you want to run. Note that, the *node_name* that you want to run should be present in [narayana-raft.xml](https://github.com/mayankkunwar/narayana-raft/blob/master/narayana-object-store-raft/src/resources/narayana-raft.xml)
> mvn exec:java -Dexec.args="node_name"
