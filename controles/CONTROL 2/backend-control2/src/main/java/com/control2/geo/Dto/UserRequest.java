package com.control2.geo.Dto;

import org.locationtech.jts.geom.Point;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
    private String userName;
    private String password;
    private Point geoPoint;
}
