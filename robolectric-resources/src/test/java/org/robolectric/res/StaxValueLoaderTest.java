package org.robolectric.res;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class StaxValueLoaderTest {

  private PackageResourceTable resourceTable;
  private NodeHandler topLevelNodeHandler;
  private StaxDocumentLoader staxDocumentLoader;

  @Before
  public void setUp() throws Exception {
    resourceTable = new PackageResourceTable("pkg");

    topLevelNodeHandler = new NodeHandler();
    staxDocumentLoader = new StaxDocumentLoader("pkg", null, topLevelNodeHandler);
  }

  @Test
  public void ignoresXliffTags() throws Exception {
    topLevelNodeHandler.addHandler("resources", new NodeHandler()
        .addHandler("string", new StaxValueLoader(resourceTable, "string", ResType.CHAR_SEQUENCE))
    );

    parse("<resources xmlns:xliff=\"urn:oasis:names:tc:xliff:document:1.2\">" +
        "<string name=\"preposition_for_date\">on <xliff:g id=\"date\" example=\"May 29\">%s</xliff:g></string>" +
        "</resources>");

    assertThat(resourceTable.getValue(new ResName("pkg:string/preposition_for_date"), "").getData())
        .isEqualTo("on %s");
  }

  @Test
  public void ignoresBTags() throws Exception {
    topLevelNodeHandler.addHandler("resources", new NodeHandler()
        .addHandler("item[@type='string']", new StaxValueLoader(resourceTable, "string", ResType.CHAR_SEQUENCE))
    );

    parse("<resources xmlns:xliff=\"urn:oasis:names:tc:xliff:document:1.2\">" +
        "<item type=\"string\" name=\"sms_short_code_details\">This <b>may cause charges</b> on your mobile account.</item>" +
        "</resources>");

    assertThat(resourceTable.getValue(new ResName("pkg:string/sms_short_code_details"), "").getData())
        .isEqualTo("This may cause charges on your mobile account.");
  }

  private void parse(String xml) throws XMLStreamException {
    XMLInputFactory factory = XMLInputFactory.newFactory();
    XMLStreamReader xmlStreamReader = factory.createXMLStreamReader(new StringReader(xml));
    staxDocumentLoader.doParse(xmlStreamReader, new XmlContext("pkg", Fs.fileFromPath("/tmp/fake.txt")));
  }
}