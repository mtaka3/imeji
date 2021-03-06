package de.mpg.imeji.rest.transfer;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import de.mpg.imeji.exceptions.ImejiException;
import de.mpg.imeji.logic.model.CollectionImeji;
import de.mpg.imeji.logic.model.ContainerAdditionalInfo;
import de.mpg.imeji.logic.model.Item;
import de.mpg.imeji.logic.model.Metadata;
import de.mpg.imeji.logic.model.Organization;
import de.mpg.imeji.logic.model.Person;
import de.mpg.imeji.logic.model.User;
import de.mpg.imeji.logic.model.factory.MetadataFactory;
import de.mpg.imeji.logic.util.ObjectHelper;
import de.mpg.imeji.logic.util.StringHelper;
import de.mpg.imeji.rest.to.CollectionTO;
import de.mpg.imeji.rest.to.ContainerAdditionalInformationTO;
import de.mpg.imeji.rest.to.IdentifierTO;
import de.mpg.imeji.rest.to.ItemTO;
import de.mpg.imeji.rest.to.MetadataTO;
import de.mpg.imeji.rest.to.OrganizationTO;
import de.mpg.imeji.rest.to.PersonTO;
import de.mpg.imeji.rest.to.defaultItemTO.DefaultItemTO;

public class TransferTOtoVO implements Serializable {

  private static final Logger LOGGER = LoggerFactory.getLogger(TransferTOtoVO.class);

  public enum TRANSFER_MODE {
    CREATE,
    UPDATE
  }

  /**
   * Transfer an {@link CollectionTO} to a {@link CollectionImeji}
   *
   * @param to
   * @param vo
   * @param mode
   * @param u
   */
  public static void transferCollection(CollectionTO to, CollectionImeji vo, TRANSFER_MODE mode, User u) {
    vo.setTitle(to.getTitle());
    if (!StringHelper.isNullOrEmptyTrim(to.getCollectionId())) {
      vo.setCollection(ObjectHelper.getURI(CollectionImeji.class, to.getCollectionId()));
    }
    vo.setDescription(to.getDescription());
    vo.setAdditionalInformations(transferAdditionalInfos(to.getAdditionalInfos()));
    // set contributors
    transferCollectionContributors(to.getContributors(), vo, u, mode);
  }

  /**
   * Transfer the list of ContainerAdditionalInformationTO to List of ContainerAdditionalInfo
   *
   * @param infosTO
   * @return
   */
  private static List<ContainerAdditionalInfo> transferAdditionalInfos(List<ContainerAdditionalInformationTO> infosTO) {
    final List<ContainerAdditionalInfo> infos = new ArrayList<>();
    for (final ContainerAdditionalInformationTO infoTO : infosTO) {
      infos.add(new ContainerAdditionalInfo(infoTO.getLabel(), infoTO.getText(), infoTO.getUrl()));
    }
    return infos;
  }

  /**
   * Transfer a {@link DefaultItemTO} to an {@link Item}
   *
   * @param to
   * @param vo
   * @param u
   * @param mode
   * @throws ImejiException
   * @throws JsonMappingException
   * @throws JsonParseException
   */
  public static void transferDefaultItem(DefaultItemTO to, Item vo, User u, TRANSFER_MODE mode) throws ImejiException {
    if (mode == TRANSFER_MODE.CREATE) {
      if (!isNullOrEmpty(to.getCollectionId())) {
        vo.setCollection(ObjectHelper.getURI(CollectionImeji.class, to.getCollectionId()));
      }
    }
    if (!isNullOrEmpty(to.getFilename())) {
      vo.setFilename(to.getFilename());
    }
    vo.getLicenses().addAll(transferLicenses(to.getLicenses()));
    transferItemMetadata(to, vo, u, mode);
  }

  /**
   * Transfer a LicenseVO to a LicenseTO
   *
   * @param licenseTOs
   * @return
   */
  private static List<de.mpg.imeji.logic.model.License> transferLicenses(List<de.mpg.imeji.rest.to.LicenseTO> licenseTOs) {
    final List<de.mpg.imeji.logic.model.License> licenses = new ArrayList<>();
    if (licenseTOs != null) {
      for (final de.mpg.imeji.rest.to.LicenseTO licTO : licenseTOs) {
        final de.mpg.imeji.logic.model.License lic = new de.mpg.imeji.logic.model.License();
        lic.setLabel(StringHelper.isNullOrEmptyTrim(licTO.getLabel()) ? licTO.getName() : licTO.getLabel());
        lic.setName(licTO.getName());
        lic.setUrl(licTO.getUrl());
        licenses.add(lic);
      }
    }
    return licenses;
  }

  /**
   * Transfer Metadata of an {@link ItemTO} to an {@link Item}
   *
   * @param to
   * @param vo
   * @param mp
   * @param u
   * @param mode
   * @throws ImejiException
   */
  public static void transferItemMetadata(DefaultItemTO to, Item vo, User u, TRANSFER_MODE mode) throws ImejiException {
    final List<Metadata> voMDs = vo.getMetadata();
    voMDs.clear();
    if (to.getMetadata() == null) {
      return;
    }
    for (final MetadataTO mdTO : to.getMetadata()) {
      final Metadata mdVO = new MetadataFactory().setStatementId(mdTO.getIndex()).setText(mdTO.getText()).setNumber(mdTO.getNumber())
          .setUrl(mdTO.getUrl()).setPerson(transferPerson(mdTO.getPerson(), new Person(), mode)).setLatitude(mdTO.getLatitude())
          .setLongitude(mdTO.getLongitude()).setDate(mdTO.getDate()).setName(mdTO.getName()).setTitle(mdTO.getTitle()).build();
      vo.getMetadata().add(mdVO);
    }
  }

  /**
   * Transfer a {@link PersonTO} into a {@link Person}
   *
   * @param pto
   * @param p
   * @param mode
   */
  public static Person transferPerson(PersonTO pto, Person p, TRANSFER_MODE mode) {
    if (pto != null) {
      if (mode == TRANSFER_MODE.CREATE) {
        final String id = pto.getIdentifiers() == null || pto.getIdentifiers().isEmpty() ? null : pto.getIdentifiers().get(0).getValue();
        /*
         * final IdentifierTO ito = new IdentifierTO();
         * ito.setValue(pto.getIdentifiers() == null || pto.getIdentifiers().isEmpty() ?
         * null : pto.getIdentifiers().get(0).getValue());
         */
        p.setIdentifier(id);
      }
      p.setFamilyName(pto.getFamilyName());
      p.setGivenName(pto.getGivenName());
      // set organizations
      transferContributorOrganizations(pto.getOrganizations(), p, mode);
      return p;
    }
    return null;
  }

  public static void transferCollectionContributors(List<PersonTO> persons, CollectionImeji vo, User u, TRANSFER_MODE mode) {
    vo.setPersons(new ArrayList<>());
    for (final PersonTO pTO : persons) {
      final Person person = new Person();
      person.setFamilyName(pTO.getFamilyName());
      person.setGivenName(pTO.getGivenName());
      if (pTO.getIdentifiers() != null) {
        if (pTO.getIdentifiers().size() == 1) {
          // set the identifier of current person
          final IdentifierTO ito = new IdentifierTO();
          ito.setValue(pTO.getIdentifiers().get(0).getValue());
          person.setIdentifier(ito.getValue());
        } else if (pTO.getIdentifiers().size() > 1) {
          LOGGER.warn("Multiple identifiers found for Person: " + pTO.getId());
        }
      }
      // set organizations
      transferContributorOrganizations(pTO.getOrganizations(), person, mode);
      vo.getPersons().add(person);
    }

    if (vo.getPersons().size() == 0 && TRANSFER_MODE.CREATE.equals(mode) && u != null)

    {
      final Person personU = new Person();
      personU.setFamilyName(u.getPerson().getFamilyName());
      personU.setGivenName(u.getPerson().getGivenName());
      if (!isNullOrEmpty(u.getPerson().getIdentifier())) {
        final IdentifierTO ito = new IdentifierTO();
        ito.setValue(u.getPerson().getIdentifier());
        personU.setIdentifier(ito.getValue());
      }
      personU.setOrganizations(u.getPerson().getOrganizations());
      vo.getPersons().add(personU);
    }

  }

  public static void transferContributorOrganizations(List<OrganizationTO> orgs, Person person, TRANSFER_MODE mode) {
    for (final OrganizationTO orgTO : orgs) {
      final Organization org = new Organization();
      org.setName(orgTO.getName());
      person.getOrganizations().add(org);
    }
  }
}
