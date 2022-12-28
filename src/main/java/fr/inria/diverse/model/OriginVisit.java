/**
 */
package fr.inria.diverse.model;


import java.io.Serializable;

public class OriginVisit implements Serializable {
	private static final long serialVersionUID = -1707044607192784010L;

	public OriginVisit() {
	}

	public OriginVisit(Long timestamp, Snapshot snapshot) {
		this.timestamp = timestamp;
		this.snapshot = snapshot;
	}

	public OriginVisit(Snapshot snapshot) {
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
