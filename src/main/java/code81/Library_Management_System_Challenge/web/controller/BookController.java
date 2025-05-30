    package code81.Library_Management_System_Challenge.web.controller;

    import code81.Library_Management_System_Challenge.application.service.BookService;
    import code81.Library_Management_System_Challenge.domain.model.Book;
    import code81.Library_Management_System_Challenge.web.dto.BookDTO;
    import jakarta.validation.Valid;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;

    import java.util.List;

    @RestController
    @RequestMapping("/api/books")
    public class BookController {

        @Autowired
        private BookService bookService;

        @GetMapping
        public ResponseEntity<List<Book>> getAllBooks() {
            List<Book> books = bookService.getAllBooks();
            return ResponseEntity.ok(books);
        }

        @GetMapping("/{id}")
        public ResponseEntity<Book> getBookById(@PathVariable Long id) {
            return bookService.getBookById(id)
                    .map(book -> ResponseEntity.ok(book))
                    .orElse(ResponseEntity.notFound().build());
        }

        @GetMapping("/isbn/{isbn}")
        public ResponseEntity<Book> getBookByIsbn(@PathVariable String isbn) {
            return bookService.getBookByIsbn(isbn)
                    .map(book -> ResponseEntity.ok(book))
                    .orElse(ResponseEntity.notFound().build());
        }

        @PostMapping
        public ResponseEntity<Book> createBook(@Valid @RequestBody BookDTO book) {
            try {
                Book createdBook = bookService.createBook(book);
                return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }

        @PutMapping("/{id}")
        public ResponseEntity<Book> updateBook(@PathVariable Long id, @Valid @RequestBody BookDTO bookDetails) {
            try {
                Book updatedBook = bookService.updateBook(id, bookDetails);
                return ResponseEntity.ok(updatedBook);
            } catch (RuntimeException e) {
                return ResponseEntity.notFound().build();
            }
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
            try {
                bookService.deleteBook(id);
                return ResponseEntity.noContent().build();
            } catch (RuntimeException e) {
                return ResponseEntity.notFound().build();
            }
        }

        @GetMapping("/search")
        public ResponseEntity<List<Book>> searchBooks(
                @RequestParam(required = false) String title,
                @RequestParam(required = false) String author,
                @RequestParam(required = false) Integer startYear,
                @RequestParam(required = false) Integer endYear) {

            List<Book> books;

            if (title != null && !title.isEmpty()) {
                books = bookService.searchBooksByTitle(title);
            } else if (author != null && !author.isEmpty()) {
                books = bookService.searchBooksByAuthor(author);
            } else if (startYear != null && endYear != null) {
                books = bookService.getBooksByPublicationYear(startYear, endYear);
            } else {
                books = bookService.getAllBooks();
            }

            return ResponseEntity.ok(books);
        }

        @GetMapping("/available")
        public ResponseEntity<List<Book>> getAvailableBooks() {
            List<Book> books = bookService.getAvailableBooks();
            return ResponseEntity.ok(books);
        }
    }
