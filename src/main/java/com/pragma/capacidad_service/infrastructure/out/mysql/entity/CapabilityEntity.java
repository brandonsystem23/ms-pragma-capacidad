package com.pragma.capacidad_service.infrastructure.out.mysql.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("capability")
public class CapabilityEntity {

    @Id
    private Long id;

    private String name;

    private String description;


}
