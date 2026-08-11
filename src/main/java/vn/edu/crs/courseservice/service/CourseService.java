package vn.edu.crs.courseservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.edu.crs.courseservice.dto.CourseDTO;
import vn.edu.crs.courseservice.entity.Course;
import vn.edu.crs.courseservice.repository.CourseRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;

    // Lấy toàn bộ môn học
    public List<CourseDTO> getAll() {

        List<Course> courses = courseRepository.findAll();

        System.out.println("=================================");
        System.out.println("Tong so mon hoc = " + courses.size());

        courses.forEach(course -> {
            System.out.println(
                    course.getId() + " - " +
                            course.getTenMonHoc() + " - " +
                            course.getSoTinChi()
            );
        });

        return courses.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Lấy môn học theo ID
    public CourseDTO getById(Long id) {

        Course course = courseRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Khong tim thay mon hoc id = " + id
                        )
                );

        return toDTO(course);
    }

    // Tạo môn học mới
    public CourseDTO create(CourseDTO dto) {

        if (courseRepository.existsByTenMonHocIgnoreCase(dto.getTenMonHoc())) {
            throw new IllegalArgumentException(
                    "Ten mon hoc da ton tai"
            );
        }

        Course course = new Course();

        course.setTenMonHoc(dto.getTenMonHoc());
        course.setSoTinChi(dto.getSoTinChi());
        course.setSoChoToiDa(dto.getSoChoToiDa());

        // Khi tạo mới: số chỗ còn lại = số chỗ tối đa
        course.setSoChoConLai(dto.getSoChoToiDa());

        return toDTO(
                courseRepository.save(course)
        );
    }

    // Cập nhật môn học
    public CourseDTO update(Long id, CourseDTO dto) {

        Course course = courseRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Khong tim thay mon hoc id = " + id
                        )
                );

        course.setTenMonHoc(dto.getTenMonHoc());
        course.setSoTinChi(dto.getSoTinChi());
        course.setSoChoToiDa(dto.getSoChoToiDa());

        // Không sửa trực tiếp soChoConLai ở update thông thường

        return toDTO(
                courseRepository.save(course)
        );
    }

    // Xóa môn học
    public void delete(Long id) {

        if (!courseRepository.existsById(id)) {
            throw new NoSuchElementException(
                    "Khong tim thay mon hoc id = " + id
            );
        }

        courseRepository.deleteById(id);
    }

    // Lab 3: trừ 1 chỗ khi đăng ký
    @Transactional
    public CourseDTO reserveSeat(Long id) {

        Course course = courseRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Khong tim thay mon hoc id = " + id
                        )
                );

        if (course.getSoChoConLai() <= 0) {
            throw new IllegalStateException(
                    "Mon hoc da het cho, khong the dang ky"
            );
        }

        course.setSoChoConLai(
                course.getSoChoConLai() - 1
        );

        return toDTO(
                courseRepository.save(course)
        );
    }

    // Lab 3: hoàn lại 1 chỗ khi hủy đăng ký
    @Transactional
    public CourseDTO releaseSeat(Long id) {

        Course course = courseRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Khong tim thay mon hoc id = " + id
                        )
                );

        // Không cho tăng vượt quá số chỗ tối đa
        if (course.getSoChoConLai() < course.getSoChoToiDa()) {

            course.setSoChoConLai(
                    course.getSoChoConLai() + 1
            );
        }

        return toDTO(
                courseRepository.save(course)
        );
    }

    // Lab 3: tìm kiếm + phân trang
    public Page<CourseDTO> search(
            String keyword,
            Pageable pageable) {

        Page<Course> page;

        if (keyword == null || keyword.isBlank()) {

            page = courseRepository.findAll(pageable);

        } else {

            page = courseRepository
                    .findByTenMonHocContainingIgnoreCase(
                            keyword,
                            pageable
                    );
        }

        return page.map(this::toDTO);
    }

    // Chuyển Entity sang DTO
    private CourseDTO toDTO(Course course) {

        return new CourseDTO(
                course.getId(),
                course.getTenMonHoc(),
                course.getSoTinChi(),
                course.getSoChoToiDa(),
                course.getSoChoConLai()
        );
    }
}