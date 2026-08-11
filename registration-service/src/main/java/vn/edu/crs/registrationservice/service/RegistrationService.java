package vn.edu.crs.registrationservice.service;


import vn.edu.crs.registrationservice.client.CourseClient;
import vn.edu.crs.registrationservice.dto.RegistrationRequestDTO;
import vn.edu.crs.registrationservice.entity.Registration;
import vn.edu.crs.registrationservice.repository.RegistrationRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private static final String DA_DANG_KY = "DA_DANG_KY";
    private static final String DA_HUY = "DA_HUY";

    private final RegistrationRepository registrationRepository;
    private final CourseClient courseClient;

    // Dang ky mon hoc
    public Registration register(RegistrationRequestDTO dto) {

        // Kiem tra sinh vien da dang ky mon nay chua
        if (registrationRepository
                .existsByStudentIdAndCourseIdAndTrangThai(
                        dto.getStudentId(),
                        dto.getCourseId(),
                        DA_DANG_KY
                )) {

            throw new IllegalStateException(
                    "Sinh vien da dang ky mon hoc nay roi"
            );
        }

        // Buoc 1: goi sang course-service de tru cho
        // Neu het cho hoac course khong ton tai thi dung tai day
        courseClient.reserveSeat(dto.getCourseId());

        // Buoc 2: chi luu registration sau khi reserve thanh cong
        Registration registration = new Registration();

        registration.setStudentId(dto.getStudentId());
        registration.setCourseId(dto.getCourseId());
        registration.setTrangThai(DA_DANG_KY);
        registration.setNgayDangKy(LocalDateTime.now());

        return registrationRepository.save(registration);
    }

    // Huy dang ky
    public void cancel(Long registrationId) {

        Registration registration =
                registrationRepository.findById(registrationId)
                        .orElseThrow(() ->
                                new NoSuchElementException(
                                        "Khong tim thay dang ky id = "
                                                + registrationId
                                )
                        );

        // Khong cho huy 2 lan
        if (DA_HUY.equals(registration.getTrangThai())) {

            throw new IllegalStateException(
                    "Dang ky nay da duoc huy truoc do"
            );
        }

        // Hoan lai cho truoc
        courseClient.releaseSeat(
                registration.getCourseId()
        );

        // Sau do moi doi trang thai
        registration.setTrangThai(DA_HUY);

        registrationRepository.save(registration);
    }
}