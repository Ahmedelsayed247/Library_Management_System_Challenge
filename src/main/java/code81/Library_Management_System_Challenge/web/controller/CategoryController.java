package code81.Library_Management_System_Challenge.web.controller;

import code81.Library_Management_System_Challenge.application.service.CategoryService;
import code81.Library_Management_System_Challenge.domain.model.BorrowingTransaction;
import code81.Library_Management_System_Challenge.domain.model.Category;
import code81.Library_Management_System_Challenge.web.dto.CategoryDTO;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
@PreAuthorize("hasAuthority('ADMIN')")
public class CategoryController {
    @Autowired
    CategoryService categoryService ;

    @PostMapping
    public ResponseEntity<Category> createCategory(@RequestBody CategoryDTO request) {
        Category category = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }
    @GetMapping("/{id}")
    public ResponseEntity<List<Category>> getAllSubCategoriesByParentId(@PathVariable Long id) {
        List<Category> transactions = categoryService.getSubcategories(id);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Category>> searchCategories(@RequestParam String name) {
        List<Category> categories = categoryService.searchByName(name);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/by-name")
    public ResponseEntity<Category> getCategoryByName(@RequestParam String name) {
        Category category = categoryService.getCategoryByName(name) ;
        return ResponseEntity.ok(category);
    }

}
