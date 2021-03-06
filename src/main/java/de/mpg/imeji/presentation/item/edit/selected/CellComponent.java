package de.mpg.imeji.presentation.item.edit.selected;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.mpg.imeji.exceptions.UnprocessableError;
import de.mpg.imeji.logic.model.Metadata;
import de.mpg.imeji.logic.model.Statement;
import de.mpg.imeji.logic.model.factory.ImejiFactory;
import de.mpg.imeji.presentation.item.edit.MetadataInputComponent;

/**
 * A cell of the edit selected items page
 *
 * @author saquet
 *
 */
public class CellComponent implements Serializable {
  private static final long serialVersionUID = 4617072974872823679L;
  private List<MetadataInputComponent> inputs = new ArrayList<>();
  private final Statement statement;
  private boolean edit = false;

  public CellComponent(Statement statement, List<Metadata> metadata) {
    this.statement = statement.clone();
    // Init the input only for the metadata of this statement
    inputs = metadata.stream().filter(md -> md.getIndex().equals(this.statement.getIndex()))
        .map(md -> new MetadataInputComponent(md, this.statement)).collect(Collectors.toList());
  }

  public List<Metadata> toMetadataList() {
    final List<Metadata> l = new ArrayList<>();
    for (final MetadataInputComponent input : inputs) {
      l.add(input.getMetadata());
    }
    return l;
  }

  /**
   * Get the index for this cell
   *
   * @return
   */
  public String getIndex() {
    return statement.getIndex();
  }

  /**
   * Add an empty value
   */
  public void addValue() {
    inputs.add(new MetadataInputComponent(ImejiFactory.newMetadata(statement).build(), statement));
    edit = true;
  }

  /**
   * Add an empty value
   */
  public void removeValue(int position) {
    inputs.remove(position);
    if (inputs.isEmpty()) {
      edit = false;
    }
  }

  /**
   * Change the Statement of this input
   * 
   * @param s
   * @throws UnprocessableError
   */
  public void changeStatement(Statement s) {
    if (s.getType().equals(statement.getType())) {
      statement.setIndex(s.getIndex());;
      inputs.stream().peek(i -> i.getMetadata().setIndex(s.getIndex())).forEach(i -> i.setStatement(s));
    }
  }

  /**
   * Add a metadata
   *
   * @param metadata
   */
  public void addValue(Metadata metadata) {
    inputs.add(new MetadataInputComponent(metadata, statement));
  }

  /**
   * @return the inputs
   */
  public List<MetadataInputComponent> getInputs() {
    return inputs;
  }

  /**
   * @param inputs the inputs to set
   */
  public void setInputs(List<MetadataInputComponent> inputs) {
    this.inputs = inputs;
  }

  /**
   * @return the statement
   */
  public Statement getStatement() {
    return statement;
  }

  public boolean isEdit() {
    return edit;
  }

  public void setEdit(boolean edit) {
    this.edit = edit;
  }

  public void showEdit() {
    edit = true;
    if (inputs.isEmpty()) {
      addValue();
    }
  }

}
