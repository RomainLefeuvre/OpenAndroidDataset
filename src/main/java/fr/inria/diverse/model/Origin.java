package fr.inria.diverse.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;

public class Origin extends Node implements Serializable {
    static Logger logger = LogManager.getLogger(Snapshot.class);

    private Snapshot snapshot;
    private String originUrl;

    public Origin(String originUrl, long nodeId) {
        super(nodeId);
        this.originUrl = originUrl;
        this.nodeId = nodeId;
    }

    public Origin() {
        super();

    }

    /**
     * Compare current snapshot with snap and update snap value if needed
     *
     * @param snap the new snap candidate
     */
    public void checkSnapshotAndUpdate(Snapshot snap) {
        if (this.snapshot == null || (this.snapshot.compareTo(snap) < 0)) {
            logger.debug("Updating snapshot");
            this.snapshot = snap;
        }
    }

    public Snapshot getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(Snapshot snapshot) {
        this.snapshot = snapshot;
    }

    public String getOriginUrl() {
        return originUrl;
    }

    public void setOriginUrl(String originUrl) {
        this.originUrl = originUrl;
    }

}
