package com.cmdbanalyzer;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppTest {

    @Test
    void applicationBootstrapPlaceholder() {
        assertTrue(true);
    }

    @Test
    void titledPanesDeclareExplicitContent() throws Exception {
        try (InputStream fxml = getClass().getResourceAsStream("/fxml/MainWindow.fxml")) {
            assertNotNull(fxml);

            Document document = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(fxml);
            NodeList titledPanes = document.getElementsByTagName("TitledPane");
            NodeList contentBlocks = document.getElementsByTagName("content");

            assertEquals(3, titledPanes.getLength());
            assertEquals(3, contentBlocks.getLength());
        }
    }
}
