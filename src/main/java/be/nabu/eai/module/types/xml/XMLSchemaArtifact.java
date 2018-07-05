package be.nabu.eai.module.types.xml;

import java.io.IOException;
import java.io.InputStream;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import be.nabu.eai.repository.api.Repository;
import be.nabu.libs.resources.api.ResourceContainer;
import be.nabu.libs.types.xml.XMLSchema;

public class XMLSchemaArtifact extends XMLSchema {

	private ResourceContainer<?> container;
	private Repository repository;
	private XMLSchemaConfiguration configuration;

	public XMLSchemaArtifact(String id, ResourceContainer<?> container, Repository repository) {
		super((Document) null);
		this.container = container;
		this.repository = repository;
		setId(id);
	}
	
	public XMLSchemaArtifact(InputStream input, String id, ResourceContainer<?> container, Repository repository) throws SAXException, IOException {
		super(input);
		this.container = container;
		this.repository = repository;
		setId(id);
	}
	
	public ResourceContainer<?> getContainer() {
		return container;
	}

	public Repository getRepository() {
		return repository;
	}

	public XMLSchemaConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(XMLSchemaConfiguration configuration) {
		this.configuration = configuration;
	}
}
