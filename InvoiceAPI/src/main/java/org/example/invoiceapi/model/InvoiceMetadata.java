package org.example.invoiceapi.model;

public class InvoiceMetadata {
    private String downloadLink;
    private long creationTime;

    public InvoiceMetadata(String downloadLink, long creationTime) {
        this.downloadLink = downloadLink;
        this.creationTime = creationTime;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    public long getCreationTime() {
        return creationTime;
    }
}
