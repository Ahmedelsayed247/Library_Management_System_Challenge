package code81.Library_Management_System_Challenge.service;

import code81.Library_Management_System_Challenge.application.exception.DuplicateResourceException;
import code81.Library_Management_System_Challenge.application.exception.ResourceNotFoundException;
import code81.Library_Management_System_Challenge.application.service.BookService;
import code81.Library_Management_System_Challenge.application.service.UserActivityService;
import code81.Library_Management_System_Challenge.application.service.UserService;
import code81.Library_Management_System_Challenge.domain.model.*;
import code81.Library_Management_System_Challenge.domain.repository.AuthorRepository;
import code81.Library_Management_System_Challenge.domain.repository.BookRepository;
import code81.Library_Management_System_Challenge.domain.repository.CategoryRepository;
import code81.Library_Management_System_Challenge.domain.repository.PublisherRepository;
import code81.Library_Management_System_Challenge.web.dto.AuthorDTO;
import code81.Library_Management_System_Challenge.web.dto.BookDTO;
import code81.Library_Management_System_Challenge.web.dto.CategoryDTO;
import code81.Library_Management_System_Challenge.web.dto.PublisherDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private PublisherRepository publisherRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserActivityService userActivityService;

    @Mock
    private UserService userService;

    @InjectMocks
    private BookService bookService;

    private Book testBook;
    private BookDTO testBookDTO;
    private User testUser;
    private Author testAuthor;
    private Publisher testPublisher;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        // Set up test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("librarian");
        testUser.setRole(Role.LIBRARIAN);

        // Set up test author
        testAuthor = new Author();
        testAuthor.setId(1L);
        testAuthor.setFirstName("John");
        testAuthor.setLastName("Doe");

        // Set up test publisher
        testPublisher = new Publisher();
        testPublisher.setId(1L);
        testPublisher.setName("Test Publisher");

        // Set up test category
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Fiction");

        // Set up test book
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setIsbn("1234567890123");
        testBook.setSummary("Test summary");
        testBook.setPublicationYear(2023);
        testBook.setEdition("First Edition");
        testBook.setLanguage("English");
        testBook.setTotalCopies(5);
        testBook.setAvailableCopies(5);
        testBook.setAvailable(true);
        testBook.setAuthors(List.of(testAuthor));
        testBook.setPublisher(testPublisher);
        testBook.setCategory(testCategory);

        // Set up test book DTO
        testBookDTO = new BookDTO();
        testBookDTO.setTitle("Test Book");
        testBookDTO.setIsbn("1234567890123");
        testBookDTO.setSummary("Test summary");
        testBookDTO.setPublicationYear(2023);
        testBookDTO.setEdition("First Edition");
        testBookDTO.setLanguage("English");
        testBookDTO.setTotalCopies(5);
        testBookDTO.setAvailableCopies(5);

        // Set up author DTO
        AuthorDTO authorDTO = new AuthorDTO();
        authorDTO.setFirstName("John");
        authorDTO.setLastName("Doe");
        testBookDTO.setAuthors(List.of(authorDTO));

        // Set up publisher DTO
        PublisherDTO publisherDTO = new PublisherDTO();
        publisherDTO.setName("Test Publisher");
        testBookDTO.setPublisher(publisherDTO);

        // Set up category DTO
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setName("Fiction");
        testBookDTO.setCategory(categoryDTO);
    }

    @Test
    void getAllBooks_ShouldReturnAllBooks() {
        // Given
        when(bookRepository.findAll()).thenReturn(List.of(testBook));

        // When
        List<Book> result = bookService.getAllBooks();

        // Then
        assertEquals(1, result.size());
        assertEquals("Test Book", result.get(0).getTitle());
        verify(bookRepository).findAll();
    }

    @Test
    void getBookById_WhenBookExists_ShouldReturnBook() {
        // Given
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        // When
        Optional<Book> result = bookService.getBookById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Test Book", result.get().getTitle());
        verify(bookRepository).findById(1L);
    }

    @Test
    void getBookById_WhenBookDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        Optional<Book> result = bookService.getBookById(99L);

        // Then
        assertFalse(result.isPresent());
        verify(bookRepository).findById(99L);
    }

    @Test
    void getBookByIsbn_WhenBookExists_ShouldReturnBook() {
        // Given
        when(bookRepository.findByIsbn("1234567890123")).thenReturn(Optional.of(testBook));

        // When
        Optional<Book> result = bookService.getBookByIsbn("1234567890123");

        // Then
        assertTrue(result.isPresent());
        assertEquals("Test Book", result.get().getTitle());
        verify(bookRepository).findByIsbn("1234567890123");
    }

    @Test
    void createBook_WhenIsbnDoesNotExist_ShouldCreateAndLogActivity() {
        // Given
        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());
        when(authorRepository.findByFirstNameAndLastName(anyString(), anyString())).thenReturn(Optional.empty());
        when(publisherRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(categoryRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);
        when(authorRepository.save(any(Author.class))).thenReturn(testAuthor);
        when(publisherRepository.save(any(Publisher.class))).thenReturn(testPublisher);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // When
        Book result = bookService.createBook(testBookDTO);

        // Then
        assertNotNull(result);
        assertEquals("Test Book", result.getTitle());
        verify(bookRepository).save(any(Book.class));
        verify(userActivityService).logActivity(
                eq(testUser),
                eq("BOOK_CREATED"),
                contains("Created new book")
        );
    }

    @Test
    void createBook_WhenIsbnExists_ShouldThrowException() {
        // Given
        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.of(testBook));

        // When & Then
        assertThrows(DuplicateResourceException.class, () -> {
            bookService.createBook(testBookDTO);
        });
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void updateBook_WhenBookExists_ShouldUpdateAndLogActivity() {
        // Given
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);
        when(userService.getCurrentUser()).thenReturn(testUser);

        // Update DTO with new values
        testBookDTO.setTitle("Updated Book Title");
        testBookDTO.setSummary("Updated summary");

        // When
        Book result = bookService.updateBook(1L, testBookDTO);

        // Then
        assertNotNull(result);
        verify(bookRepository).save(any(Book.class));
        verify(userActivityService).logActivity(
                eq(testUser),
                eq("BOOK_UPDATED"),
                contains("Updated book")
        );
    }

    @Test
    void updateBook_WhenBookDoesNotExist_ShouldThrowException() {
        // Given
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            bookService.updateBook(99L, testBookDTO);
        });
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void deleteBook_WhenBookExists_ShouldDelete() {
        // Given
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        // When
        bookService.deleteBook(1L);

        // Then
        verify(bookRepository).delete(testBook);
    }

    @Test
    void deleteBook_WhenBookDoesNotExist_ShouldThrowException() {
        // Given
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            bookService.deleteBook(99L);
        });
        verify(bookRepository, never()).delete(any(Book.class));
    }

    @Test
    void searchBooksByTitle_ShouldReturnMatchingBooks() {
        // Given
        when(bookRepository.findByTitleContainingIgnoreCase("Test")).thenReturn(List.of(testBook));

        // When
        List<Book> result = bookService.searchBooksByTitle("Test");

        // Then
        assertEquals(1, result.size());
        assertEquals("Test Book", result.get(0).getTitle());
        verify(bookRepository).findByTitleContainingIgnoreCase("Test");
    }

    @Test
    void getBooksByCategory_ShouldReturnBooksInCategory() {
        // Given
        when(bookRepository.findByCategory(testCategory)).thenReturn(List.of(testBook));

        // When
        List<Book> result = bookService.getBooksByCategory(testCategory);

        // Then
        assertEquals(1, result.size());
        assertEquals("Test Book", result.get(0).getTitle());
        verify(bookRepository).findByCategory(testCategory);
    }

    @Test
    void getAvailableBooks_ShouldReturnAvailableBooks() {
        // Given
        when(bookRepository.findByAvailableTrue()).thenReturn(List.of(testBook));

        // When
        List<Book> result = bookService.getAvailableBooks();

        // Then
        assertEquals(1, result.size());
        assertEquals("Test Book", result.get(0).getTitle());
        verify(bookRepository).findByAvailableTrue();
    }

    @Test
    void searchBooksByAuthor_ShouldReturnBooksWithMatchingAuthor() {
        // Given
        when(bookRepository.findByAuthorName("John")).thenReturn(List.of(testBook));

        // When
        List<Book> result = bookService.searchBooksByAuthor("John");

        // Then
        assertEquals(1, result.size());
        assertEquals("Test Book", result.get(0).getTitle());
        verify(bookRepository).findByAuthorName("John");
    }

    @Test
    void getBooksByPublicationYear_ShouldReturnBooksInYearRange() {
        // Given
        when(bookRepository.findByPublicationYearBetween(2020, 2023)).thenReturn(List.of(testBook));

        // When
        List<Book> result = bookService.getBooksByPublicationYear(2020, 2023);

        // Then
        assertEquals(1, result.size());
        assertEquals("Test Book", result.get(0).getTitle());
        verify(bookRepository).findByPublicationYearBetween(2020, 2023);
    }

    @Test
    void updateBookAvailability_ShouldUpdateAvailabilityStatus() {
        // Given
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        // When
        bookService.updateBookAvailability(1L, false);

        // Then
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void updateBookAvailability_WithCopies_ShouldUpdateAvailabilityAndCopies() {
        // Given
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        // When
        Book result = bookService.updateBookAvailability(1L, 3, true);

        // Then
        assertNotNull(result);
        verify(bookRepository).save(any(Book.class));
    }
}