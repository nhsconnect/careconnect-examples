package uk.nhs.careconnect.ri.client.validation;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class ResourceStatus {
    private String name;
    private String profileStatus;
    private List<InteractionStatus> interactionStatusList;

}
