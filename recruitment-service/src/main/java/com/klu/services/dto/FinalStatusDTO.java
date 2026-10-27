package com.klu.services.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class FinalStatusDTO {

    @NotBlank(message = "Final status is required")
    @Pattern(regexp = "^(SELECTED|REJECTED)$", message = "Final status must be either SELECTED or REJECTED")
    private String finalStatus;

    public FinalStatusDTO() {
    }

    public FinalStatusDTO(String finalStatus) {
        this.finalStatus = finalStatus;
    }

    public String getFinalStatus() {
        return finalStatus;
    }

    public void setFinalStatus(String finalStatus) {
        this.finalStatus = finalStatus;
    }
}
