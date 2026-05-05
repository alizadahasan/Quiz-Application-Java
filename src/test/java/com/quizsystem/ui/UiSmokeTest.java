package com.quizsystem.ui;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UiSmokeTest {
    private static final String FX_NAMESPACE = "http://javafx.com/fxml/1";
    private static final List<String> FXML_RESOURCES = List.of(
            "/com/quizsystem/ui/login.fxml",
            "/com/quizsystem/ui/register.fxml",
            "/com/quizsystem/ui/admin.fxml",
            "/com/quizsystem/ui/user.fxml",
            "/com/quizsystem/ui/quiz.fxml",
            "/com/quizsystem/ui/results.fxml",
            "/com/quizsystem/ui/viewQuestions.fxml",
            "/com/quizsystem/ui/addQuestion.fxml"
    );

    @Test
    void fxmlFilesReferenceValidControllersAndBindings() throws Exception {
        for (String resource : FXML_RESOURCES) {
            Document document = parseFxml(resource);
            Element root = document.getDocumentElement();

            String controllerClassName = root.getAttributeNS(FX_NAMESPACE, "controller");
            assertTrue(!controllerClassName.isBlank(), "Missing fx:controller for " + resource);

            Class<?> controllerClass = Class.forName(controllerClassName);
            assertNotNull(controllerClass, "Controller class should resolve for " + resource);

            Set<String> fxIds = new LinkedHashSet<>();
            Set<String> handlerNames = new LinkedHashSet<>();
            collectBindings(root, fxIds, handlerNames);

            for (String fieldName : getFxmlFieldNames(controllerClass)) {
                assertTrue(fxIds.contains(fieldName),
                        () -> "Missing fx:id '" + fieldName + "' for " + resource);
            }

            for (String handlerName : handlerNames) {
                assertTrue(hasFxmlHandler(controllerClass, handlerName),
                        () -> "Missing FXML handler '" + handlerName + "' for " + resource);
            }
        }
    }

    @Test
    void stageAwareControllersExposeNavigationEntryPoints() throws Exception {
        assertHasMethod(LoginController.class, "setMainStage", Stage.class, Scene.class);
        assertHasMethod(RegisterController.class, "setMainStage", Stage.class, Scene.class);
        assertHasMethod(AdminController.class, "setMainStage", Stage.class, Scene.class);
        assertHasMethod(UserController.class, "setMainStage", Stage.class, Scene.class);
        assertHasMethod(QuizController.class, "setMainStage", Stage.class, Scene.class);
        assertHasMethod(ResultsController.class, "setMainStage", Stage.class, Scene.class);
        assertHasMethod(ViewQuestionsController.class, "setMainStage", Stage.class, Scene.class);
        assertHasMethod(AddQuestionController.class, "setMainStage", Stage.class, Scene.class);
    }

    private Document parseFxml(String resource) throws Exception {
        try (InputStream inputStream = getClass().getResourceAsStream(resource)) {
            assertNotNull(inputStream, "Missing FXML resource " + resource);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            return factory.newDocumentBuilder().parse(inputStream);
        }
    }

    private void collectBindings(Element element, Set<String> fxIds, Set<String> handlerNames) {
        String fxId = element.getAttributeNS(FX_NAMESPACE, "id");
        if (!fxId.isBlank()) {
            fxIds.add(fxId);
        }

        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attribute = attributes.item(i);
            String value = attribute.getNodeValue();
            if (attribute.getNamespaceURI() == null
                    && attribute.getNodeName().startsWith("on")
                    && value != null
                    && value.startsWith("#")) {
                handlerNames.add(value.substring(1));
            }
        }

        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if (child instanceof Element childElement) {
                collectBindings(childElement, fxIds, handlerNames);
            }
        }
    }

    private Set<String> getFxmlFieldNames(Class<?> type) {
        Set<String> fieldNames = new LinkedHashSet<>();
        Class<?> current = type;
        while (current != null) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(FXML.class)) {
                    fieldNames.add(field.getName());
                }
            }
            current = current.getSuperclass();
        }
        return fieldNames;
    }

    private boolean hasFxmlHandler(Class<?> type, String methodName) {
        Class<?> current = type;
        while (current != null) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getName().equals(methodName) && method.getParameterCount() <= 1) {
                    return true;
                }
            }
            current = current.getSuperclass();
        }
        return false;
    }

    private void assertHasMethod(Class<?> type, String methodName, Class<?>... parameterTypes) throws Exception {
        Method method = type.getMethod(methodName, parameterTypes);
        assertNotNull(method, () -> "Missing method " + methodName + " on " + type.getSimpleName());
    }
}
