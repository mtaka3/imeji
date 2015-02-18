/**
 * License: src/main/resources/license/escidoc.license
 */
package de.mpg.imeji.presentation.collection;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;

import de.mpg.imeji.logic.Imeji;
import de.mpg.imeji.logic.controller.CollectionController;
import de.mpg.imeji.logic.controller.ProfileController;
import de.mpg.imeji.logic.vo.CollectionImeji;
import de.mpg.imeji.logic.vo.MetadataProfile;
import de.mpg.imeji.logic.vo.Organization;
import de.mpg.imeji.logic.vo.Person;
import de.mpg.imeji.logic.vo.User;
import de.mpg.imeji.presentation.mdProfile.MdProfileBean;
import de.mpg.imeji.presentation.util.BeanHelper;
import de.mpg.imeji.presentation.util.ImejiFactory;

/**
 * Java Bean for the create Collection Page
 * 
 * @author saquet (initial creation)
 * @author $Author$ (last modification)
 * @version $Revision$ $LastChangedDate$
 */
@ManagedBean(name = "CreateCollectionBean")
@ViewScoped
public class CreateCollectionBean extends CollectionBean {
	private static final long serialVersionUID = 1257698224590957642L;
	
    private List<SelectItem> profileItems = new ArrayList<SelectItem>();
//    private String selectedProfileItem;
//    
//    private MdProfileBean mdProfileBean;    
	
//    /**
//     * Load the templates (i.e. the {@link MetadataProfile} that can be used by the {@link User}), and add it the the
//     * menu (sorted by name)
//     */
//    public void loadProfiles()
//    {    
//    	profileItems.clear();
//        try
//        {    
//            ProfileController pc = new ProfileController();
//            List<MetadataProfile> profiles = pc.search(sessionBean.getUser());
//            
//            for (MetadataProfile mdp : profiles)
//            {  
//            	profileItems.add(new SelectItem(mdp.getIdString(), mdp.getTitle()));
//            }           
//            selectedProfileItem = (String) profileItems.get(0).getValue();
//          
//            mdProfileBean = new MdProfileBean();
//            MetadataProfile profile = pc.retrieve(selectedProfileItem, sessionBean.getUser());
//            mdProfileBean.setProfile(profile);
//            mdProfileBean.setId(profile.getIdString());
//
//        }
//        catch (Exception e)
//        {
//            BeanHelper.error(sessionBean.getMessage("error_profile_template_load"));
//        }
//    }
//    
//    /**
//     * Listener for the template value
//     * 
//     * @param event
//     * @throws Exception
//     */
//    public void profileChangeListener(AjaxBehaviorEvent event) throws Exception
//    {
////        if (event != null && event.getNewValue() != event.getOldValue())
////        {
////            this.template = event.getNewValue().toString();
////            MetadataProfile tp = ObjectCachedLoader.loadProfile(URI.create(this.template));
////
////        }
//    }
    
	/**
	 * Bean Constructor
	 */
	public CreateCollectionBean() {
		initialize();
	}

	/**
	 * Method called when paged is loaded (defined in pretty-config.xml)
	 */
	public void initialize() {
		setCollection(ImejiFactory.newCollection());
		((List<Person>) getCollection().getMetadata().getPersons()).set(0, sessionBean.getUser().getPerson().clone());
		setModeCreate(true);
		loadProfiles();
		// if (UrlHelper.getParameterBoolean("reset")) {
		// setCollection(ImejiFactory.newCollection());
		// ((List<Person>) getCollection().getMetadata().getPersons()).set(0,
		// sessionBean.getUser().getPerson().clone());
		// }
	}

	/**
	 * Method for save button. Create the {@link CollectionImeji} according to
	 * the form
	 * 
	 * @return
	 * @throws Exception
	 */
	public String save() throws Exception {
		if (valid()) {
			// Create collection
			CollectionController collectionController = new CollectionController();
	        int pos = 0;
	        // Set the position of the statement (used for the sorting later)
	        for (Person p : getCollection().getMetadata().getPersons())
	        {
	            p.setPos(pos);
	            pos++;
	            int pos2 = 0;
	            for(Organization o : p.getOrganizations()){
	            	o.setPos(pos2);
	            	pos2++;	            	
	            }
	        }  
			collectionController.create(getCollection(), null, sessionBean.getUser());
			BeanHelper.info(sessionBean.getMessage("success_collection_create"));
			FacesContext
					.getCurrentInstance()
					.getExternalContext()
					.redirect(
							navigation.getCollectionUrl()
									+ getCollection().getIdString());
			return "";
		} else
			return "";
	}

	/**
	 * Return the link for the Cancel button
	 * 
	 * @return
	 */
	public String getCancel() {
		return navigation.getCollectionsUrl() + "?q=";
	}

	@Override
	protected String getNavigationString() {
		return "pretty:createCollection";
	}
	

    
//    public List<SelectItem> getProfileItems() {
//		return profileItems;
//	}
//
//	public void setProfileItems(List<SelectItem> profileItems) {
//		this.profileItems = profileItems;
//	}

//	public String getSelectedProfileItem() {
//		return selectedProfileItem;
//	}
//
//	public void setSelectedProfileItem(String selectedProfileItem) {
//		this.selectedProfileItem = selectedProfileItem;
//	}

}
