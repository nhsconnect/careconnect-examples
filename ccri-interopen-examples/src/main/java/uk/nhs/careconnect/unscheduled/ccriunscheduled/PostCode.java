package uk.nhs.careconnect.unscheduled.ccriunscheduled;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import uk.nhs.careconnect.eolc.Result;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PostCode {
    private Long status;

    private uk.nhs.careconnect.eolc.Result result;

    public Long getStatus() {
        return status;
    }

    public void setStatus(Long status) {
        this.status = status;
    }

    public uk.nhs.careconnect.eolc.Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }
}
