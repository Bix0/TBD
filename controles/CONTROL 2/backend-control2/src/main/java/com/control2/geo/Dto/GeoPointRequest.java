package com.control2.geo.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeoPointRequest {
    private Double longitude;    
    private Double latitude;
    private String name;
    private String sector;
}
