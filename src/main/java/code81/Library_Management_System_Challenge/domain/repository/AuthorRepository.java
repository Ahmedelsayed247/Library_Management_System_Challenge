package code81.Library_Management_System_Challenge.domain.repository;
import code81.Library_Management_System_Challenge.domain.model.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
    List<Author> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);

    Optional<Author> findByFirstNameAndLastName(String firstName , String lastName);
}
