package com.cmdbanalyzer;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppTest {

    @Test
    void applicationBootstrapPlaceholder() {
        assertTrue(true);
    }

    @Test
    void applicationStartsMaximizedWithoutFullscreen() throws IOException {
        String appSource = new String(
                Files.readAllBytes(Path.of("src/main/java/com/cmdbanalyzer/app/CmdbAnalyzerApp.java")),
                StandardCharsets.UTF_8
        );

        assertTrue(appSource.contains("stage.setMaximized(true);"));
        assertFalse(appSource.contains("setFullScreen(true)"));
    }

    @Test
    void mainWindowDeclaresNavigationTree() throws Exception {
        try (InputStream fxml = getClass().getResourceAsStream("/fxml/MainWindow.fxml")) {
            assertNotNull(fxml);

            Document document = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(fxml);
            NodeList treeViews = document.getElementsByTagName("TreeView");

            assertEquals(1, treeViews.getLength());
            assertEquals("navigationTreeView", treeViews.item(0).getAttributes().getNamedItem("fx:id").getTextContent());
        }
    }
}
