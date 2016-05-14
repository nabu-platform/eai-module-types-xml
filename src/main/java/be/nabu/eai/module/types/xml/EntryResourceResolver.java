package be.nabu.eai.module.types.xml;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.libs.resources.ResourceReadableContainer;
import be.nabu.libs.resources.api.ReadableResource;
import be.nabu.libs.resources.api.Resource;
import be.nabu.libs.resources.api.ResourceContainer;
import be.nabu.libs.types.ParsedPath;
import be.nabu.libs.types.api.TypeRegistry;
import be.nabu.libs.types.xml.ResourceResolver;
import be.nabu.utils.io.IOUtils;

public class EntryResourceResolver implements ResourceResolver {

	private ResourceEntry root;

	public EntryResourceResolver(ResourceEntry root) {
		this.root = root;
	}
	
	@Override
	public InputStream resolve(URI uri) throws IOException {
		if (uri.getScheme() != null) {
			throw new IOException("The uri should be local: " + uri);
		}
		String path = uri.getPath();
		if (path.startsWith("/")) {
			throw new IOException("The uri should be relative: " + uri);
		}
		ParsedPath parsed = new ParsedPath(path);
		// always check the private/public folder first (if any)
		Resource privateFolder = root.getContainer().getChild(EAIResourceRepository.PRIVATE);
		Resource publicFolder = root.getContainer().getChild(EAIResourceRepository.PUBLIC);
		
		Resource resource = null;
		// first check private folder
		if (privateFolder != null) {
			resource = getResourceIn((ResourceContainer<?>) privateFolder, parsed);
		}
		// check public folder
		if (resource == null && publicFolder != null) {
			resource = getResourceIn((ResourceContainer<?>) publicFolder, parsed);
		}
		// otherwise, check locally
		if (resource == null) {
			ResourceEntry entry = root;
			while (parsed.getChildPath() != null) {
				entry = (ResourceEntry) entry.getChild(parsed.getName());
				if (entry == null) {
					throw new FileNotFoundException("Could not find the folder " + parsed.getName());
				}
				parsed = parsed.getChildPath();
			}
			resource = entry.getContainer().getChild(parsed.getName());
		}
		if (resource == null) {
			throw new FileNotFoundException("Could not find the file " + parsed.getName());
		}
		return IOUtils.toInputStream(new ResourceReadableContainer((ReadableResource) resource));
	}
	
	private Resource getResourceIn(ResourceContainer<?> container, ParsedPath path) {
		Resource child = container.getChild(path.getName());
		if (child == null || path.getChildPath() == null) {
			return child;
		}
		else {
			return getResourceIn((ResourceContainer<?>) child, path.getChildPath());
		}
	}

	@Override
	public TypeRegistry resolve(String namespace) throws IOException {
		throw new IllegalStateException("Can not resolve namespaces in an entry resolver");
	}
	
}