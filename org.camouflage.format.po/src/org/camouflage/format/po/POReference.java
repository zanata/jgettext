package org.camouflage.format.po;

import java.util.ArrayList;
import java.util.List;

public class POReference {

	private String file;
	private List<String> locations = new ArrayList<String>();
	
	public POReference(String file, List<String> locations){
		this.file = file;
		this.locations.addAll(locations);
	}

	public POReference(String file){
		this.file = file;
	}
	
	/**
	 * @return the file
	 */
	public String getFile() {
		return file;
	}

	/**
	 * @param file the file to set
	 */
	public void setFile(String file) {
		this.file = file;
	}

	/**
	 * @return the locations
	 */
	public List<String> getLocations() {
		return locations;
	}

	/**
	 * @param locations the locations to set
	 */
	public void setLocations(List<String> locations) {
		this.locations = locations;
	}
	
	
}
