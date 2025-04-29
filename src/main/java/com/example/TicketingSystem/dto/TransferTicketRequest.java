package com.example.TicketingSystem.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferTicketRequest {
    private String email;
    private String comment;
}
//.dto transcient
//Json ignore(annotation)
//stream api
