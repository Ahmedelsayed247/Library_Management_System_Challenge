package code81.Library_Management_System_Challenge.domain.repository;

import code81.Library_Management_System_Challenge.domain.model.UserActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {
    List<UserActivity> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<UserActivity> findAllByOrderByCreatedAtDesc();
}
