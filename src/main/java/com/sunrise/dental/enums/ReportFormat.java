package com.sunrise.dental.enums;

// Output format for generated reports.
public enum ReportFormat {
    PDF("application/pdf", "pdf"),
    CSV("text/csv", "csv");

    private final String contentType;
    private final String extension;

    ReportFormat(String contentType, String extension) {
        this.contentType = contentType;
        this.extension = extension;
    }

    public String getContentType() {
        return contentType;
    }

    public String getExtension() {
        return extension;
    }
}
