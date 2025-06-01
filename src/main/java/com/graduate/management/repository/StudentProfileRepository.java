package com.graduate.management.repository;

import com.graduate.management.entity.College;
import com.graduate.management.entity.Major;
import com.graduate.management.entity.StudentProfile;
import com.graduate.management.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {
    
    Optional<StudentProfile> findByStudentId(String studentId);
    
    Optional<StudentProfile> findByUser(User user);
    
    List<StudentProfile> findByCollege(College college);
    
    List<StudentProfile> findByMajor(Major major);
    
    List<StudentProfile> findBySupervisor(User supervisor);
    
    Page<StudentProfile> findByCollege(College college, Pageable pageable);
    
    Page<StudentProfile> findByCollegeAndApproved(College college, Boolean approved, Pageable pageable);
    
    Page<StudentProfile> findBySupervisorAndApproved(User supervisor, Boolean approved, Pageable pageable);
    
    Page<StudentProfile> findByNameContainingOrStudentIdContaining(String name, String studentId, Pageable pageable);
    
    Page<StudentProfile> findByCollegeAndNameContainingOrCollegeAndStudentIdContaining(
            College college1, String name, College college2, String studentId, Pageable pageable);
    
    List<StudentProfile> findByApproved(boolean approved);
    
    @Query("SELECT sp FROM StudentProfile sp WHERE sp.name LIKE %?1% OR sp.studentId LIKE %?1%")
    Page<StudentProfile> searchByNameOrStudentId(String keyword, Pageable pageable);
    
    @Query("SELECT sp FROM StudentProfile sp WHERE sp.college = ?1 AND (sp.name LIKE %?2% OR sp.studentId LIKE %?2%)")
    Page<StudentProfile> searchByCollegeAndNameOrStudentId(College college, String keyword, Pageable pageable);
    
    @Query("SELECT MAX(CAST(SUBSTRING(s.studentId, LENGTH(s.studentId) - 2, 3) AS int)) FROM StudentProfile s " +
           "WHERE s.major = ?1 AND s.degreeType = ?2 AND SUBSTRING(s.studentId, 1, 4) = SUBSTRING(CAST(YEAR(CURRENT_DATE) AS string), 1, 4)")
    Integer findMaxSequenceByMajorAndDegreeType(Major major, String degreeType);
    
    boolean existsByStudentId(String studentId);
}
