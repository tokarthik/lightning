package uk.co.automatictester;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import uk.co.automatictester.lightning.enums.Mode;

import java.io.File;

abstract class ConfigurationMojo extends AbstractMojo {

    /**
     * Execution mode.
     * Allowed values: verify, report
     */
    @Parameter
    Mode mode;

    /**
     * Lightning XML config file with test definitions
     */
    @Parameter
    File testSetXml;

    /**
     * JMeter CSV file
     */
    @Parameter(required = true)
    File jmeterCsv;

    /**
     * PerfMon CSV file
     */
    @Parameter
    File perfmonCsv;
}
