package net.thecodemaster.esvd.marker.resolution;

public class ResolutionMessage {

	private int			type;
	private String	label;
	private String	description;

	public ResolutionMessage(int type, String label, String description) {
		this.type = type;
		this.label = label;
		this.description = description;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
