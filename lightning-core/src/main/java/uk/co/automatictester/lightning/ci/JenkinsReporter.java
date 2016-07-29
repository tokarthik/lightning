package uk.co.automatictester.lightning.ci;

import uk.co.automatictester.lightning.TestSet;
import uk.co.automatictester.lightning.data.JMeterTransactions;

import java.io.*;
import java.util.Properties;

public class JenkinsReporter extends CIReporter {

    public JenkinsReporter(TestSet testSet) {
        super(testSet);
    }

    public JenkinsReporter(JMeterTransactions jmeterTransactions) {
        super(jmeterTransactions);
    }

    public void setJenkinsBuildName() {
        if (testSet != null) {
            writeJenkinsFile(getVerifySummary(testSet));
        } else if (jmeterTransactions != null) {
            writeJenkinsFile(getReportSummary(jmeterTransactions));
        }
    }

    private void writeJenkinsFile(String summary) {
        try {
            Properties props = new Properties();
            props.setProperty("result.string", summary);
            OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream("lightning-jenkins.properties"), "UTF-8");
            props.store(out, "In Jenkins Build Name Setter Plugin, define build name as: ${BUILD_NUMBER} - ${PROPFILE,file=\"lightning-jenkins.properties\",property=\"result.string\"}");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}