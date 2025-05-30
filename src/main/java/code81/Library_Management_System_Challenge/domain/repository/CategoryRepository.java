package code81.Library_Management_System_Challenge.domain.repository;
import code81.Library_Management_System_Challenge.domain.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByParentIsNull(); // Root categories
    List<Category> findByParent(Category parent);
    List<Category> findByNameContainingIgnoreCase(String name);

    Optional<Category> findByName(String name);
}
