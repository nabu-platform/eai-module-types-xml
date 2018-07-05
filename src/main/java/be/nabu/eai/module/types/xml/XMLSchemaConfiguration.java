package be.nabu.eai.module.types.xml;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import be.nabu.eai.repository.jaxb.ArtifactXMLAdapter;
import be.nabu.libs.types.api.DefinedTypeRegistry;

@XmlRootElement(name = "xmlSchema")
public class XMLSchemaConfiguration {
	
	private List<DefinedTypeRegistry> imports;
	
	@XmlJavaTypeAdapter(value = ArtifactXMLAdapter.class)	
	public List<DefinedTypeRegistry> getImports() {
		return imports;
	}
	public void setImports(List<DefinedTypeRegistry> imports) {
		this.imports = imports;
	}
	
	public static XMLSchemaConfiguration unmarshal(InputStream input) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(XMLSchemaConfiguration.class);
		return (XMLSchemaConfiguration) context.createUnmarshaller().unmarshal(input);
	}
	
	public void marshal(OutputStream output) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(XMLSchemaConfiguration.class);
		context.createMarshaller().marshal(this, output);
	}
}
