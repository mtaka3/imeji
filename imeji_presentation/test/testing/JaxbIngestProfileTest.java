package testing;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.xml.sax.SAXException;

import de.mpg.imeji.logic.ingest.jaxb.JaxbIngestProfile;
import de.mpg.imeji.logic.ingest.vo.IngestProfile;
import de.mpg.imeji.logic.ingest.vo.Items;
import de.mpg.imeji.logic.ingest.vo.MetadataProfiles;
import de.mpg.imeji.logic.vo.Item;
import de.mpg.imeji.logic.vo.Metadata;
import de.mpg.imeji.logic.vo.MetadataProfile;
import de.mpg.imeji.logic.vo.MetadataSet;
import de.mpg.imeji.logic.vo.predefinedMetadata.Text;
import de.mpg.imeji.presentation.util.PropertyReader;

public class JaxbIngestProfileTest {
	
//	@Test
	public void testPropertyReader() {
		URL solution = PropertyReader.class.getClassLoader().getResource("imeji_item_schema.xsd");
		System.out.println(solution);
	}
	
	@Test
	public void testUnmarshalMarshalItem() throws URISyntaxException {
		String xmlFilename = "C:\\Users\\hnguyen\\Development\\GitHub\\imeji\\imeji_presentation\\test\\testResources\\item.xml";
		
		try {

			JaxbIngestProfile jmp = new JaxbIngestProfile();
			
			Item item = jmp.unmarshalItem(xmlFilename);
			
//			List<MetadataSet> mdsList = item.getMetadataSets();
//			
//			MetadataSet mds = mdsList.get(0);
//			
//			Collection<Metadata> md = mds.getMetadata();
//			
//			mds.setId(new URI("http://imeji.org/mds/id/123123123123213123123"));
//			mds.setProfile(new URI("http://imeji.org/profile/12----123123"));
//			
//			Text txt = new Text();
//			txt.setId(new URI("http://imeji.org/text/id/3123"));
//			txt.setPos(0);
//			txt.setText("a text string");
//			txt.setStatement(new URI("http://imeji.org/statement/98273493123"));
//			md.add(txt);
			
//			jmp.marshalItem(xmlFile + "-output.xml", item);
			
			jmp.toString(item);
			
		} catch (JAXBException e) {
			
			e.printStackTrace();
			fail("JAXBException");
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("SAXException");
		}
	}
	
	@Test
	public void testUnmarshalMarshalItems() {
		String xmlFilename = "C:\\Users\\hnguyen\\Development\\GitHub\\imeji\\imeji_presentation\\test\\testResources\\items.xml";
		
		try {

			JaxbIngestProfile jmp = new JaxbIngestProfile();
			
			Items items = jmp.unmarshalItems(xmlFilename);
			
//			jmp.marshalItem(xmlFile + "-output.xml", item);
			
			jmp.toString(items);
			
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("JAXBException");
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("SAXException");
		}
	}	
	
	@Test
	public void testUnmarshalMarshalMdProfile() {
		String xmlFile = "C:\\Users\\hnguyen\\Development\\GitHub\\imeji\\imeji_presentation\\test\\testResources\\mdp.xml";
		
		try {

			JaxbIngestProfile jmp = new JaxbIngestProfile();
			
			MetadataProfile mpd = jmp.unmarshalMdProfile(xmlFile);
			
			if(mpd.getStatements().isEmpty()) {
				fail("no statements...");
			}
			
//			jmp.marshalMdProfile(xmlFile + "-output.xml", mpd);
			
			jmp.toString(mpd);
			
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("JAXBException");
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("SAXException");
		}
	}
	
	@Test
	public void testUnmarshalMarshalMdProfiles() {
		String xmlFilename = "C:\\Users\\hnguyen\\Development\\GitHub\\imeji\\imeji_presentation\\test\\testResources\\mdps.xml";
		
		try {

			JaxbIngestProfile jmp = new JaxbIngestProfile();
			
			MetadataProfiles mdps = jmp.unmarshalMdProfiles(xmlFilename);
			
			if(mdps.getMetadataProfile().isEmpty()) {
				fail("no profiles...");
			}
			
//			jmp.marshalMdProfile(xmlFilename + "-output.xml", mpd);
			
			jmp.toString(mdps);
			
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("JAXBException");
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("SAXException");
		}
	}
	
//	@Test
	public void testUnmarshalMarshalIngestProfile() {

		String xmlFile = "C:\\Users\\hnguyen\\Development\\GitHub\\imeji\\imeji_presentation\\test\\testResources\\ingest-profile.xml";
		
		try {
			
			JaxbIngestProfile jmp = new JaxbIngestProfile();
			
			IngestProfile ingestProfile = jmp.unmarshalIngestProfile(xmlFile);
			MetadataProfiles mdps = ingestProfile.getMdProfiles();
			
			if(mdps.getMetadataProfile().isEmpty()) {
				fail("bla?");
			}
			
//			jmp.marshalMdProfiles(xmlFile + "-output.xml", mdps);
			
			jmp.toString(mdps);
			
//			System.out.println(mpd.toString());
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("JAXBException");
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("SAXException");
		}
	}

//	static final DecimalFormat DF_2 = new DecimalFormat("#,##0.00");
//
//	/** Die main()-Methode ist nur fuer Testzwecke */
//	public static void main(String[] args) throws JAXBException, SAXException,
//			ClassNotFoundException {
//		String[] args2 = new String[3];
//		args2[0] = ImejiNamespacePrefixMapper.XSDFILE;//"C:\\Users\\hnguyen\\Development\\GitHub\\imeji\\imeji_presentation\\test\\testResources\\imeji_ingest_schema.xsd";
//		args2[1] = "C:\\Users\\hnguyen\\Development\\GitHub\\imeji\\imeji_presentation\\test\\testResources\\mdps.xml";
//		args2[2] = "MetadataProfile";
//		
//		args = args2;
//		
//		if (args.length != 3) {
//			System.out
//					.println("\nBitte XSD-Schema, XML-Dokument und Zielklasse angeben.");
//			return;
//		}
//		System.out.println("\nSchema: " + args[0] + ", XML-Dokument: "
//				+ args[1] + ", Zielklasse: " + args[2] + "\n");
//
//		// Unmarshalling-Test:
//		long startSpeicherverbrauch = ermittleSpeicherverbrauch();
//		long startZeit = System.nanoTime();
//		Object obj = unmarshal(args[0], args[1], MetadataProfiles.class);
//		String dauer = ermittleDauer(startZeit);
//		String speicherverbrauch = formatiereSpeichergroesse(ermittleSpeicherverbrauch()
//				- startSpeicherverbrauch);
//		System.out.println("Parsingspeicherverbrauch = " + speicherverbrauch
//				+ ", Parsingdauer = " + dauer);
//		System.out.println(obj.getClass());
//		// Die folgende Ausgabe macht nur Sinn, wenn es eine sinnvolle
//		// toString()-Methode gibt:
//		System.out.println(obj);
//
//		// Marshalling-Test:
//		startZeit = System.nanoTime();
//		marshal(args[0], args[1] + "-output.xml", obj);
//		dauer = ermittleDauer(startZeit);
//		System.out.println("\n'" + args[1] + "-output.xml' erzeugt in " + dauer
//				+ ".");
//	}
//
//	static String ermittleDauer(long startZeitNanoSek) {
//		long dauerMs = (System.nanoTime() - startZeitNanoSek) / 1000 / 1000;
//		if (dauerMs < 1000)
//			return "" + dauerMs + " ms";
//		return DF_2.format(dauerMs / 1000.) + " s";
//	}
//
//	static long ermittleSpeicherverbrauch() {
//		System.gc();
//		System.gc();
//		return Runtime.getRuntime().totalMemory()
//				- Runtime.getRuntime().freeMemory();
//	}
//
//	static String formatiereSpeichergroesse(long bytes) {
//		if (bytes < 0)
//			return "0 Byte";
//		if (bytes < 1024)
//			return "" + bytes + " Byte";
//		double b = bytes / 1024.;
//		if (b < 1024.)
//			return DF_2.format(b) + " KByte";
//		return DF_2.format(b / 1024.) + " MByte";
//	}
}
