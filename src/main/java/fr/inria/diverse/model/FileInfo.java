package fr.inria.diverse.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

public class FileInfo {
    static Logger logger = LogManager.getLogger(FileInfo.class);

    private Long nodeId;
    private Set<String> paths;
    private long commitTimestamp;
    private Set<Long> originIds;

    private Set<String> branchs;


    private volatile boolean isInitializationDone;


    public FileInfo(long nodeId) {
        this.nodeId = nodeId;
        this.paths = new HashSet<>();
        this.branchs = new HashSet<>();
        this.originIds = new HashSet<>();

    }

    public void checkInitializationAndWait() {
        synchronized (this) {
            while (!isInitializationDone) {
                logger.debug("Waiting FileInfo init ...");

                try {
                    this.wait(2000);
                } catch (InterruptedException e) {
                    throw (new RuntimeException("Error while checking initialization", e));
                }
            }
        }
    }

    public void addOrigin(String path, long commitTimestamp, long originId, String branch) {
        synchronized (this) {
            this.paths.add(path);
            if (this.commitTimestamp < commitTimestamp)
                this.commitTimestamp = commitTimestamp;
            this.originIds.add(originId);
            this.branchs.add(branch);
        }
    }

    public void importFrom(FileInfo toImport) {
        //checkInitializationAndWait();
        synchronized (this) {
            this.paths.addAll(toImport.paths);
            this.originIds.addAll(toImport.originIds);
            //Todo
        }
    }

    public void initDone() {
        synchronized (this) {
            isInitializationDone = true;
            this.notifyAll();
        }
    }

    public long getNodeId() {
        //checkInitializationAndWait();
        return nodeId;
    }


    public Set<String> getPaths() {
        return paths;
    }


    public long getCommitTimestamp() {
        return commitTimestamp;
    }


    public Set getOriginIds() {
        return originIds;
    }


    public Set<String> getBranchs() {
        return branchs;
    }


}
