/**
 * License: src/main/resources/license/escidoc.license
 */
package de.mpg.imeji.presentation.edit.editors;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.mpg.imeji.exceptions.ImejiException;
import de.mpg.imeji.logic.item.ItemService;
import de.mpg.imeji.logic.vo.Item;
import de.mpg.imeji.logic.vo.License;
import de.mpg.imeji.logic.vo.Metadata;
import de.mpg.imeji.logic.vo.Properties.Status;
import de.mpg.imeji.logic.vo.Statement;
import de.mpg.imeji.logic.vo.User;
import de.mpg.imeji.logic.vo.factory.ImejiFactory;
import de.mpg.imeji.presentation.edit.ItemWrapper;
import de.mpg.imeji.presentation.license.LicenseEditor;

/**
 * Abstract call for the {@link Metadata} editors
 *
 * @author saquet (initial creation)
 * @author $Author$ (last modification)
 * @version $Revision$ $LastChangedDate$
 */
public abstract class AbstractMetadataEditor {
  protected List<ItemWrapper> items;
  protected Statement statement;
  protected User sessionUser;
  protected Locale locale;
  private LicenseEditor licenseEditor;

  /**
   * Editor: Edit a list of images for one statement.
   *
   * @param items
   * @param statement
   */
  public AbstractMetadataEditor(List<Item> itemList, Statement statement, User sessionUser,
      Locale locale) {
    this.statement = statement;
    this.locale = locale;
    this.sessionUser = sessionUser;
    items = new ArrayList<ItemWrapper>();
    for (final Item item : itemList) {
      items.add(new ItemWrapper(item, true));
    }
  }

  /**
   * Default editor
   */
  public AbstractMetadataEditor() {
    // cosntructor...
  }

  /**
   * Reset all value to empty state
   */
  public void reset() {
    items = new ArrayList<ItemWrapper>();
    statement = null;
  }

  /**
   * Clone as a {@link MultipleEditor}
   */
  @Override
  public AbstractMetadataEditor clone() {
    final AbstractMetadataEditor editor = new MultipleEditor();
    editor.setItems(items);
    editor.setStatement(statement);
    editor.setSessionUser(sessionUser);
    editor.setLocale(locale);
    return editor;
  }

  /**
   * Save the {@link Item} and {@link Metadata} defined in the editor
   *
   * @throws ImejiException
   */
  public void save() throws ImejiException {
    final ItemService ic = new ItemService();
    final List<Item> itemList = validateAndFormatItemsForSaving();
    ic.updateBatch(itemList, sessionUser);
  }

  /**
   * Validate and prepare the Items of the editor, so they can be saved
   *
   * @return
   */
  public List<Item> validateAndFormatItemsForSaving() {
    final List<Item> itemList = new ArrayList<Item>();
    for (final ItemWrapper eib : items) {
      final Item item = eib.asItem();
      addLicense(item);
      itemList.add(item);
    }
    return itemList;
  }

  /**
   * Add the selected license to the item if not null
   *
   * @param item
   */
  private void addLicense(Item item) {
    if (licenseEditor != null) {
      final License lic = licenseEditor.getLicense();
      if (lic != null) {
        if (!lic.isEmtpy()) {
          item.getLicenses().add(lic);
        } else if (lic.isEmtpy() && item.getStatus().equals(Status.PENDING)) {
          item.getLicenses().clear();
        }
      }
    }
  }

  /**
   * Create a new Metadata according to current Editor configuration.
   *
   * @return
   */
  protected Metadata newMetadata() {
    if (statement != null) {
      return ImejiFactory.newMetadata(statement).build();
    }
    return null;
  }

  public List<ItemWrapper> getItems() {
    return items;
  }

  public int getItemsSize() {
    return items.size();
  }

  public void setItems(List<ItemWrapper> items) {
    this.items = items;
  }

  public Statement getStatement() {
    return statement;
  }

  public void setStatement(Statement statement) {
    this.statement = statement;
  }


  /**
   * @return the sessionUser
   */
  public User getSessionUser() {
    return sessionUser;
  }

  /**
   * @param sessionUser the sessionUser to set
   */
  public void setSessionUser(User sessionUser) {
    this.sessionUser = sessionUser;
  }

  /**
   * @return the locale
   */
  public Locale getLocale() {
    return locale;
  }

  /**
   * @param locale the locale to set
   */
  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  /**
   * @return the licenseEditor
   */
  public LicenseEditor getLicenseEditor() {
    return licenseEditor;
  }

  /**
   * @param licenseEditor the licenseEditor to set
   */
  public void setLicenseEditor(LicenseEditor licenseEditor) {
    this.licenseEditor = licenseEditor;
  }
}