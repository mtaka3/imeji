//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.11.27 at 12:52:09 PM MEZ 
//


package de.mpg.imeji.logic.vo;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

import de.mpg.imeji.logic.ingest.vo.IngestProfile;
import de.mpg.imeji.logic.ingest.vo.Items;
import de.mpg.imeji.logic.ingest.vo.MetadataProfiles;
import de.mpg.imeji.logic.vo.Item;
import de.mpg.imeji.logic.vo.MetadataProfile;
import de.mpg.imeji.logic.vo.MetadataSet;
import de.mpg.imeji.logic.vo.Properties;
import de.mpg.imeji.logic.vo.Statement;
import de.mpg.imeji.logic.vo.predefinedMetadata.ConePerson;
import de.mpg.imeji.logic.vo.predefinedMetadata.Date;
import de.mpg.imeji.logic.vo.predefinedMetadata.Geolocation;
import de.mpg.imeji.logic.vo.predefinedMetadata.License;
import de.mpg.imeji.logic.vo.predefinedMetadata.Link;
import de.mpg.imeji.logic.vo.predefinedMetadata.Publication;
import de.mpg.imeji.logic.vo.predefinedMetadata.Text;
import de.mpg.imeji.logic.vo.predefinedMetadata.Number;
import de.mpg.j2j.misc.LocalizedString;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the classes package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _IngestProfile_QNAME = new QName("", "ingestProfile");
    private final static QName _Item_QNAME = new QName("", "item");
    private final static QName _Items_QNAME = new QName("", "items");
    private final static QName _MetadataProfile_QNAME = new QName("", "metadataProfile");
    private final static QName _MdProfiles_QNAME = new QName("", "mdProfiles");
    private final static QName _MdProfile_QNAME = new QName("", "mdProfile");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: classes
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link MetadataProfiles }
     * 
     */
    public MetadataProfiles createMetadataProfiles() {
        return new MetadataProfiles();
    }

    /**
     * Create an instance of {@link Items }
     * 
     */
    public Items createItems() {
        return new Items();
    }

    /**
     * Create an instance of {@link Link }
     * 
     */
    public Link createLink() {
        return new Link();
    }

    /**
     * Create an instance of {@link Organization }
     * 
     */
    public Organization createOrganization() {
        return new Organization();
    }

    /**
     * Create an instance of {@link Number }
     * 
     */
    public Number createNumber() {
        return new Number();
    }

    /**
     * Create an instance of {@link Publication }
     * 
     */
    public Publication createPublication() {
        return new Publication();
    }

    /**
     * Create an instance of {@link License }
     * 
     */
    public License createLicense() {
        return new License();
    }

    /**
     * Create an instance of {@link Person }
     * 
     */
    public Person createPerson() {
        return new Person();
    }

    /**
     * Create an instance of {@link Geolocation }
     * 
     */
    public Geolocation createGeolocation() {
        return new Geolocation();
    }

    /**
     * Create an instance of {@link Text }
     * 
     */
    public Text createText() {
        return new Text();
    }

    /**
     * Create an instance of {@link ConePerson }
     * 
     */
    public ConePerson createConePerson() {
        return new ConePerson();
    }

    /**
     * Create an instance of {@link Date }
     * 
     */
    public Date createDate() {
        return new Date();
    }

    /**
     * Create an instance of {@link MetadataProfile }
     * 
     */
    public MetadataProfile createMetadataProfile() {
        return new MetadataProfile();
    }

    /**
     * Create an instance of {@link MetadataSet }
     * 
     */
    public MetadataSet createMetadataSet() {
        return new MetadataSet();
    }

    /**
     * Create an instance of {@link IngestProfile }
     * 
     */
    public IngestProfile createIngestProfile() {
        return new IngestProfile();
    }

    /**
     * Create an instance of {@link LocalizedString }
     * 
     */
    public LocalizedString createLocalizedString() {
        return new LocalizedString();
    }

    /**
     * Create an instance of {@link Statement }
     * 
     */
    public Statement createStatement() {
        return new Statement();
    }

    /**
     * Create an instance of {@link Properties }
     * 
     */
    public Properties createProperties() {
        return new Properties();
    }

    /**
     * Create an instance of {@link Item }
     * 
     */
    public Item createItem() {
        return new Item();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IngestProfile }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "ingestProfile")
    public JAXBElement<IngestProfile> createIngestProfile(IngestProfile value) {
        return new JAXBElement<IngestProfile>(_IngestProfile_QNAME, IngestProfile.class, null, value);
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Item }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "item")
    public JAXBElement<Item> createItem(Item value) {
        return new JAXBElement<Item>(_Item_QNAME, Item.class, null, value);
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Items }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "items")
    public JAXBElement<Items> createItems(Items value) {
        return new JAXBElement<Items>(_Items_QNAME, Items.class, null, value);
    }
    

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MetadataProfile }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "metadataProfile")
    public JAXBElement<MetadataProfile> createMetadataProfile(MetadataProfile value) {
        return new JAXBElement<MetadataProfile>(_MetadataProfile_QNAME, MetadataProfile.class, null, value);
    }
    
    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MetadataProfiles }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "mdProfiles")
    public JAXBElement<MetadataProfiles> createMdProfiles(MetadataProfiles value) {
        return new JAXBElement<MetadataProfiles>(_MdProfiles_QNAME, MetadataProfiles.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MetadataProfile }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "mdProfile")
    public JAXBElement<MetadataProfile> createMdProfile(MetadataProfile value) {
        return new JAXBElement<MetadataProfile>(_MdProfile_QNAME, MetadataProfile.class, null, value);
    }

}
