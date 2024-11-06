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
import java.io.OutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.xml.sax.SAXException;

import be.nabu.eai.repository.api.Repository;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.eai.repository.jaxb.ArtifactXMLAdapter;
import be.nabu.eai.repository.managers.base.TypeRegistryManager;
import be.nabu.libs.resources.ResourceReadableContainer;
import be.nabu.libs.resources.api.ManageableContainer;
import be.nabu.libs.resources.api.ReadableResource;
import be.nabu.libs.resources.api.Resource;
import be.nabu.libs.resources.api.WritableResource;
import be.nabu.libs.types.api.TypeRegistry;
import be.nabu.libs.validator.api.Validation;
import be.nabu.utils.io.IOUtils;
import be.nabu.utils.io.api.ByteBuffer;
import be.nabu.utils.io.api.ReadableContainer;
import be.nabu.utils.io.api.WritableContainer;

public class XMLSchemaManager extends TypeRegistryManager<XMLSchemaArtifact> {

	public XMLSchemaManager() {
		super(XMLSchemaArtifact.class);
	}

	@Override
	public List<Validation<?>> save(ResourceEntry entry, XMLSchemaArtifact artifact) throws IOException {
		Resource resource = entry.getContainer().getChild("xml-schema.xml");
		if (artifact.getConfiguration() != null) {
			if (resource == null) {
				resource = ((ManageableContainer<?>) entry.getContainer()).create("xml-schema.xml", "application/xml");
			}
			WritableContainer<ByteBuffer> writable = ((WritableResource) resource).getWritable();
			try {
				marshal(artifact.getRepository(), artifact.getConfiguration(), IOUtils.toOutputStream(writable));
			}
			finally {
				writable.close();
			}
		}
		else {
			((ManageableContainer<?>) entry.getContainer()).delete("uml-model.xml");
		}
		return new ArrayList<Validation<?>>();
	}
	
	@Override
	public XMLSchemaArtifact load(ResourceEntry entry, List<Validation<?>> messages) throws IOException, ParseException {
		Resource configResource = entry.getContainer().getChild("xml-schema.xml");
		XMLSchemaConfiguration configuration = null;
		if (configResource != null) {
			ReadableContainer<ByteBuffer> readable = ((ReadableResource) configResource).getReadable();
			try {
				configuration = XMLSchemaConfiguration.unmarshal(IOUtils.toInputStream(readable));
			}
			catch (JAXBException e) {
				throw new IOException(e);
			}
			finally {
				readable.close();
			}
		}
		
		Resource resource = entry.getContainer().getChild("schema.xsd");
		if (resource == null) {
			XMLSchemaArtifact xmlSchemaArtifact = new XMLSchemaArtifact(entry.getId(), entry.getContainer(), entry.getRepository());
			xmlSchemaArtifact.setConfiguration(configuration);
			return xmlSchemaArtifact;
		}
		ReadableContainer<ByteBuffer> readable = new ResourceReadableContainer((ReadableResource) resource);
		try {
			XMLSchemaArtifact schema = new XMLSchemaArtifact(IOUtils.toInputStream(readable), entry.getId(), entry.getContainer(), entry.getRepository());
			schema.setConfiguration(configuration);
			// we have our own import mechanism
			schema.setIgnoreInclusionFailure(true);
			// if we have included registries, load them
			if (configuration != null && configuration.getImports() != null && !configuration.getImports().isEmpty()) {
				schema.register(configuration.getImports().toArray(new TypeRegistry[0]));
				schema.setPrioritizeIncludes(true);
			}
			schema.setResolver(new EntryResourceResolver(entry));
			schema.parse();
			return schema;
		}
		catch (SAXException e) {
			throw new ParseException(e.getMessage(), 0);
		}
		finally {
			readable.close();
		}
	}

	public static void marshal(Repository repository, XMLSchemaConfiguration configuration, OutputStream output) {
		try {
			JAXBContext context = JAXBContext.newInstance(XMLSchemaConfiguration.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.setAdapter(new ArtifactXMLAdapter(repository));
			marshaller.marshal(configuration, output);
		}
		catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}
}
