package code81.Library_Management_System_Challenge.domain.repository;
import code81.Library_Management_System_Challenge.domain.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    List<Member> findByActiveTrue();
    List<Member> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String firstName, String lastName, String email);
    boolean existsByEmail(String email);
}
