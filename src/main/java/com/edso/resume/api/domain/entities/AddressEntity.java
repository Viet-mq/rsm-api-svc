package com.edso.resume.api.domain.entities;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressEntity {
    private String id;
    private String officeName;
    private String name;
}
