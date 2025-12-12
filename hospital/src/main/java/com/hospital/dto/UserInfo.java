// UserInfo.java
package com.hospital.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {
    private Long id;
    private String name;
    private String phone;
    private Long departmentId; // 仅医生有
    private String departmentName; // 仅医生有
    private String title; // 仅医生有
}
