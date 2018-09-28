package consensus;

public class Peer {

	public enum ConnectionType {
		WAN, LAN
	};

	private ConnectionType connectionType;
	private int nodeID;

	public Peer(ConnectionType connectionType, int nodeID) {
		super();
		this.connectionType = connectionType;
		this.nodeID = nodeID;
	}

	public ConnectionType getConnectionType() {
		return connectionType;
	}

	public void setConnectionType(ConnectionType connectionType) {
		this.connectionType = connectionType;
	}

	public int getNode() {
		return nodeID;
	}

	public void setNode(int nodeID) {
		this.nodeID = nodeID;
	}

}
