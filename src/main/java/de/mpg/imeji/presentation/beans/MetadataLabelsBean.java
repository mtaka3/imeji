/**
 * License: src/main/resources/license/escidoc.license
 */
package de.mpg.imeji.presentation.beans;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.util.Metadata;

import de.mpg.imeji.exceptions.ImejiException;
import de.mpg.imeji.logic.vo.Item;
import de.mpg.imeji.logic.vo.MetadataProfile;
import de.mpg.imeji.logic.vo.Statement;
import de.mpg.imeji.logic.vo.util.MetadataProfileUtil;
import de.mpg.imeji.presentation.session.SessionBean;
import de.mpg.imeji.presentation.util.BeanHelper;
import de.mpg.j2j.misc.LocalizedString;

/**
 * Utility class for the labels of the {@link Metadata}
 * 
 * @author saquet (initial creation)
 * @author $Author$ (last modification)
 * @version $Revision$ $LastChangedDate$
 */
public class MetadataLabelsBean implements Serializable {
  private static final long serialVersionUID = -5672593145712801376L;
  private String lang = "en";
  private Map<URI, String> labels = new HashMap<URI, String>();
  private Map<URI, String> internationalizedLabels = new HashMap<URI, String>();
  private static final Logger LOGGER = Logger.getLogger(MetadataLabelsBean.class);

  /**
   * Return the current bean (session scope)
   * 
   * @return
   */
  public static MetadataLabelsBean getBean() {
    return ((MetadataLabelsBean) BeanHelper.getSessionBean(MetadataLabelsBean.class));
  }

  /**
   * Initialize the labels for a {@link List} of {@link Item}
   * 
   * @param items
   * @throws Exception
   */
  public void init(List<Item> items) {
    labels = new HashMap<URI, String>();
    Map<URI, MetadataProfile> profiles;
    try {
      profiles = MetadataProfileUtil.loadProfiles(items);
      init1(new ArrayList<MetadataProfile>(profiles.values()));
    } catch (ImejiException e) {
      LOGGER.error("Error initialiting metadata labels", e);
    }
  }

  /**
   * initialize the labels for a {@link List} of {@link MetadataProfile}
   * 
   * @param profiles
   * @throws Exception
   */
  public void init1(List<MetadataProfile> profiles) {
    HashMap<URI, String> map = new HashMap<URI, String>();
    for (MetadataProfile p : profiles) {
      if (p != null) {
        init(p);
        map.putAll(internationalizedLabels);
      }
    }
    internationalizedLabels = new HashMap<URI, String>(map);
  }

  /**
   * Initialize the labels for one {@link MetadataProfile}
   * 
   * @param profile
   * @throws Exception
   */
  public void init(MetadataProfile profile) {
    labels = new HashMap<URI, String>();
    internationalizedLabels = new HashMap<URI, String>();
    lang = ((SessionBean) BeanHelper.getSessionBean(SessionBean.class)).getLocale().getLanguage();
    if (profile != null) {
      for (Statement s : profile.getStatements()) {
        boolean hasInternationalizedLabel = false;
        boolean hasEnglishLabel = false;
        String labelFallBack = null;
        for (LocalizedString ls : s.getLabels()) {
          if (ls.getLang().equals("en")) {
            labels.put(s.getId(), ls.getValue());
            hasEnglishLabel = true;
          }
          if (ls.getLang().equals(lang)) {
            internationalizedLabels.put(s.getId(), ls.getValue());
            hasInternationalizedLabel = true;
          }
          labelFallBack = ls.getValue();
        }
        if (!hasEnglishLabel) {
          labels.put(s.getId(), labelFallBack);
        }
        if (!hasInternationalizedLabel) {
          internationalizedLabels.put(s.getId(), labels.get(s.getId()));
        }
      }
    }
  }

  /**
   * getter
   * 
   * @return
   */
  public Map<URI, String> getLabels() {
    return labels;
  }

  /**
   * setter
   * 
   * @param labels
   */
  public void setLabels(Map<URI, String> labels) {
    this.labels = labels;
  }

  /**
   * getter
   * 
   * @return
   */
  public Map<URI, String> getInternationalizedLabels() {
    return internationalizedLabels;
  }

  /**
   * setter
   * 
   * @param internationalizedLabels
   */
  public void setInternationalizedLabels(Map<URI, String> internationalizedLabels) {
    this.internationalizedLabels = internationalizedLabels;
  }
}