package jplee.worldmanager.config;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

@SuppressWarnings("unused")
public abstract class Configurations {

	private DocumentBuilderFactory factory;
	private DocumentBuilder builder;

	private Document document;

	public Configurations() {
		try {
			factory = DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
		} catch(ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	public boolean parse(File file) {
		if(!file.exists()) {
			save(file);
		}
		try {
			document = builder.parse(file);
		} catch(SAXException | IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean validate(File file) {
		
		return false;
	}
	
	public boolean save(File file) {
		
		return true;
	}

	protected abstract void defaultConfig(StringBuilder xmlBuilder);

	protected abstract boolean isValidAtribute(String component, String...atributes);

	protected abstract boolean isValidElement(String element);

	protected abstract boolean isValidNode(String node);
}
