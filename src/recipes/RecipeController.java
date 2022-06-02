package recipes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@Validated
public class RecipeController {
    @Autowired
    RecipeService recipeService;


    //Creates a new recipe entity in the db.
    @PostMapping("/api/recipe/new")
    public ResponseEntity<String> newRecipe(@RequestBody @Valid recipes.Recipe recipe) {
        recipeService.saveRecipe(recipe);
        return ResponseEntity.ok(Map.of("id", recipe.getId()).toString());
    }

    //Finds a recipe from the db by ID.
    @GetMapping("/api/recipe/{id}")
    public recipes.Recipe getRecipe(@PathVariable int id) {
        Optional<recipes.Recipe> recipe = recipeService.findById(id);
        if (recipe.isPresent()) {
            return recipe.get();
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @GetMapping(value = "/api/recipe/search", params = "category")
    public List<recipes.Recipe> searchByCategory(@RequestParam String category) {
        return recipeService.findByCategory(category);
    }

    @GetMapping(value = "/api/recipe/search", params = "name")
    public List<recipes.Recipe> searchByName(@RequestParam String name) {
        return recipeService.findByName(name);
    }

    //Deletes a recipe from the db.
    @DeleteMapping("api/recipe/{id}")
    public void deleteRecipe(@PathVariable int id) {
        Optional<recipes.Recipe> recipe = recipeService.findById(id);
        if (recipe.isPresent()) {
            recipeService.deleteById(id);
            throw new ResponseStatusException(HttpStatus.NO_CONTENT);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    //Updates a recipe in db from a given partial recipe.
    @PutMapping("/api/recipe/{id}")
    public void updateRecipe(@PathVariable int id, @Valid @RequestBody recipes.Recipe recipe) {
        Optional<recipes.Recipe> optionalRecipe = recipeService.findById(id);
        if(optionalRecipe.isPresent()) {
            recipes.Recipe savedRecipe = optionalRecipe.get();
            savedRecipe.setName(recipe.getName());
            savedRecipe.setDescription(recipe.getDescription());
            savedRecipe.setDirections(recipe.getDirections());
            savedRecipe.setIngredients(recipe.getIngredients());
            recipeService.saveRecipe(savedRecipe);
            throw new ResponseStatusException(HttpStatus.NO_CONTENT);
        } else if (!recipeService.findById(id).isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

}
