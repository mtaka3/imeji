package de.mpg.imeji.presentation.user;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jose4j.lang.JoseException;

import de.mpg.imeji.exceptions.ImejiException;
import de.mpg.imeji.exceptions.ImejiExceptionWithUserMessage;
import de.mpg.imeji.exceptions.ReloadBeforeSaveException;
import de.mpg.imeji.exceptions.UnprocessableError;
import de.mpg.imeji.logic.config.Imeji;
import de.mpg.imeji.logic.model.Organization;
import de.mpg.imeji.logic.model.User;
import de.mpg.imeji.logic.model.UserGroup;
import de.mpg.imeji.logic.model.factory.ImejiFactory;
import de.mpg.imeji.logic.search.jenasearch.ImejiSPARQL;
import de.mpg.imeji.logic.search.jenasearch.JenaCustomQueries;
import de.mpg.imeji.logic.security.authentication.impl.APIKeyAuthentication;
import de.mpg.imeji.logic.security.authorization.util.SecurityUtil;
import de.mpg.imeji.logic.security.sharing.ShareService;
import de.mpg.imeji.logic.security.user.UserService;
import de.mpg.imeji.logic.security.user.util.QuotaUtil;
import de.mpg.imeji.logic.util.UrlHelper;
import de.mpg.imeji.presentation.beans.SuperBean;
import de.mpg.imeji.presentation.collection.share.ShareListItem;
import de.mpg.imeji.presentation.collection.share.ShareUtil;
import de.mpg.imeji.presentation.session.BeanHelper;

@ManagedBean(name = "UserBean")
@ViewScoped
public class UserBean extends SuperBean {
  private static final long serialVersionUID = 8339673964329354673L;
  private static final Logger LOGGER = LogManager.getLogger(UserBean.class);
  private User user;
  private String id;
  private List<ShareListItem> roles = new ArrayList<ShareListItem>();
  private boolean edit = false;
  private QuotaUICompoment quota;

  @PostConstruct
  public void init() {
    init(UrlHelper.getParameterValue("email"));
  }

  /**
   * Initialize the bean
   *
   * @param id
   */
  private void init(String id) {
    try {
      this.id = id;
      retrieveUser();
      if (user != null) {
        this.roles = ShareUtil.getAllRoles(user, getSessionUser(), getLocale());
        this.setEdit(false);
        this.setQuota(new QuotaUICompoment(user, getLocale()));
      }
    } catch (final ImejiExceptionWithUserMessage exceptionWithMessage) {
      String userMessage = "Error initializing page. " + exceptionWithMessage.getUserMessage(getLocale());
      BeanHelper.error(userMessage);
      if (exceptionWithMessage.getMessage() != null) {
        LOGGER.error(exceptionWithMessage.getMessage(), exceptionWithMessage);
      } else {
        LOGGER.error(userMessage, exceptionWithMessage);
      }
    } catch (final Exception e) {
      LOGGER.error("Error initializing page", e);
      BeanHelper.error("Error initializing page");
    }
  }

  /**
   * Retrieve the current user
   *
   * @throws ImejiException
   *
   * @throws Exception
   */
  public void retrieveUser() throws ImejiException {
    if (id != null && getSessionUser() != null) {
      user = new UserService().retrieve(id, getSessionUser());
      if (user.getPerson().getOrganizations() == null || user.getPerson().getOrganizations().isEmpty()) {
        user.getPerson().getOrganizations().add(new Organization());
      }
    }
  }

  public void toggleEdit() {
    this.edit = edit ? false : true;
  }

  /**
   * Generate a new API Key, and update the user. Used in User.xhtml
   *
   * @throws ImejiException
   * @throws NoSuchAlgorithmException
   * @throws UnsupportedEncodingException
   * @throws JoseException
   */
  public void generateNewApiKey() {
    if (user != null) {
      try {
        user.setApiKey(APIKeyAuthentication.generateKey(user.getId(), Integer.MAX_VALUE));
        user = new UserService().update(user, getSessionUser());
      } catch (final JoseException e) {
        LOGGER.error("Error generating API Key ", e);
        BeanHelper.error("Error generating API Key");
      } catch (final ImejiExceptionWithUserMessage exceptionWithMessage) {
        String userMessage = "Error saving API key. " + exceptionWithMessage.getUserMessage(getLocale());
        BeanHelper.error(userMessage);
        if (exceptionWithMessage.getMessage() != null) {
          LOGGER.error(exceptionWithMessage.getMessage(), exceptionWithMessage);
        } else {
          LOGGER.error(userMessage, exceptionWithMessage);
        }
        reloadPage();
      } catch (ImejiException ie) {
        LOGGER.error("Error generating API Key ", ie);
        BeanHelper.error("Error generating API Key");
      }
    }
  }

  /**
   * Add a new empty organization
   *
   * @param index
   */
  public void addOrganization(int index) {
    ((List<Organization>) this.user.getPerson().getOrganizations()).add(index, ImejiFactory.newOrganization());
  }

  /**
   * Remove an nth organization
   *
   * @param index
   */
  public void removeOrganization(int index) {
    final List<Organization> orgas = (List<Organization>) this.user.getPerson().getOrganizations();
    if (!orgas.isEmpty()) {
      orgas.remove(index);
    }
  }

  /**
   * Toggle the Admin Role of the {@link User}
   *
   * @throws Exception
   */
  public void toggleAdmin() throws ImejiException {
    final ShareService shareController = new ShareService();
    if (SecurityUtil.authorization().isSysAdmin(user)) {
      shareController.unshareSysAdmin(getSessionUser(), user);
    } else {
      shareController.shareSysAdmin(getSessionUser(), user);
    }
    reloadPage();
  }

  public boolean isSysAdmin() {
    return SecurityUtil.authorization().isSysAdmin(user);
  }

  public boolean isUniqueAdmin() {
    return ImejiSPARQL.exec(JenaCustomQueries.selectUserSysAdmin(), Imeji.userModel).size() == 1;
  }

  /**
   * Toggle the create collction role of the {@link User}
   *
   * @throws Exception
   */
  public void toggleCreateCollection() throws ImejiException {
    final ShareService shareController = new ShareService();
    if (!SecurityUtil.authorization().isSysAdmin(user)) {
      // admin can not be forbidden to create collections
      if (SecurityUtil.authorization().hasCreateCollectionGrant(user)) {
        shareController.unshareCreateCollection(getSessionUser(), user);
      } else {
        shareController.shareCreateCollection(getSessionUser(), user);
      }
    }
  }

  /**
   * Update the user in jena
   *
   * @throws ImejiException
   * @throws IOException
   */
  public void updateUser() throws ImejiException {
    if (user != null) {
      final UserService userService = new UserService();
      user.setQuota(QuotaUtil.getQuotaInBytes(quota.getQuota()));
      try {
        user = userService.update(user, getSessionUser());
        BeanHelper.info(Imeji.RESOURCE_BUNDLE.getMessage("success_save", getLocale()));
        reloadPage();
      } catch (final ImejiExceptionWithUserMessage exceptionWithMessage) {
        String userMessage = "Error updating user: " + exceptionWithMessage.getUserMessage(getLocale());
        BeanHelper.error(userMessage);
        if (exceptionWithMessage.getMessage() != null) {
          LOGGER.error(exceptionWithMessage.getMessage(), exceptionWithMessage);
        } else {
          LOGGER.error(userMessage, exceptionWithMessage);
        }
        reloadPage();
      } catch (final UnprocessableError e) {
        BeanHelper.error(e, getLocale());
        LOGGER.error("Error updating user", e);
      }
    }
  }

  /**
   * Return the quota of the current user in a user friendly way
   *
   * @param locale
   * @return
   */
  public String getQuotaHumanReadable(Locale locale) {
    if (user.getQuota() == Long.MAX_VALUE) {
      return Imeji.RESOURCE_BUNDLE.getLabel("unlimited", getLocale());
    } else {
      return FileUtils.byteCountToDisplaySize(user.getQuota());
    }
  }

  /**
   * Reload the page with the current user
   *
   * @throws IOException
   */
  private void reloadPage() {
    try {
      redirect(getUserPageUrl());
    } catch (final IOException e) {
      LOGGER.error("Error reloading user page", e);
    }
  }

  /**
   * return the URL of the current user
   *
   * @return
   */
  public String getUserPageUrl() {
    return getNavigation().getUserUrl() + "?email=" + UTF8("\"" + user.getEmail() + "\"");
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  /**
   * @return the roles
   */
  public List<ShareListItem> getRoles() {
    return roles;
  }

  public List<ShareListItem> getGroupRoles(UserGroup userGroup) throws ImejiException {
    if (userGroup != null) {
      return ShareUtil.getAllRoles(userGroup, getSessionUser(), getLocale());
    } else {
      return null;
    }
  }

  /**
   * @param roles the roles to set
   */
  public void setRoles(List<ShareListItem> roles) {
    this.roles = roles;
  }

  /**
   * @return the edit
   */
  public boolean isEdit() {
    return edit;
  }

  /**
   * @param edit the edit to set
   */
  public void setEdit(boolean edit) {
    this.edit = edit;
  }

  /**
   * @return the quota
   */
  public QuotaUICompoment getQuota() {
    return quota;
  }

  /**
   * @param quota the quota to set
   */
  public void setQuota(QuotaUICompoment quota) {
    this.quota = quota;
  }

  public String getPasswordResetUrl() {
    return UTF8(getCurrentPage().getCompleteUrl());
  }

}
