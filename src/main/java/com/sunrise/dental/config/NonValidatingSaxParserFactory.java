package com.sunrise.dental.config;

import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.xml.BaseSaxParserFactory;
import org.apache.commons.collections4.map.ReferenceMap;

import javax.xml.parsers.SAXParser;
import java.util.List;

/**
 * SAX parser factory used by JasperReports when loading JRXML templates.
 * The schema bundled with JasperReports 6.21.3 is a minimal/legacy schema that
 * rejects constructs of the classic JRXML format (e.g. {@code <color>} child
 * elements inside {@code <graphicElement>}). This factory skips XSD validation
 * and lets the Digester parse reports with its own rules.
 */
public class NonValidatingSaxParserFactory extends BaseSaxParserFactory {

    private final ThreadLocal<ReferenceMap<Object, Object>> grammarPoolCache = new ThreadLocal<>();

    public NonValidatingSaxParserFactory(JasperReportsContext jasperReportsContext) {
        super(jasperReportsContext);
    }

    @Override
    protected boolean isValidating() {
        return false;
    }

    @Override
    protected List<String> getSchemaLocations() {
        return List.of();
    }

    @Override
    protected void configureParser(SAXParser parser) {
        // No schema configuration - validation disabled.
    }

    @Override
    protected ThreadLocal<ReferenceMap<Object, Object>> getGrammarPoolCache() {
        return grammarPoolCache;
    }
}
