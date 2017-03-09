package de.mpg.imeji.presentation.edit.single;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import de.mpg.imeji.exceptions.ImejiException;
import de.mpg.imeji.logic.vo.Item;
import de.mpg.imeji.logic.vo.Metadata;
import de.mpg.imeji.logic.vo.Statement;
import de.mpg.imeji.logic.vo.User;
import de.mpg.imeji.logic.vo.factory.MetadataFactory;
import de.mpg.imeji.presentation.edit.EditMetadataAbstract;
import de.mpg.imeji.presentation.edit.MetadataInputComponent;
import de.mpg.imeji.presentation.license.LicenseEditor;

/**
 * Edit the {@link Metadata} of a single {@link Item}
 *
 * @author saquet
 *
 */
public class EditItemComponent extends EditMetadataAbstract {
  private static final long serialVersionUID = 4116466458089234630L;
  private static Logger LOGGER = Logger.getLogger(EditItemComponent.class);
  private List<EditItemEntry> entries = new ArrayList<>();
  private Item item;
  private String filename;
  private LicenseEditor licenseEditor;
  private final User user;
  private final Locale locale;

  public EditItemComponent(Item item, User user, Locale locale) throws ImejiException {
    super();
    this.item = item;
    this.user = user;
    this.locale = locale;
    this.licenseEditor = new LicenseEditor(getLocale(), item);
    this.filename = item.getFilename();
    entries = item.getMetadata().stream().map(md -> new EditItemEntry(md, statementMap))
        .collect(Collectors.toList());
    entries
        .addAll(
            getDefaultStatements().values().stream()
                .map(s -> new EditItemEntry(
                    new MetadataFactory().setStatementId(s.getIndex()).build(), statementMap))
                .collect(Collectors.toList()));
  }

  @Override
  public List<Item> toItemList() {
    item.setFilename(filename);
    item.setMetadata(entries.stream().map(EditItemEntry::getInput)
        .map(MetadataInputComponent::getMetadata).collect(Collectors.toList()));
    item.getLicenses().add(licenseEditor.getLicense());
    return Arrays.asList(item);
  }

  @Override
  public List<Statement> getAllStatements() {
    return entries.stream().filter(row -> row.getInput() != null && !row.getInput().isEmpty())
        .map(EditItemEntry::getInput).map(MetadataInputComponent::getStatement)
        .collect(Collectors.toMap(Statement::getIndex, Function.identity(), (a, b) -> a)).values()
        .stream().collect(Collectors.toList());
  }

  /**
   * Add a new empty row
   */
  public void addMetadata() {
    entries.add(new EditItemEntry(statementMap));
  }

  /**
   * Remove a metadata
   *
   * @param index
   */
  public void removeMetadata(int index) {
    entries.remove(index);
  }

  /**
   * @return the rows
   */
  public List<EditItemEntry> getEntries() {
    return entries;
  }

  /**
   * @param rows the rows to set
   */
  public void setEntries(List<EditItemEntry> entries) {
    this.entries = entries;
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

  /**
   * @return the filename
   */
  public String getFilename() {
    return filename;
  }

  /**
   * @param filename the filename to set
   */
  public void setFilename(String filename) {
    this.filename = filename;
  }

  @Override
  public User getSessionUser() {
    return this.user;
  }

  @Override
  public Locale getLocale() {
    return this.locale;
  }
}
