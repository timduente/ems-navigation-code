package de.duente.navigation;

public class Step {
	private GeoPoint start;
	/**
	 * @return the start
	 */
	public GeoPoint getStart() {
		return start;
	}

	/**
	 * @return the end
	 */
	public GeoPoint getEnd() {
		return end;
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	private GeoPoint end;
	private String text;
	private String htmlInstruction;
	
	/**
	 * @return the htmlInstruction
	 */
	public String getHtmlInstruction() {
		return htmlInstruction;
	}

	/**
	 * @param htmlInstruction the htmlInstruction to set
	 */
	public void setHtmlInstruction(String htmlInstruction) {
		this.htmlInstruction = htmlInstruction;
	}

	public Step(GeoPoint start, GeoPoint end, String text){
		this.start = start;
		this.end = end;
		this.text = text;
	}
	
	@Override
	public String toString(){
		return text + " @Start: " + start.toString() + " End: " + end.toString();
	}
}
