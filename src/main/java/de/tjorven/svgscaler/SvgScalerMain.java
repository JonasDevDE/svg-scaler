package de.tjorven.svgscaler;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SvgScalerMain {

    public static void main(String[] args) throws IOException {
        // Verzeichnisse sicherstellen
        File inputDir = new File("svgs/original/");
        File outputDir = new File("svgs/processed/");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // SVG-Dateien im Input-Verzeichnis verarbeiten
        Files.list(inputDir.toPath()).toList().stream().map(Path::toFile).forEach(svgFilePath -> {
            try {
                // SVG-Dokument laden
                String parser = XMLResourceDescriptor.getXMLParserClassName();
                SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
                SVGDocument svgDocument = factory.createSVGDocument(svgFilePath.toURI().toString());

                // Originalgröße der SVG
                Element root = svgDocument.getDocumentElement();
                String width = root.getAttribute("width");
                String height = root.getAttribute("height");

                // Skalierungsfaktor für "Padding"
                double scaleFactor = 0.6; // Verkleinert die Grafik auf 80%, um einen transparenten Rand zu schaffen
                String translateX = String.valueOf((Double.parseDouble(width.replace("px", "")) * (1 - scaleFactor)) / 2).replace(",", ".");
                String translateY = String.valueOf((Double.parseDouble(height.replace("px", "")) * (1 - scaleFactor)) / 2).replace(",", ".");

                // Neue Gruppe für Padding erstellen
                Element paddingGroup = svgDocument.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, "g");
                paddingGroup.setAttribute("transform", String.format("translate(%s, %s) scale(%s)", translateX, translateY, String.valueOf(scaleFactor).replace(",", ".")));

                // Verschiebe alle ursprünglichen Elemente in die Gruppe
                while (root.getFirstChild() != null) {
                    paddingGroup.appendChild(root.getFirstChild());
                }
                root.appendChild(paddingGroup);

                // Speichern der bearbeiteten SVG-Datei
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                DOMSource source = new DOMSource(svgDocument);
                StreamResult result = new StreamResult(new FileWriter(new File(outputDir, svgFilePath.getName())));
                transformer.transform(source, result);

                System.out.println("SVG-Datei erfolgreich bearbeitet und gespeichert unter: " + outputDir.getPath() + "/" + svgFilePath.getName());

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
