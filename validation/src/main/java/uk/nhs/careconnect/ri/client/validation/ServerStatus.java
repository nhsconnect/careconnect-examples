package uk.nhs.careconnect.ri.client.validation;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ServerStatus {
    private String version;
    private boolean versionAvailable;
    private String status;
    private boolean isStatusActive;
    private String publisher;
    private boolean isPublisherSpecified;
    private String formats;
    private boolean isValidFormat;

}
