package org.amdocs.tsuzammen.plugin.collaborationstore.git.utils;


import org.amdocs.tsuzammen.commons.datatypes.SessionContext;
import org.amdocs.tsuzammen.commons.datatypes.item.Content;
import org.amdocs.tsuzammen.commons.datatypes.item.Entity;
import org.amdocs.tsuzammen.commons.datatypes.item.Format;
import org.amdocs.tsuzammen.commons.datatypes.item.ItemVersion;
import org.amdocs.tsuzammen.utils.common.CommonMethods;
import org.amdocs.tsuzammen.utils.fileutils.FileUtils;

import java.io.File;
import java.util.List;
import java.util.Map;

public class SourceControlFileStore {

  private SessionContext context;
  private String path;
  private List<File> filesToRemove;
  private List<File> filesToAdd;


  public SourceControlFileStore(SessionContext context, String path) {
    this.path = path;
    this.context = context;
  }


  public File[] getFilesToRemove() {
    return CommonMethods.toArray(this.filesToRemove, File.class);
  }

  public File[] getFilesToAdd() {
    return CommonMethods.toArray(this.filesToAdd, File.class);
  }

  public void store(ItemVersion itemVersion) {
    if (itemVersion.getInfo() != null) {

      filesToAdd
          .add(FileUtils.writeFile(path, PluginConstants.INFO_FILE_NAME, itemVersion.getInfo()));
    }

    if (itemVersion.getRelations() != null) {
      filesToAdd.add(
          FileUtils
              .writeFile(path, PluginConstants.RELATIONS_FILE_NAME, itemVersion.getRelations()));
      storeContentListToDisc(context, path, itemVersion.getContents());
    }
  }

  private void storeContentListToDisc(SessionContext context, String path, Map<String, Content>
      contents) {
    for (Map.Entry<String, Content> content : contents.entrySet()) {
      path += path + File.separator + content.getKey();
      storeContentToDisc(context, path, content.getValue());
    }
  }

  private void storeContentToDisc(SessionContext context, String path, Content content) {


    for (Entity entity : content.getEntities()) {
      path += path + File.separator + entity.getOid();
      if (!SourceControlUtil.isEmpty(entity)) {
        storeEntityToDisc(context, path, entity.getOid(), entity, content.getDataFormat()
        );
      } else {
        deleteEntityFromDisc(context, path);
      }
    }

  }

  private void deleteEntityFromDisc(SessionContext context, String path) {
    File file = new File(path);
    List<File> fileToDelete = FileUtils.getFiles(path);
    if (!FileUtils.delete(file)) {
      throw new RuntimeException("path [" + path + "] does not exist");
    }
    this.filesToRemove.addAll(fileToDelete);


  }

  private void storeEntityToDisc(SessionContext context, String path, String entityId, Entity
      entity, Format dataFormat) {

    filesToAdd.add(FileUtils.writeFile(path, entityId, entity.getData()));
    if (entity.getInfo() != null) {
      filesToAdd.add(FileUtils.writeFile(path, PluginConstants.INFO_FILE_NAME, entity.getInfo()));
    }
    if (entity.getVisualization() != null) {
      filesToAdd.add(FileUtils
          .writeFile(path, PluginConstants.VISUALIZATION_FILE_NAME, entity.getVisualization()));
    }

    if (entity.getRelations() != null) {
      filesToAdd.add(
          FileUtils.writeFile(path, PluginConstants.RELATIONS_FILE_NAME, entity.getRelations()));
    }
    storeContentListToDisc(context, path, entity.getContents());

  }


}
