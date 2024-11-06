/*
* Copyright (C) 2016 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

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
