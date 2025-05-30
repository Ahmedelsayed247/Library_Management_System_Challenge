package code81.Library_Management_System_Challenge.application.service;

import code81.Library_Management_System_Challenge.application.exception.DuplicateResourceException;
import code81.Library_Management_System_Challenge.application.exception.ResourceNotFoundException;
import code81.Library_Management_System_Challenge.domain.model.Author;
import code81.Library_Management_System_Challenge.domain.model.Book;
import code81.Library_Management_System_Challenge.domain.model.Category;
import code81.Library_Management_System_Challenge.domain.model.Publisher;
import code81.Library_Management_System_Challenge.domain.repository.AuthorRepository;
import code81.Library_Management_System_Challenge.domain.repository.BookRepository;
import code81.Library_Management_System_Challenge.domain.repository.CategoryRepository;
import code81.Library_Management_System_Challenge.domain.repository.PublisherRepository;
import code81.Library_Management_System_Challenge.web.dto.AuthorDTO;
import code81.Library_Management_System_Challenge.web.dto.BookDTO;
import code81.Library_Management_System_Challenge.web.dto.CategoryDTO;
import code81.Library_Management_System_Challenge.web.dto.PublisherDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private  AuthorRepository authorRepository;
    @Autowired
    private  PublisherRepository publisherRepository;
    @Autowired
    private  CategoryRepository categoryRepository;

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    public Optional<Book> getBookByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn);
    }

    public Book createBook(BookDTO bookDTO) {
        if (bookRepository.findByIsbn(bookDTO.getIsbn()).isPresent()) {
            throw new DuplicateResourceException("Book with ISBN already exists");
        }


        Book book = mapDtoToEntity(bookDTO);

        return bookRepository.save(book);
    }

    private Book mapDtoToEntity(BookDTO dto) {
        Book book = new Book();
        book.setTitle(dto.getTitle());
        book.setIsbn(dto.getIsbn());
        book.setSummary(dto.getSummary());
        book.setPublicationYear(dto.getPublicationYear());
        book.setEdition(dto.getEdition());
        book.setLanguage(dto.getLanguage());
        book.setCoverImageUrl(dto.getCoverImageUrl());
        book.setTotalCopies(dto.getTotalCopies());
        book.setAvailableCopies(dto.getAvailableCopies());
        book.setAvailable(dto.getAvailableCopies() != null && dto.getAvailableCopies() > 0);

        book.setAuthors(persistAuthors(mapAuthorDtosToEntities(dto.getAuthors())));
        book.setPublisher(persistPublisher(mapPublisherDtoToEntity(dto.getPublisher())));
        book.setCategory(persistCategory(mapCategoryDtoToEntity(dto.getCategory())));

        return book;
    }

    private List<Author> mapAuthorDtosToEntities(List<AuthorDTO> dtos) {
        if (dtos == null) return List.of();
        return dtos.stream().map(dto -> {
            Author author = new Author();
            author.setFirstName(dto.getFirstName());
            author.setLastName(dto.getLastName());
            author.setBiography(dto.getBiography());
            return author;
        }).toList();
    }

    private Publisher mapPublisherDtoToEntity(PublisherDTO dto) {
        if (dto == null) return null;
        Publisher publisher = new Publisher();
        publisher.setName(dto.getName());
        publisher.setAddress(dto.getAddress());
        publisher.setContactEmail(dto.getContactEmail());
        return publisher;
    }

    private Category mapCategoryDtoToEntity(CategoryDTO dto) {
        if (dto == null) return null;

        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());

        if (dto.getParentId() != null) {
            Category parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with id: " + dto.getParentId()));


            category.setParent(parent);
        }

        return category;
    }


    public Book updateBook(Long id, BookDTO bookDTO) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + id));

        if (bookDTO.getTitle() != null) book.setTitle(bookDTO.getTitle());
        if (bookDTO.getIsbn() != null) book.setIsbn(bookDTO.getIsbn());
        if (bookDTO.getSummary() != null) book.setSummary(bookDTO.getSummary());
        if (bookDTO.getPublicationYear() != null) book.setPublicationYear(bookDTO.getPublicationYear());
        if (bookDTO.getEdition() != null) book.setEdition(bookDTO.getEdition());
        if (bookDTO.getLanguage() != null) book.setLanguage(bookDTO.getLanguage());
        if (bookDTO.getCoverImageUrl() != null) book.setCoverImageUrl(bookDTO.getCoverImageUrl());

        if (bookDTO.getAuthors() != null)
            book.setAuthors(persistAuthors(mapAuthorDtosToEntities(bookDTO.getAuthors())));

        if (bookDTO.getPublisher() != null)
            book.setPublisher(persistPublisher(mapPublisherDtoToEntity(bookDTO.getPublisher())));

        if (bookDTO.getCategory() != null)
            book.setCategory(persistCategory(mapCategoryDtoToEntity(bookDTO.getCategory())));

        if (bookDTO.getTotalCopies() != null)
            book.setTotalCopies(bookDTO.getTotalCopies());

        if (bookDTO.getAvailableCopies() != null)
            book.setAvailableCopies(bookDTO.getAvailableCopies());

        // Update availability status only if availableCopies was updated
        if (bookDTO.getAvailableCopies() != null)
            book.setAvailable(bookDTO.getAvailableCopies() > 0);

        return bookRepository.save(book);
    }
    public Book updateBookAvailability(Long id, int availableCopies, boolean isAvailable) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));


        book.setAvailableCopies(availableCopies);
        book.setAvailable(isAvailable);

        return bookRepository.save(book);
    }



    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));

        bookRepository.delete(book);
    }

    public List<Book> searchBooksByTitle(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title);
    }

    public List<Book> getBooksByCategory(Category category) {
        return bookRepository.findByCategory(category);
    }

    public List<Book> getAvailableBooks() {
        return bookRepository.findByAvailableTrue();
    }

    public List<Book> searchBooksByAuthor(String authorName) {
        return bookRepository.findByAuthorName(authorName);
    }

    public List<Book> getBooksByPublicationYear(Integer startYear, Integer endYear) {
        return bookRepository.findByPublicationYearBetween(startYear, endYear);
    }

    public void updateBookAvailability(Long bookId, boolean available) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

        book.setAvailable(available);
        bookRepository.save(book);
    }
    private List<Author> persistAuthors(List<Author> authors) {
        return authors.stream()
                .map(author -> authorRepository.findByFirstNameAndLastName(author.getFirstName(), author.getLastName())
                        .orElseGet(() -> authorRepository.save(author)))
                .toList();
    }

    private Publisher persistPublisher(Publisher publisher) {
        if (publisher == null || publisher.getName() == null) return null;
        return publisherRepository.findByName(publisher.getName())
                .orElseGet(() -> publisherRepository.save(publisher));
    }

    private Category persistCategory(Category category) {
        if (category == null || category.getName() == null) return null;
        return categoryRepository.findByName(category.getName())
                .orElseGet(() -> categoryRepository.save(category));
    }
}
