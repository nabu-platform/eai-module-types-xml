package be.nabu.eai.module.types.xml;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.managers.TypeRegistryGUIManager;
import be.nabu.eai.developer.managers.util.SimpleProperty;
import be.nabu.eai.developer.managers.util.SimplePropertyUpdater;
import be.nabu.eai.developer.util.EAIDeveloperUtils;
import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;
import be.nabu.libs.resources.ResourceUtils;
import be.nabu.libs.resources.api.ManageableContainer;
import be.nabu.libs.resources.api.Resource;
import be.nabu.libs.resources.api.ResourceContainer;
import be.nabu.libs.resources.api.ResourceFilter;
import be.nabu.libs.resources.api.WritableResource;
import be.nabu.libs.resources.file.FileItem;
import be.nabu.utils.io.IOUtils;
import be.nabu.utils.io.api.ByteBuffer;
import be.nabu.utils.io.api.Container;
import be.nabu.utils.io.api.WritableContainer;

public class XMLSchemaGUIManager extends TypeRegistryGUIManager<XMLSchemaArtifact> {

	public XMLSchemaGUIManager() {
		super(new XMLSchemaManager(), "XML Schema");
	}
	
	@Override
	public String getCategory() {
		return "Types";
	}

	@Override
	protected XMLSchemaArtifact newInstance(MainController controller, RepositoryEntry entry, Value<?>...values) throws IOException {
		return new XMLSchemaArtifact(entry.getId(), entry.getContainer(), entry.getRepository());
	}

	@Override
	public void display(MainController controller, AnchorPane pane, XMLSchemaArtifact artifact) throws IOException, ParseException {
		final ResourceContainer<?> container = artifact.getContainer();
		
		final ListView<String> files = new ListView<String>();
		ResourceContainer<?> privateDirectory = (ResourceContainer<?>) artifact.getContainer().getChild(EAIResourceRepository.PRIVATE);
		if (privateDirectory != null) {
			listFiles(files, privateDirectory);
		}

		Button setFiles = new Button("Set XSD");
		setFiles.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public void handle(ActionEvent arg0) {
				SimpleProperty<File> mainProperty = new SimpleProperty<File>("Main XSD file", File.class, true);
				SimpleProperty<File> zipProperty = new SimpleProperty<File>("XSD support files zip", File.class, false);
				mainProperty.setInput(true);
				zipProperty.setInput(true);
				Set properties = new LinkedHashSet(Arrays.asList(new Property [] {
					mainProperty,
					zipProperty
				}));
				final SimplePropertyUpdater updater = new SimplePropertyUpdater(true, properties);
				EAIDeveloperUtils.buildPopup(MainController.getInstance(), updater, "Set XML Schema's", new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						File xsdFile = updater.getValue("Main XSD file");
						File zipFile = updater.getValue("XSD support files zip");
						if (xsdFile != null) {
							try {
								if (xsdFile.isFile() && xsdFile.getName().endsWith(".xsd")) {
									Container<ByteBuffer> wrap = IOUtils.wrap(xsdFile);
									try {
										Resource resource = container.getChild("schema.xsd");
										if (resource == null) {
											resource = ((ManageableContainer<?>) container).create("schema.xsd", "application/xml");
										}
										WritableContainer<ByteBuffer> writable = ((WritableResource) resource).getWritable();
										try {
											IOUtils.copyBytes(wrap, writable);
										}
										finally {
											writable.close();
										}
									}
									finally {
										wrap.close();
									}
									ResourceContainer<?> privateDirectory = (ResourceContainer<?>) artifact.getContainer().getChild(EAIResourceRepository.PRIVATE);
									// always delete the private directory if we upload a new main scheme
									if (privateDirectory != null) {
										((ManageableContainer<?>) artifact.getContainer()).delete(EAIResourceRepository.PRIVATE);
									}
									files.getItems().clear();
									if (zipFile != null && zipFile.isFile() && zipFile.getName().endsWith(".zip")) {
										privateDirectory = (ResourceContainer<?>) ((ManageableContainer<?>) artifact.getContainer()).create(EAIResourceRepository.PRIVATE, Resource.CONTENT_TYPE_DIRECTORY);
										ResourceUtils.unzip(new FileItem(null, zipFile, false), privateDirectory);
										listFiles(files, privateDirectory);
									}
									reload(artifact);
								}
							}
							catch (IOException e) {
								throw new RuntimeException(e);
							}
						}
					}
				}, true);
			}
		});
		
		HBox box = new HBox();
		box.getStyleClass().add("buttons");
		box.getChildren().addAll(setFiles);
		
		VBox vbox = new VBox();
		vbox.getChildren().addAll(box, files);
		pane.getChildren().add(vbox);
		
		AnchorPane.setLeftAnchor(vbox, 0d);
		AnchorPane.setRightAnchor(vbox, 0d);
		AnchorPane.setTopAnchor(vbox, 0d);
		AnchorPane.setBottomAnchor(vbox, 0d);
	}

	private void listFiles(final ListView<String> files, ResourceContainer<?> privateDirectory) {
		List<Resource> supporting = ResourceUtils.find(privateDirectory, new ResourceFilter() {
			@Override
			public boolean accept(Resource resource) {
				return resource.getName().endsWith(".xsd");
			}
		}, true);
		String privatePath = ResourceUtils.getPath(privateDirectory);
		for (Resource support : supporting) {
			files.getItems().add(ResourceUtils.getPath(support).substring(privatePath.length() + 1));
		}
	}
	
	public static void reload(Artifact artifact) {
		try {
			// reload artifact in repo
			MainController.getInstance().getRepository().reload(artifact.getId());
			// reload remote
			MainController.getInstance().getServer().getRemote().reload(artifact.getId());
			// trigger refresh in tree
			TreeItem<Entry> resolve = MainController.getInstance().getTree().resolve(artifact.getId().replace('.', '/'));
			if (resolve != null) {
				resolve.getParent().refresh();
				resolve.refresh();
				MainController.getInstance().getRepository().reload(artifact.getId());
				MainController.getInstance().getTree().getTreeCell(resolve.getParent()).refresh();
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
