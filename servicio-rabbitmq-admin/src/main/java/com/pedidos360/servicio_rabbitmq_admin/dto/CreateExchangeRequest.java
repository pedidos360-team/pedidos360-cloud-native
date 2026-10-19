package com.pedidos360.servicio_rabbitmq_admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CreateExchangeRequest {

    @NotBlank(message = "El nombre del exchange no puede estar vacío")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "El nombre del exchange contiene caracteres inválidos")
    private String name;

    @NotBlank(message = "El tipo de exchange no puede estar vacío")
    @Pattern(regexp = "^(direct|fanout|topic|headers)$",
            message = "El tipo de exchange debe ser direct, fanout, topic o headers")
    private String type;

    private boolean durable = true;

    public CreateExchangeRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isDurable() {
        return durable;
    }

    public void setDurable(boolean durable) {
        this.durable = durable;
    }
}
