package uk.nhs.careconnect.ri.client.validation;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Builder
@Data
public class InteractionStatus {
    private String interactionName;
    private String status;
    private String data;

    @Singular("subInteraction")
    private List<InteractionStatus> subInteraction;
}
