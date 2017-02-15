package de.mpg.imeji.testimpl.logic.businesscontroller;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.mpg.imeji.exceptions.ImejiException;
import de.mpg.imeji.logic.authorization.util.SecurityUtil;
import de.mpg.imeji.logic.config.Imeji;
import de.mpg.imeji.logic.db.keyValue.KeyValueStoreService;
import de.mpg.imeji.logic.share.ShareService;
import de.mpg.imeji.logic.share.ShareService.ShareRoles;
import de.mpg.imeji.logic.share.invitation.Invitation;
import de.mpg.imeji.logic.share.invitation.InvitationService;
import de.mpg.imeji.logic.user.UserService;
import de.mpg.imeji.logic.user.UserService.USER_TYPE;
import de.mpg.imeji.logic.vo.User;
import de.mpg.imeji.logic.vo.factory.ImejiFactory;
import de.mpg.imeji.test.logic.controller.SuperServiceTest;

/**
 * Unit test for {@link InvitationService}
 * 
 * @author bastiens
 *
 */
public class InvitationBusinessControllerTest extends SuperServiceTest {
  private InvitationService invitationBC = new InvitationService();
  private static final Logger LOGGER = Logger.getLogger(InvitationBusinessControllerTest.class);
  private static final String UNKNOWN_EMAIL = "unknown@imeji.org";

  @BeforeClass
  public static void specificSetup() {
    try {
      createCollection();
      createItemWithFile();
    } catch (ImejiException e) {
      LOGGER.error("Error initializing collection or item", e);
    }
  }

  @Before
  public void resetStore() {
    KeyValueStoreService.resetAllStores();
  }

  /**
   * Invite an unknown user, create the user, check that the user got the roles from the invitation
   * 
   * @throws ImejiException
   */
  @Test
  public void inviteAndConsume() throws ImejiException {
    List<String> roles =
        ShareService.rolesAsList(ShareRoles.READ, ShareRoles.EDIT, ShareRoles.CREATE);
    Invitation invitation = new Invitation(UNKNOWN_EMAIL, collection.getId().toString(), roles);
    invitationBC.invite(invitation);
    UserService userController = new UserService();
    userController.create(getRegisteredUser(), USER_TYPE.DEFAULT);
    User user = userController.retrieve(UNKNOWN_EMAIL, Imeji.adminUser);
    Assert.assertTrue(SecurityUtil.authorization().read(user, collection));
    Assert.assertTrue(SecurityUtil.authorization().update(user, collection));
    Assert.assertTrue(SecurityUtil.authorization().createContent(user, collection));
    // Check the invitation has been deleted
    Assert.assertEquals(0, invitationBC.retrieveInvitationOfUser(UNKNOWN_EMAIL).size());
  }

  /**
   * Check that the invitation is still there after a restart of a store
   * 
   * @throws ImejiException
   */
  @Test
  public void inviteStopAndStartStore() throws ImejiException {
    List<String> roles =
        ShareService.rolesAsList(ShareRoles.READ, ShareRoles.EDIT, ShareRoles.CREATE);
    Invitation invitation = new Invitation(UNKNOWN_EMAIL, collection.getId().toString(), roles);
    invitationBC.invite(invitation);
    List<Invitation> invitationsBefore = invitationBC.retrieveInvitationOfUser(UNKNOWN_EMAIL);
    KeyValueStoreService.stopAllStores();
    KeyValueStoreService.startAllStores();
    invitationBC = new InvitationService();
    List<Invitation> invitations = invitationBC.retrieveInvitationOfUser(UNKNOWN_EMAIL);
    // Check the invitation is here
    Assert.assertEquals(invitationsBefore.size(), invitations.size());
    Assert.assertTrue(invitations.size() > 0);
  }

  @Test
  public void getAllinvitationsOfUser() throws ImejiException {
    // create many invitations for different object for one user
    List<String> roles =
        ShareService.rolesAsList(ShareRoles.READ, ShareRoles.EDIT, ShareRoles.CREATE);
    int numberOfInvitations = 15;
    for (int i = 0; i < numberOfInvitations; i++) {
      Invitation invitation =
          new Invitation(UNKNOWN_EMAIL, collection.getId().toString() + i, roles);
      invitationBC.invite(invitation);
    }
    List<Invitation> invitations = invitationBC.retrieveInvitationOfUser(UNKNOWN_EMAIL);
    Assert.assertEquals(numberOfInvitations, invitations.size());

    // Re-invite the user to the same objects, + one new objects -> allinvitations should return
    // numberOfInvitations +1
    for (int i = 0; i < numberOfInvitations + 1; i++) {
      Invitation invitation =
          new Invitation(UNKNOWN_EMAIL, collection.getId().toString() + i, roles);
      invitationBC.invite(invitation);
    }
    invitations = invitationBC.retrieveInvitationOfUser(UNKNOWN_EMAIL);
    Assert.assertEquals(numberOfInvitations + 1, invitations.size());
  }

  @Test
  public void getAllInvitationsOfObject() throws ImejiException {
    // create many invitations for one object
    List<String> roles =
        ShareService.rolesAsList(ShareRoles.READ, ShareRoles.EDIT, ShareRoles.CREATE);
    int numberOfInvitations = 15;
    for (int i = 0; i < numberOfInvitations; i++) {
      Invitation invitation =
          new Invitation(i + UNKNOWN_EMAIL, collection.getId().toString(), roles);
      invitationBC.invite(invitation);
    }
    List<Invitation> invitations =
        invitationBC.retrieveInvitationsOfObject(collection.getId().toString());
    Assert.assertEquals(numberOfInvitations, invitations.size());
    // Re-send same invitations for one object
    for (int i = 0; i < numberOfInvitations; i++) {
      Invitation invitation =
          new Invitation(i + UNKNOWN_EMAIL, collection.getId().toString(), roles);
      invitationBC.invite(invitation);
    }
    invitations = invitationBC.retrieveInvitationsOfObject(collection.getId().toString());
    Assert.assertEquals(numberOfInvitations, invitations.size());
  }


  private User getRegisteredUser() {
    User user = new User();
    user.setEmail(UNKNOWN_EMAIL);
    user.setPerson(ImejiFactory.newPerson("Unknown", "person", "somewhere"));
    return user;
  }



}
