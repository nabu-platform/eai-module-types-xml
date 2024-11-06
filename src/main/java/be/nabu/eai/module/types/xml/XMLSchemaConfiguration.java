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
