package org.example.invoiceapi.model;

public class InvoiceMetadata {
    private String downloadLink;
    private long creationTime;
    private long totalTime;

    public InvoiceMetadata() {
    }

    public InvoiceMetadata(String downloadLink, long creationTime, long totalTime) {
        this.downloadLink = downloadLink;
        this.creationTime = creationTime;
        this.totalTime = totalTime;
    }

    // Getters and setters
    public String getDownloadLink() {
        return downloadLink;
    }

    public void setDownloadLink(String downloadLink) {
        this.downloadLink = downloadLink;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }
}
