package de.mpg.imeji.presentation.collection.subcollection;

import java.io.IOException;
import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import de.mpg.imeji.exceptions.ImejiException;
import de.mpg.imeji.exceptions.ImejiExceptionWithUserMessage;
import de.mpg.imeji.logic.core.collection.CollectionService;
import de.mpg.imeji.logic.model.CollectionImeji;
import de.mpg.imeji.logic.model.factory.ImejiFactory;
import de.mpg.imeji.presentation.beans.SuperBean;
import de.mpg.imeji.presentation.session.BeanHelper;

/**
 * JSF Bean for the subcollection form
 * 
 * @author saquet
 *
 */
@ManagedBean(name = "SubCollectionFormBean")
@ViewScoped
public class SubCollectionFormBean extends SuperBean implements Serializable {
  private static final long serialVersionUID = 3542202393184509349L;
  private static final Logger LOGGER = LogManager.getLogger(SubCollectionFormBean.class);
  private final CollectionService collectionService = new CollectionService();
  private String name;
  private CollectionImeji parent;

  public void init(CollectionImeji collection) {
    this.parent = collection;
    this.name = collection.getName();
  }

  /**
   * Create a subcollection with the name set in the bean into the parent collection
   * 
   * @param parent
   * @throws IOException
   */
  public void create(CollectionImeji parent) throws IOException {
    try {
      CollectionImeji subcollection = collectionService.create(ImejiFactory.newCollection().setTitle(name)
          .setPerson(getSessionUser().getPerson()).setCollection(parent.getId().toString()).build(), getSessionUser());
      BeanHelper.info("Subcollection created");
      redirect(getNavigation().getCollectionUrl() + subcollection.getIdString());
    } catch (final ImejiExceptionWithUserMessage exceptionWithMessage) {
      String userMessage = "Error creating Subcollection: " + exceptionWithMessage.getUserMessage(getLocale());
      BeanHelper.error(userMessage);
      if (exceptionWithMessage.getMessage() != null) {
        LOGGER.error("Error creating Subcollection: " + exceptionWithMessage.getMessage(), exceptionWithMessage);
      } else {
        LOGGER.error(userMessage, exceptionWithMessage);
      }
      redirect(getNavigation().getCollectionUrl());
    } catch (ImejiException e) {
      BeanHelper.error("Error creating Subcollection: " + e.getMessage());
      LOGGER.error("Error creating Subcollection", e);
      redirect(getNavigation().getCollectionUrl());
    }
  }

  /**
   * Create a Subcollection and then redirect to the upload link
   * 
   * @param parent
   * @throws IOException
   */
  public void createAndUpload() throws IOException {
    try {
      CollectionImeji subcollection = collectionService.create(ImejiFactory.newCollection().setTitle(name)
          .setPerson(getSessionUser().getPerson()).setCollection(parent.getId().toString()).build(), getSessionUser());
      BeanHelper.info("Subcollection created");
      redirect(getNavigation().getCollectionUrl() + subcollection.getIdString() + "?showUpload=1");
    } catch (final ImejiExceptionWithUserMessage exceptionWithMessage) {
      String userMessage = "Error creating Subcollection: " + exceptionWithMessage.getUserMessage(getLocale());
      BeanHelper.error(userMessage);
      if (exceptionWithMessage.getMessage() != null) {
        LOGGER.error("Error creating Subcollection: " + exceptionWithMessage.getMessage(), exceptionWithMessage);
      } else {
        LOGGER.error(userMessage, exceptionWithMessage);
      }
      redirect(getNavigation().getCollectionUrl());
    } catch (ImejiException e) {
      BeanHelper.error("Error creating Subcollection: " + e.getMessage());
      LOGGER.error("Error creating Subcollection", e);
      redirect(getNavigation().getCollectionUrl());
    }
  }

  /**
   * Change the name of the subcollection
   * 
   * @param subCollection
   */
  public void edit(CollectionImeji subCollection) throws IOException {
    try {
      subCollection.setTitle(name);
      collectionService.update(subCollection, getSessionUser());
      BeanHelper.info("Subcollection name changed");
      redirect(getNavigation().getCollectionUrl() + subCollection.getIdString());
    } catch (final ImejiExceptionWithUserMessage exceptionWithMessage) {
      String userMessage = "Error editing subcollection: " + exceptionWithMessage.getUserMessage(getLocale());
      BeanHelper.error(userMessage);
      if (exceptionWithMessage.getMessage() != null) {
        LOGGER.error("Error editing subcollection: " + exceptionWithMessage.getMessage(), exceptionWithMessage);
      } else {
        LOGGER.error(userMessage, exceptionWithMessage);
      }
      redirect(getNavigation().getCollectionUrl() + subCollection.getIdString());
    } catch (ImejiException e) {
      BeanHelper.error("Error editing subcollection: " + e.getMessage());
      LOGGER.error("Error editing subcollection", e);
      redirect(getNavigation().getCollectionUrl() + subCollection.getIdString());
    }
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }
}
