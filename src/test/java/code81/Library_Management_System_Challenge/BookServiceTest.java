package code81.Library_Management_System_Challenge;

import code81.Library_Management_System_Challenge.application.exception.DuplicateResourceException;
import code81.Library_Management_System_Challenge.application.exception.ResourceNotFoundException;
import code81.Library_Management_System_Challenge.application.service.BookService;
import code81.Library_Management_System_Challenge.domain.model.*;
import code81.Library_Management_System_Challenge.domain.repository.*;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

    @InjectMocks
    private BookService bookService;

    private Book testBook;
    private BookDTO testBookDTO;
    private Author testAuthor;
    private Publisher testPublisher;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testAuthor = new Author("John", "Doe");
        testAuthor.setId(1L);

        testPublisher = new Publisher("Test Publisher");
        testPublisher.setId(1L);

        testCategory = new Category("Fiction");
        testCategory.setId(1L);

        testBook = new Book("Test Book", "978-0123456789", "Test Summary", 2023);
        testBook.setId(1L);
        testBook.setAuthors(Arrays.asList(testAuthor));
        testBook.setPublisher(testPublisher);
        testBook.setCategory(testCategory);
        testBook.setTotalCopies(5);
        testBook.setAvailableCopies(5);

        testBookDTO = new BookDTO();
        testBookDTO.setTitle("Test Book");
        testBookDTO.setIsbn("978-0123456789");
        testBookDTO.setSummary("Test Summary");
        testBookDTO.setPublicationYear(2023);
        testBookDTO.setTotalCopies(5);
        testBookDTO.setAvailableCopies(5);

        AuthorDTO authorDTO = new AuthorDTO();
        authorDTO.setFirstName("John");
        authorDTO.setLastName("Doe");
        testBookDTO.setAuthors(Arrays.asList(authorDTO));

        PublisherDTO publisherDTO = new PublisherDTO();
        publisherDTO.setName("Test Publisher");
        testBookDTO.setPublisher(publisherDTO);

        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setName("Fiction");
        testBookDTO.setCategory(categoryDTO);
    }

    @Test
    void getAllBooks_ShouldReturnAllBooks() {
        // Given
        List<Book> expectedBooks = Arrays.asList(testBook);
        when(bookRepository.findAll()).thenReturn(expectedBooks);

        // When
        List<Book> actualBooks = bookService.getAllBooks();

        // Then
        assertEquals(expectedBooks, actualBooks);
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
        assertEquals(testBook, result.get());
        verify(bookRepository).findById(1L);
    }

    @Test
    void getBookById_WhenBookDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        Optional<Book> result = bookService.getBookById(1L);

        // Then
        assertFalse(result.isPresent());
        verify(bookRepository).findById(1L);
    }

    @Test
    void createBook_WhenValidBook_ShouldCreateSuccessfully() {
        // Given
        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());
        when(authorRepository.findByFirstNameAndLastName(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(authorRepository.save(any(Author.class))).thenReturn(testAuthor);
        when(publisherRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(publisherRepository.save(any(Publisher.class))).thenReturn(testPublisher);
        when(categoryRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        // When
        Book result = bookService.createBook(testBookDTO);

        // Then
        assertNotNull(result);
        assertEquals(testBook.getTitle(), result.getTitle());
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void createBook_WhenISBNAlreadyExists_ShouldThrowDuplicateResourceException() {
        // Given
        when(bookRepository.findByIsbn(testBookDTO.getIsbn())).thenReturn(Optional.of(testBook));

        // When & Then
        assertThrows(DuplicateResourceException.class, () -> {
            bookService.createBook(testBookDTO);
        });

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void updateBook_WhenBookExists_ShouldUpdateSuccessfully() {
        // Given
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        BookDTO updateDTO = new BookDTO();
        updateDTO.setTitle("Updated Title");

        // When
        Book result = bookService.updateBook(1L, updateDTO);

        // Then
        assertNotNull(result);
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void updateBook_WhenBookDoesNotExist_ShouldThrowResourceNotFoundException() {
        // Given
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            bookService.updateBook(1L, testBookDTO);
        });

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void deleteBook_WhenBookExists_ShouldDeleteSuccessfully() {
        // Given
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        // When
        bookService.deleteBook(1L);

        // Then
        verify(bookRepository).delete(testBook);
    }

    @Test
    void deleteBook_WhenBookDoesNotExist_ShouldThrowResourceNotFoundException() {
        // Given
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            bookService.deleteBook(1L);
        });

        verify(bookRepository, never()).delete(any(Book.class));
    }

    @Test
    void searchBooksByTitle_ShouldReturnMatchingBooks() {
        // Given
        List<Book> expectedBooks = Arrays.asList(testBook);
        when(bookRepository.findByTitleContainingIgnoreCase("Test")).thenReturn(expectedBooks);

        // When
        List<Book> result = bookService.searchBooksByTitle("Test");

        // Then
        assertEquals(expectedBooks, result);
        verify(bookRepository).findByTitleContainingIgnoreCase("Test");
    }

    @Test
    void getAvailableBooks_ShouldReturnOnlyAvailableBooks() {
        // Given
        List<Book> expectedBooks = Arrays.asList(testBook);
        when(bookRepository.findByAvailableTrue()).thenReturn(expectedBooks);

        // When
        List<Book> result = bookService.getAvailableBooks();

        // Then
        assertEquals(expectedBooks, result);
        verify(bookRepository).findByAvailableTrue();
    }

    @Test
    void updateBookAvailability_WhenBookExists_ShouldUpdateSuccessfully() {
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