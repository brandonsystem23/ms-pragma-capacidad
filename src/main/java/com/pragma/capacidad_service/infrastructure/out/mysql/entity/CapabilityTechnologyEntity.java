package com.pragma.capacidad_service.infrastructure.out.mysql.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("capability_technology")
public class CapabilityTechnologyEntity {

    @Id
    private Long id;

    @Column("capability_id")
    private Long capabilityId;

    @Column("technology_id")
    private Long technologyId;

}
