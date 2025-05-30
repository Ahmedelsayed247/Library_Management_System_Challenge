package code81.Library_Management_System_Challenge.domain.repository;
import code81.Library_Management_System_Challenge.domain.model.Book;
import code81.Library_Management_System_Challenge.domain.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByIsbn(String isbn);
    List<Book> findByTitleContainingIgnoreCase(String title);
    List<Book> findByCategory(Category category);
    List<Book> findByAvailableTrue();

    @Query("SELECT b FROM Book b JOIN b.authors a WHERE a.firstName LIKE %:name% OR a.lastName LIKE %:name%")
    List<Book> findByAuthorName(@Param("name") String name);

    @Query("SELECT b FROM Book b WHERE b.publicationYear BETWEEN :startYear AND :endYear")
    List<Book> findByPublicationYearBetween(@Param("startYear") Integer startYear, @Param("endYear") Integer endYear);
}
