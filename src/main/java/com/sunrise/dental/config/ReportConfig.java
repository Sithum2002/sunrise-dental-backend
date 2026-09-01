package com.sunrise.dental.config;

import jakarta.annotation.PostConstruct;
import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import org.springframework.context.annotation.Configuration;

/**
 * Configures JasperReports to load the classic JRXML templates without XSD
 * validation. The parser factory bundled with JasperReports 6.21.3 validates
 * against a minimal legacy schema that does not match the JRXML syntax used by
 * this application's report templates.
 */
@Configuration
public class ReportConfig {

    @PostConstruct
    public void configureJasperReports() {
        DefaultJasperReportsContext.getInstance().setProperty(
                "net.sf.jasperreports.compiler.xml.parser.factory",
                NonValidatingSaxParserFactory.class.getName());
    }
}
