package de.mpg.imeji.presentation.share;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import de.mpg.imeji.exceptions.ImejiException;
import de.mpg.imeji.logic.Imeji;
import de.mpg.imeji.logic.search.model.SearchIndex.SearchFields;
import de.mpg.imeji.logic.share.invitation.Invitation;
import de.mpg.imeji.logic.share.invitation.InvitationBusinessController;
import de.mpg.imeji.logic.search.model.SearchOperators;
import de.mpg.imeji.logic.search.model.SearchPair;
import de.mpg.imeji.logic.search.model.SearchQuery;
import de.mpg.imeji.logic.user.UserService;
import de.mpg.imeji.logic.usergroup.UserGroupService;
import de.mpg.imeji.logic.vo.User;
import de.mpg.imeji.logic.vo.UserGroup;
import de.mpg.imeji.presentation.share.ShareBean.SharedObjectType;

/**
 * List of all entities with grant for one resource
 *
 * @author bastiens
 *
 */
public final class ShareList implements Serializable {
  private static final long serialVersionUID = -3986021952970961215L;
  private final List<ShareListItem> items = new ArrayList<ShareListItem>();
  private final List<ShareListItem> invitations = new ArrayList<ShareListItem>();


  /**
   * Create a list of all entities with grant for one resource
   *
   * @param ownerUri
   * @param sharedObjectUri
   * @param profileUri
   * @param type
   * @param currentUser
   * @throws ImejiException
   */
  public ShareList(URI ownerUri, String sharedObjectUri, String profileUri, SharedObjectType type,
      User currentUser, Locale locale) throws ImejiException {
    retrieveUsers(ownerUri, sharedObjectUri, profileUri, type, currentUser, locale);
    retrieveGroups(ownerUri, sharedObjectUri, profileUri, type, currentUser, locale);
    retrieveInvitations(sharedObjectUri, profileUri, type, currentUser, locale);
  }

  /**
   * Retrieve the user groups having grant for the this resource
   *
   * @param ownerUri
   * @param sharedObjectUri
   * @param profileUri
   * @param type
   * @param currentUser
   */
  private void retrieveGroups(URI ownerUri, String sharedObjectUri, String profileUri,
      SharedObjectType type, User currentUser, Locale locale) {
    final UserGroupService ugc = new UserGroupService();
    final Collection<UserGroup> groups = ugc.searchAndRetrieve(
        SearchQuery.toSearchQuery(
            new SearchPair(SearchFields.read, SearchOperators.EQUALS, sharedObjectUri, false)),
        null, Imeji.adminUser, 0, -1);;
    for (final UserGroup group : groups) {
      items.add(
          new ShareListItem(group, type, sharedObjectUri, profileUri, null, currentUser, locale));
    }
  }

  /**
   * Retrieve all Users having grants for this resource
   *
   * @param ownerUri
   * @param sharedObjectUri
   * @param profileUri
   * @param type
   * @param currentUser
   */
  private void retrieveUsers(URI ownerUri, String sharedObjectUri, String profileUri,
      SharedObjectType type, User currentUser, Locale locale) {
    final UserService uc = new UserService();
    final Collection<User> allUser = uc.searchAndRetrieve(
        SearchQuery.toSearchQuery(
            new SearchPair(SearchFields.read, SearchOperators.EQUALS, sharedObjectUri, false)),
        null, Imeji.adminUser, 0, -1);
    for (final User u : allUser) {
      // Do not display the creator of this collection here
      items.add(new ShareListItem(u, type, sharedObjectUri, profileUri, null, currentUser, locale,
          u.getId().toString().equals(ownerUri.toString())));
      // if (!u.getId().toString().equals(ownerUri.toString())) {
      // items.add(
      // new ShareListItem(u, type, sharedObjectUri, profileUri, null, currentUser, locale));
      // }
    }
  }

  /**
   * Retrieve all Pending invitations for this resource
   *
   * @param sharedObjectUri
   * @param profileUri
   * @param type
   * @param currentUser
   * @throws ImejiException
   */
  private void retrieveInvitations(String sharedObjectUri, String profileUri, SharedObjectType type,
      User currentUser, Locale locale) throws ImejiException {
    final InvitationBusinessController invitationBC = new InvitationBusinessController();
    for (final Invitation invitation : invitationBC.retrieveInvitationsOfObject(sharedObjectUri)) {
      invitations.add(
          new ShareListItem(invitation, type, sharedObjectUri, profileUri, currentUser, locale));
    }
  }

  public boolean isSizeEmpty() {
    return items.isEmpty() && invitations.isEmpty();
  }

  public List<ShareListItem> getItems() {
    return items;
  }

  public List<ShareListItem> getInvitations() {
    return invitations;
  }

}
