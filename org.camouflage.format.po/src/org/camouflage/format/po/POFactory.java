package org.camouflage.format.po;

import java.io.FileNotFoundException;

public class POFactory {

	public static POMarshaller createMarshaller(){
		return new POMarshallerImpl();
	}
	
	public static POUnmarshaller createUnmarshaller(){
		return new POUnmarshallerImpl();
	}
	
	public static void main(String[] args) throws FileNotFoundException, ParseException {
		POUnmarshaller unmarshaller = POFactory.createUnmarshaller();
		POFile file = unmarshaller.unmarshall("/home/asgeirf/Workspaces/flies/transifex-devel.diego/po/el/LC_MESSAGES/el.po");
		System.out.println(file.toString());
	}
}
