package vn.edu.crs.courseservice.repository;

import vn.edu.crs.courseservice.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {

    boolean existsByTenMonHocIgnoreCase(String tenMonHoc);

    // Lab 3: tim kiem mon hoc va phan trang
    Page<Course> findByTenMonHocContainingIgnoreCase(
            String keyword,
            Pageable pageable
    );
}
