/**
 */
package fr.inria.diverse.model;


import java.io.Serializable;
import java.util.concurrent.atomic.LongAccumulator;

public class OriginVisit implements Serializable {
	public OriginVisit(Long timestamp, Snapshot snapshot) {
		this.timestamp = timestamp;
		this.snapshot = snapshot;
	}

	Long timestamp;
	Snapshot snapshot;

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public Snapshot getSnapshot() {
		return snapshot;
	}

	public void setSnapshot(Snapshot snapshot) {
		this.snapshot = snapshot;
	}
}
