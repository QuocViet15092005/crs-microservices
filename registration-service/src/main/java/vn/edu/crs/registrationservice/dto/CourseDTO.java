package vn.edu.crs.registrationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDTO {

    private Long id;
    private String tenMonHoc;
    private Integer soTinChi;
    private Integer soChoToiDa;
    private Integer soChoConLai;
}