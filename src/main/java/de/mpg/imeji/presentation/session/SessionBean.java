/**
 * License: src/main/resources/license/escidoc.license
 */
package de.mpg.imeji.presentation.session;

import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import de.mpg.imeji.logic.util.ObjectHelper;
import de.mpg.imeji.logic.vo.Album;
import de.mpg.imeji.logic.vo.CollectionImeji;
import de.mpg.imeji.logic.vo.MetadataProfile;
import de.mpg.imeji.logic.vo.User;
import de.mpg.imeji.presentation.beans.Navigation.Page;
import de.mpg.imeji.presentation.user.ShareBean.ShareType;
import de.mpg.imeji.presentation.util.CookieUtils;
import de.mpg.imeji.presentation.util.PropertyReader;

/**
 * The session Bean for imeji.
 * 
 * @author saquet (initial creation)
 * @author $Author$ (last modification)
 * @version $Revision$ $LastChangedDate$
 */
@ManagedBean
@SessionScoped
public class SessionBean
{
    public enum Style
    {
        NONE, DEFAULT, ALTERNATIVE;
    }

    private User user = null;
    // Bundle
    public static final String LABEL_BUNDLE = "labels";
    public static final String MESSAGES_BUNDLE = "messages";
    public static final String METADATA_BUNDLE = "metadata";
    // imeji locale
    private Locale locale = new Locale("en");
    private Page currentPage;
    private List<String> selected;
    private List<URI> selectedCollections;
    private List<URI> selectedAlbums;
    private Album activeAlbum;
    private Map<URI, MetadataProfile> profileCached;
    private Map<URI, CollectionImeji> collectionCached;
    private String selectedImagesContext = null;
    private Style selectedCss = Style.NONE;
    public final static String styleCookieName = "IMEJI_STYLE";
    public final static String langCookieName = "IMEJI_LANG";

    /**
     * The session Bean for imeji
     */
    public SessionBean()
    {
        selected = new ArrayList<String>();
        selectedCollections = new ArrayList<URI>();
        selectedAlbums = new ArrayList<URI>();
        profileCached = new HashMap<URI, MetadataProfile>();
        collectionCached = new HashMap<URI, CollectionImeji>();
        initLocale();
        initCssWithCookie();
    }

    /**
     * Initialize the CSS value with the Cookie value
     */
    private void initCssWithCookie()
    {
        selectedCss = Style.valueOf(CookieUtils.readNonNull(styleCookieName, Style.NONE.name()));
    }

    /**
     * Returns the label according to the current user locale.
     * 
     * @param placeholder A string containing the name of a label.
     * @return The label.
     */
    public String getLabel(String placeholder)
    {
        try
        {
            return ResourceBundle.getBundle(this.getSelectedLabelBundle()).getString(placeholder);
        }
        catch (Exception e)
        {
            return placeholder;
        }
    }

    /**
     * Returns the message according to the current user locale.
     * 
     * @param placeholder A string containing the name of a message.
     * @return The label.
     */
    public String getMessage(String placeholder)
    {
        try
        {
            return ResourceBundle.getBundle(this.getSelectedMessagesBundle()).getString(placeholder);
        }
        catch (Exception e)
        {
            return placeholder;
        }
    }

    /**
     * Get the bundle for the labels
     * 
     * @return
     */
    private String getSelectedLabelBundle()
    {
        return LABEL_BUNDLE + "_" + locale.getLanguage();
    }

    /**
     * Get the bundle for the messages
     * 
     * @return
     */
    private String getSelectedMessagesBundle()
    {
        return MESSAGES_BUNDLE + "_" + locale.getLanguage();
    }

    // public String getSelectedMetadataBundle()
    // {
    // return METADATA_BUNDLE + "_" + locale.getLanguage();
    // }
    /**
     * Return the version of the software
     * 
     * @return
     */
    public String getVersion()
    {
        return PropertyReader.getVersion();
    }

    /**
     * Return the name of the current application (defined in the property)
     * 
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    public String getInstanceName()
    {
        try
        {
            return PropertyReader.getProperty("imeji.instance.name");
        }
        catch (Exception e)
        {
            return "imeji";
        }
    }

    /**
     * First read the {@link Locale} in the request. This is the default value.Then read the cookie. If the cookie is
     * null, it is set to the default value, else the cookie value is used
     */
    private void initLocale()
    {
        FacesContext fc = FacesContext.getCurrentInstance();
        HttpServletRequest req = (HttpServletRequest)fc.getExternalContext().getRequest();
        if (req.getLocale() != null)
        {
            locale = req.getLocale();
        }
        else
        {
            locale = new Locale("en");
        }
        locale = new Locale(CookieUtils.readNonNull(langCookieName, locale.getLanguage()));
    }

    /**
     * Getter
     * 
     * @return
     */
    public Locale getLocale()
    {
        return locale;
    }

    /**
     * Setter
     * 
     * @param userLocale
     */
    public void setLocale(final Locale userLocale)
    {
        this.locale = userLocale;
    }

    /**
     * Get the context of the images (item, collection, album)
     * 
     * @return
     */
    public String getSelectedImagesContext()
    {
        return selectedImagesContext;
    }

    /**
     * setter
     * 
     * @param selectedImagesContext
     */
    public void setSelectedImagesContext(String selectedImagesContext)
    {
        this.selectedImagesContext = selectedImagesContext;
    }

    /**
     * @return the user
     */
    public User getUser()
    {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(User user)
    {
        this.user = user;
    }

    /**
     * getter
     * 
     * @return
     */
    public Page getCurrentPage()
    {
        return currentPage;
    }

    /**
     * setter
     * 
     * @param currentPage
     */
    public void setCurrentPage(Page currentPage)
    {
        this.currentPage = currentPage;
    }

    /**
     * getter
     * 
     * @return
     */
    public List<String> getSelected()
    {
        return selected;
    }

    /**
     * setter
     * 
     * @param selected
     */
    public void setSelected(List<String> selected)
    {
        this.selected = selected;
    }

    /**
     * Return the number of item selected
     * 
     * @return
     */
    public int getSelectedSize()
    {
        return selected.size();
    }

    /**
     * getter
     * 
     * @return
     */
    public List<URI> getSelectedCollections()
    {
        return selectedCollections;
    }

    /**
     * setter
     * 
     * @param selectedCollections
     */
    public void setSelectedCollections(List<URI> selectedCollections)
    {
        this.selectedCollections = selectedCollections;
    }

    /**
     * Return the number of selected collections
     * 
     * @return
     */
    public int getSelectCollectionsSize()
    {
        return this.selectedCollections.size();
    }

    /**
     * getter
     * 
     * @return
     */
    public List<URI> getSelectedAlbums()
    {
        return selectedAlbums;
    }

    /**
     * setter
     * 
     * @param selectedAlbums
     */
    public void setSelectedAlbums(List<URI> selectedAlbums)
    {
        this.selectedAlbums = selectedAlbums;
    }

    /**
     * getter
     * 
     * @return
     */
    public int getSelectedAlbumsSize()
    {
        return this.selectedAlbums.size();
    }

    /**
     * setter
     * 
     * @param activeAlbum
     */
    public void setActiveAlbum(Album activeAlbum)
    {
        this.activeAlbum = activeAlbum;
    }

    /**
     * getter
     * 
     * @return
     */
    public Album getActiveAlbum()
    {
        return activeAlbum;
    }

    /**
     * setter
     * 
     * @return
     */
    public String getActiveAlbumId()
    {
        return ObjectHelper.getId(activeAlbum.getId());
    }

    /**
     * getter
     * 
     * @return
     */
    public int getActiveAlbumSize()
    {
        return activeAlbum.getImages().size();
    }

    /**
     * Getter
     * 
     * @return
     */
    public Map<URI, MetadataProfile> getProfileCached()
    {
        return profileCached;
    }

    /**
     * Setter
     * 
     * @param profileCached
     */
    public void setProfileCached(Map<URI, MetadataProfile> profileCached)
    {
        this.profileCached = profileCached;
    }

    /**
     * @return the collectionCached
     */
    public Map<URI, CollectionImeji> getCollectionCached()
    {
        return collectionCached;
    }

    /**
     * @param collectionCached the collectionCached to set
     */
    public void setCollectionCached(Map<URI, CollectionImeji> collectionCached)
    {
        this.collectionCached = collectionCached;
    }

    /**
     * Check if the selected CSS is correct according to the configuration value. If errors are found, then change the
     * selected CSS
     * 
     * @param defaultCss - the value of the default css in the config
     * @param alternativeCss - the value of the alternative css in the config
     */
    public void checkCss(String defaultCss, String alternativeCss)
    {
        if (selectedCss == Style.ALTERNATIVE && (alternativeCss == null || "".equals(alternativeCss)))
        {
            // alternative css doesn't exist, therefore set to default
            selectedCss = Style.DEFAULT;
        }
        if (selectedCss == Style.DEFAULT && (defaultCss == null || "".equals(defaultCss)))
        {
            // default css doesn't exist, therefore set to none
            selectedCss = Style.NONE;
        }
        if (selectedCss == Style.NONE && defaultCss != null && !"".equals(defaultCss))
        {
            // default css exists, therefore set to default
            selectedCss = Style.DEFAULT;
        }
    }

    /**
     * Get the the selected {@link Style}
     * 
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    public String getSelectedCss()
    {
        return selectedCss.name();
    }

    /**
     * Toggle the selected css
     * 
     * @return
     */
    public void toggleCss()
    {
        selectedCss = selectedCss == Style.DEFAULT ? Style.ALTERNATIVE : Style.DEFAULT;
        CookieUtils.updateCookieValue(styleCookieName, selectedCss.name());
    }

    public List<SelectItem> getShareCollectionGrantItems()
    {
        List<SelectItem> itemList = new ArrayList<SelectItem>();
        itemList.add(new SelectItem(ShareType.READ, getLabel("collection_share_read")));
        itemList.add(new SelectItem(ShareType.CREATE, getLabel("collection_share_image_upload")));
        itemList.add(new SelectItem(ShareType.EDIT_ITEM, getLabel("collection_share_image_edit")));
        itemList.add(new SelectItem(ShareType.DELETE, getLabel("collection_share_image_delete")));
        itemList.add(new SelectItem(ShareType.EDIT_CONTAINER, getLabel("collection_share_collection_edit")));
        itemList.add(new SelectItem(ShareType.EDIT_PROFILE, getLabel("collection_share_profile_edit")));
        itemList.add(new SelectItem(ShareType.ADMIN, getLabel("collection_share_admin")));
        return itemList;
    }
    
    public List<SelectItem> getShareItemGrantItems()
    {
        List<SelectItem> itemList = new ArrayList<SelectItem>();
        itemList.add(new SelectItem(ShareType.READ, getLabel("collection_share_read")));
        return itemList;
    }

    public List<SelectItem> getShareAlbumGrantItems()
    {
        List<SelectItem> itemList = new ArrayList<SelectItem>();
        itemList.add(new SelectItem(ShareType.READ, getLabel("album_share_read")));
        itemList.add(new SelectItem(ShareType.CREATE, getLabel("album_share_image_add")));
        itemList.add(new SelectItem(ShareType.EDIT_CONTAINER, getLabel("album_share_album_edit")));
        itemList.add(new SelectItem(ShareType.ADMIN, getLabel("album_share_admin")));
        return itemList;
    }
}
