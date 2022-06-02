package recipes;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeRepository extends CrudRepository<recipes.Recipe, Long> {
    // CREATE/UPDATE methods
    <S extends recipes.Recipe> S save(S entity);
    <S extends recipes.Recipe> Iterable<S> saveAll(Iterable<S> entities);

    // READ methods
    Optional<recipes.Recipe> findById(Long id);

    //Checks the column of categories for a given category match.
    @Query(value = "SELECT * FROM Recipe r WHERE UPPER(r.category) = ?1 ORDER BY r.date DESC",
            nativeQuery = true)
    List<recipes.Recipe> findByCategory(String category);

    //Checks the column of names for names that contain the given name.
    @Query(value = "SELECT * FROM Recipe r WHERE UPPER(r.name) like %?1% ORDER BY r.date DESC",
            nativeQuery = true)
    List<recipes.Recipe> findByName(String name);

    boolean existsById(Long id);
    Iterable<recipes.Recipe> findAll();
    Iterable<recipes.Recipe> findAllById(Iterable<Long> ids);
    long count();

    // DELETE methods
    void deleteById(Long id);
    void delete(recipes.Recipe entity);
    void deleteAllById(Iterable<? extends Long> ids);
    void deleteAll(Iterable<? extends recipes.Recipe> entities);
    void deleteAll();
}
