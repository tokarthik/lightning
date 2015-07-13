package uk.co.automatictester.lightning;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import uk.co.automatictester.lightning.exceptions.*;
import uk.co.automatictester.lightning.tests.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestSet {

    private List<Test> tests = new ArrayList<>();
    private int passCount = 0;
    private int failureCount = 0;
    private int errorCount = 0;
    private String testSetExecutionReport = "";

    public void load(String xmlFile) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(xmlFile));
            doc.getDocumentElement().normalize();

            addRespTimeAvgTests(doc);
            addRespTimeStdDevTestNodes(doc);
            addPassedTransactionsTestNodes(doc);
            addRespTimeNthPercTests(doc);

        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new XMLFileException(e.getMessage());
        }

        if (tests.size() == 0) {
            throw new XMLFileNoTestsException("No tests of expected type found in XML file");
        }
    }

    public void execute(JMeterTransactions originalJMeterTransactions) {
        for (Test test : getTests()) {
            test.execute(originalJMeterTransactions);
            if (test.isPassed()) passCount++;
            if (test.isFailed()) failureCount++;
            if (test.isError()) errorCount++;
            testSetExecutionReport += test.getReport();
        }
    }

    public String getTestSetExecutionReport() {
        return testSetExecutionReport;
    }

    public String getTestSetExecutionSummaryReport() {
        return String.format("============= EXECUTION SUMMARY =============%n"
                + "Tests executed:    %s%n"
                + "Tests passed:      %s%n"
                + "Tests failed:      %s%n"
                + "Tests with errors: %s%n"
                + "Test set status:   %s", getTests().size(), getPassCount(), getFailCount(), getErrorCount(), getTestSetStatus());
    }

    public int getPassCount() {
        return passCount;
    }

    public int getFailCount() {
        return failureCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public List<Test> getTests() {
        return tests;
    }

    private void addPassedTransactionsTestNodes(Document xmlDoc) {
        NodeList passedTransactionsTestNodes = xmlDoc.getElementsByTagName("passedTransactionsTest");
        for (int i = 0; i < passedTransactionsTestNodes.getLength(); i++) {
            Element passedTransactionsElement = (Element) passedTransactionsTestNodes.item(i);

            String name = getTestName(passedTransactionsElement);
            String description = getTestDescription(passedTransactionsElement);
            String transactionName = getTransactionName(passedTransactionsElement);

            int allowedNumberOfFailedTransactions = getIntegerValueFromElement(passedTransactionsElement, "allowedNumberOfFailedTransactions");

            PassedTransactionsTest passedTransactionsTest = new PassedTransactionsTest(name, description, transactionName, allowedNumberOfFailedTransactions);
            tests.add(passedTransactionsTest);
        }

    }

    private void addRespTimeStdDevTestNodes(Document xmlDoc) {
        NodeList respTimeStdDevTestNodes = xmlDoc.getElementsByTagName("respTimeStdDevTest");
        for (int i = 0; i < respTimeStdDevTestNodes.getLength(); i++) {
            Element respTimeStdDevTestElement = (Element) respTimeStdDevTestNodes.item(i);

            String name = getTestName(respTimeStdDevTestElement);
            String description = getTestDescription(respTimeStdDevTestElement);
            String transactionName = getTransactionName(respTimeStdDevTestElement);
            long maxRespTimeStdDevTime = Long.parseLong(getSubElementValueByTagName(respTimeStdDevTestElement, "maxRespTimeStdDev"));

            RespTimeStdDevTest respTimeStdDevTest = new RespTimeStdDevTest(name, description, transactionName, maxRespTimeStdDevTime);
            tests.add(respTimeStdDevTest);
        }
    }

    private void addRespTimeAvgTests(Document xmlDoc) {
        NodeList avgRespTimeTestNodes = xmlDoc.getElementsByTagName("avgRespTimeTest");
        for (int i = 0; i < avgRespTimeTestNodes.getLength(); i++) {
            Element avgRespTimeTestElement = (Element) avgRespTimeTestNodes.item(i);

            String name = getTestName(avgRespTimeTestElement);
            String description = getTestDescription(avgRespTimeTestElement);
            String transactionName = getTransactionName(avgRespTimeTestElement);
            long maxAvgRespTime = Long.parseLong(getSubElementValueByTagName(avgRespTimeTestElement, "maxAvgRespTime"));

            RespTimeAvgTest respTimeAvgTest = new RespTimeAvgTest(name, description, transactionName, maxAvgRespTime);
            tests.add(respTimeAvgTest);
        }
    }

    private void addRespTimeNthPercTests(Document xmlDoc) {
        NodeList respTimeNthPercTestNodes = xmlDoc.getElementsByTagName("nthPercRespTimeTest");
        for (int i = 0; i < respTimeNthPercTestNodes.getLength(); i++) {
            Element respTimeNthPercTestElement = (Element) respTimeNthPercTestNodes.item(i);

            String name = getTestName(respTimeNthPercTestElement);
            String description = getTestDescription(respTimeNthPercTestElement);
            String transactionName = getTransactionName(respTimeNthPercTestElement);
            int percentile = Integer.parseInt(getSubElementValueByTagName(respTimeNthPercTestElement, "percentile"));
            double maxRespTime = Double.parseDouble(getSubElementValueByTagName(respTimeNthPercTestElement, "maxRespTime"));

            RespTimeNthPercentileTest respTimeAvgTest = new RespTimeNthPercentileTest(name, description, transactionName, percentile, maxRespTime);
            tests.add(respTimeAvgTest);
        }
    }

    private String getTestSetStatus() {
        return (((failureCount != 0) || (getErrorCount() != 0)) ? "FAIL" : "Pass");
    }

    private String getSubElementValueByTagName(Element element, String subElement) {
        String elementValue = getNodeByTagName(element, subElement).getTextContent();
        if (elementValue.length() == 0) {
            String parentNodeName = element.getNodeName();
            throw new XMLFileMissingElementValueException(String.format("Missing %s value for %s", subElement, parentNodeName));
        } else {
            return elementValue;
        }
    }

    private Node getNodeByTagName(Element element, String subElement) {
        Node node = element.getElementsByTagName(subElement).item(0);
        if (node == null) {
            throw new XMLFileMissingElementException(String.format("Missing element %s for %s", subElement, element.getNodeName()));
        } else {
            return node;
        }
    }

    private String getTestName(Element element) {
        return getSubElementValueByTagName(element, "testName");
    }

    private String getTestDescription(Element element) {
        Node descriptionElement = element.getElementsByTagName("description").item(0);
        return (descriptionElement != null) ? descriptionElement.getTextContent() : "";
    }

    private String getTransactionName(Element element) {
        if (element.getElementsByTagName("transactionName").getLength() != 0) {
            return element.getElementsByTagName("transactionName").item(0).getTextContent();
        } else {
            return null;
        }
    }

    private int getIntegerValueFromElement(Element element, String subElement) {
        String elementValue = getSubElementValueByTagName(element, subElement);

        try {
            return Integer.parseInt(elementValue);
        } catch (NumberFormatException e) {
            String parentNodeName = element.getNodeName();
            throw new XMLFileNumberFormatException(String.format("Incorrect %s value for %s: %s", subElement, parentNodeName, elementValue));
        }
    }

}
