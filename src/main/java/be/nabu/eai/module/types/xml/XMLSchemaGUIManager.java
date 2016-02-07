package be.nabu.eai.module.types.xml;

import be.nabu.eai.developer.managers.TypeRegistryGUIManager;
import be.nabu.libs.types.xml.XMLSchema;

public class XMLSchemaGUIManager extends TypeRegistryGUIManager<XMLSchema> {

	public XMLSchemaGUIManager() {
		super(new XMLSchemaManager(), "XML Schema");
	}
	
	@Override
	public String getCategory() {
		return "Types";
	}
}
