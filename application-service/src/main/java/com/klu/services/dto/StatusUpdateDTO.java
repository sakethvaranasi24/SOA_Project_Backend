package com.klu.services.dto;

import jakarta.validation.constraints.NotBlank;

public class StatusUpdateDTO {

    @NotBlank(message = "Status cannot be blank")
    private String status;

    public StatusUpdateDTO() {
    }

    public StatusUpdateDTO(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
