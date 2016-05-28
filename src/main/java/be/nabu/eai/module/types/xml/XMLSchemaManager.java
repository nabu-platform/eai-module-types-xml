package be.nabu.eai.module.types.xml;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import org.xml.sax.SAXException;

import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.eai.repository.managers.base.TypeRegistryManager;
import be.nabu.libs.resources.ResourceReadableContainer;
import be.nabu.libs.resources.api.ReadableResource;
import be.nabu.libs.resources.api.Resource;
import be.nabu.libs.validator.api.Validation;
import be.nabu.utils.io.IOUtils;
import be.nabu.utils.io.api.ByteBuffer;
import be.nabu.utils.io.api.ReadableContainer;

public class XMLSchemaManager extends TypeRegistryManager<XMLSchemaArtifact> {

	public XMLSchemaManager() {
		super(XMLSchemaArtifact.class);
	}

	@Override
	public XMLSchemaArtifact load(ResourceEntry entry, List<Validation<?>> messages) throws IOException, ParseException {
		Resource resource = entry.getContainer().getChild("schema.xsd");
		if (resource == null) {
			return new XMLSchemaArtifact(entry.getId(), entry.getContainer(), entry.getRepository());
		}
		ReadableContainer<ByteBuffer> readable = new ResourceReadableContainer((ReadableResource) resource);
		try {
			XMLSchemaArtifact schema = new XMLSchemaArtifact(IOUtils.toInputStream(readable), entry.getId(), entry.getContainer(), entry.getRepository());
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

}
