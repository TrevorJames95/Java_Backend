package recipes;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

//Business Layer for CrudRepository Methods.
@Service
public class RecipeService {
    private final RecipeRepository recipeRepository;

    @Autowired
    public RecipeService(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    public Optional<Recipe> findById(long id) {
        return recipeRepository.findById(id);
    }

    public Recipe saveRecipe(Recipe toSave) {
        return recipeRepository.save(toSave);
    }


    public void deleteById(long id) {
        recipeRepository.deleteById(id);
    }

    public List<Recipe> findByName(String name){
        return recipeRepository.findByName(name.toUpperCase());
    }

    public List<Recipe> findByCategory(String category) {
        return recipeRepository.findByCategory(category.toUpperCase());
    }


}
