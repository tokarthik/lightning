package uk.co.automatictester.lightning;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import uk.co.automatictester.lightning.tests.AvgRespTimeTest;
import uk.co.automatictester.lightning.tests.PassedTransactionsTest;
import uk.co.automatictester.lightning.tests.RespTimeStdDevTest;
import uk.co.automatictester.lightning.tests.Test;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestSet {

    private static List<Test> tests = new ArrayList<Test>();
    private static int failureCount = 0;

    public static void load(String xmlFile) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(xmlFile));
            doc.getDocumentElement().normalize();

            addAvgRespTimeTests(doc);
            addRespTimeStdDevTestNodes(doc);
            addPassedTransactionsTestNodes(doc);

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }

    public static String getTestSetExecutionReport() {
        String ls = System.lineSeparator();
        int testCount = tests.size();
        int passCount = testCount - failureCount;

        String executionReport = "============= EXECUTION SUMMARY =============" + ls;
        executionReport += "Tests executed:   " + testCount + ls;
        executionReport += "Tests passed:     " + passCount + ls;
        executionReport += "Tests failed:     " + failureCount + ls;
        executionReport += "Test set status:  " + getTestSetStatus() + ls;

        return executionReport;
    }

    public static int getFailureCount() {
        return failureCount;
    }

    public static List<Test> getTests() {
        return tests;
    }

    private static void addPassedTransactionsTestNodes(Document xmlDoc) {
        NodeList passedTransactionsTestNodes = xmlDoc.getElementsByTagName("passedTransactionsTest");
        for (int i = 0; i < passedTransactionsTestNodes.getLength(); i++) {
            Element passedTransactionsElement = (Element) passedTransactionsTestNodes.item(i);

            String name = getTestName(passedTransactionsElement);
            String description = getTestDescription(passedTransactionsElement);
            String transactionName = getTransactionName(passedTransactionsElement);

            int allowedNumberOfFailedTransactions = Integer.parseInt(getElementByTagName(passedTransactionsElement, "allowedNumberOfFailedTransactions"));

            PassedTransactionsTest passedTransactionsTest = new PassedTransactionsTest(name, description, transactionName, allowedNumberOfFailedTransactions);
            tests.add(passedTransactionsTest);
        }

    }

    private static void addRespTimeStdDevTestNodes(Document xmlDoc) {
        NodeList respTimeStdDevTestNodes = xmlDoc.getElementsByTagName("respTimeStdDevTest");
        for (int i = 0; i < respTimeStdDevTestNodes.getLength(); i++) {
            Element respTimeStdDevTestElement = (Element) respTimeStdDevTestNodes.item(i);

            String name = getTestName(respTimeStdDevTestElement);
            String description = getTestDescription(respTimeStdDevTestElement);
            String transactionName = getTransactionName(respTimeStdDevTestElement);
            long maxRespTimeStdDevTime = Long.parseLong(getElementByTagName(respTimeStdDevTestElement, "maxRespTimeStdDev"));

            RespTimeStdDevTest respTimeStdDevTest = new RespTimeStdDevTest(name, description, transactionName, maxRespTimeStdDevTime);
            tests.add(respTimeStdDevTest);
        }
    }

    private static void addAvgRespTimeTests(Document xmlDoc) {
        NodeList avgRespTimeTestNodes = xmlDoc.getElementsByTagName("avgRespTimeTest");
        for (int i = 0; i < avgRespTimeTestNodes.getLength(); i++) {
            Element avgRespTimeTestElement = (Element) avgRespTimeTestNodes.item(i);

            String name = getTestName(avgRespTimeTestElement);
            String description = getTestDescription(avgRespTimeTestElement);
            String transactionName = getTransactionName(avgRespTimeTestElement);
            long maxAvgRespTime = Long.parseLong(getElementByTagName(avgRespTimeTestElement, "maxAvgRespTime"));

            AvgRespTimeTest avgRespTimeTest = new AvgRespTimeTest(name, description, transactionName, maxAvgRespTime);
            tests.add(avgRespTimeTest);
        }
    }

    private static String getTestSetStatus() {
        return ((failureCount != 0) ? "FAIL" : "Pass");
    }

    private static String getTestName(Element xmlElement) {
        return xmlElement.getElementsByTagName("testName").item(0).getTextContent();
    }

    private static String getTransactionName(Element xmlElement) {
        return xmlElement.getElementsByTagName("transactionName").item(0).getTextContent();
    }

    private static String getTestDescription(Element xmlElement) {
        Node descriptionElement = xmlElement.getElementsByTagName("description").item(0);
        return (descriptionElement != null) ? descriptionElement.getTextContent() : "";
    }

    private static String getElementByTagName(Element xmlElement, String tagName) {
        return xmlElement.getElementsByTagName(tagName).item(0).getTextContent();
    }

}
