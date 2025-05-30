package code81.Library_Management_System_Challenge;

import code81.Library_Management_System_Challenge.application.exception.InvalidOperationException;
import code81.Library_Management_System_Challenge.application.exception.ResourceNotFoundException;
import code81.Library_Management_System_Challenge.application.service.BorrowingService;
import code81.Library_Management_System_Challenge.application.service.BookService;
import code81.Library_Management_System_Challenge.application.service.MemberService;
import code81.Library_Management_System_Challenge.application.service.UserService;
import code81.Library_Management_System_Challenge.application.service.UserActivityService;
import code81.Library_Management_System_Challenge.domain.model.*;
import code81.Library_Management_System_Challenge.domain.repository.BorrowingTransactionRepository;
import code81.Library_Management_System_Challenge.web.dto.BorrowBookRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowingServiceTest {

    @Mock
    private BorrowingTransactionRepository borrowingTransactionRepository;

    @Mock
    private BookService bookService;

    @Mock
    private MemberService memberService;

    @Mock
    private UserService userService;

    @Mock
    private UserActivityService userActivityService;

    @InjectMocks
    private BorrowingService borrowingService;

    private Book testBook;
    private Member testMember;
    private User testUser;
    private BorrowingTransaction testTransaction;
    private BorrowBookRequestDTO borrowRequest;

    @BeforeEach
    void setUp() {
        testBook = new Book("Test Book", "978-0123456789", "Test Summary", 2023);
        testBook.setId(1L);
        testBook.setAvailable(true);
        testBook.setAvailableCopies(5);

        testMember = new Member("John", "Doe", "john.doe@email.com");
        testMember.setId(1L);
        testMember.setActive(true);

        testUser = new User("librarian", "password", "librarian@library.com",
                "Jane", "Smith", Role.LIBRARIAN);
        testUser.setId(1L);

        testTransaction = new BorrowingTransaction(testBook, testMember, testUser);
        testTransaction.setId(1L);
        testTransaction.setBorrowDate(LocalDate.now());
        testTransaction.setDueDate(LocalDate.now().plusDays(14));
        testTransaction.setStatus(TransactionStatus.BORROWED);

        borrowRequest = new BorrowBookRequestDTO();
        borrowRequest.setBookId(1L);
        borrowRequest.setMemberId(1L);
        borrowRequest.setIssuedByUserId(1L);
    }

    @Test
    void borrowBook_WhenValidRequest_ShouldCreateTransaction() {
        // Given
        when(bookService.getBookById(1L)).thenReturn(Optional.of(testBook));
        when(memberService.getMemberById(1L)).thenReturn(Optional.of(testMember));
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(borrowingTransactionRepository.countActiveBorrowingsByMember(testMember)).thenReturn(2L);
        when(borrowingTransactionRepository.findByMemberIdAndStatus(1L, TransactionStatus.OVERDUE))
                .thenReturn(Arrays.asList());
        when(borrowingTransactionRepository.save(any(BorrowingTransaction.class)))
                .thenReturn(testTransaction);

        // When
        BorrowingTransaction result = borrowingService.borrowBook(borrowRequest);

        // Then
        assertNotNull(result);
        assertEquals(testBook, result.getBook());
        assertEquals(testMember, result.getMember());
        assertEquals(testUser, result.getIssuedBy());
        verify(borrowingTransactionRepository).save(any(BorrowingTransaction.class));
        verify(bookService).updateBookAvailability(eq(1L), eq(4), eq(true));
        verify(userActivityService).logActivity(eq(testUser), eq("BOOK_BORROWED"), anyString());
    }

    @Test
    void borrowBook_WhenBookNotFound_ShouldThrowResourceNotFoundException() {
        // Given
        when(bookService.getBookById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            borrowingService.borrowBook(borrowRequest);
        });

        verify(borrowingTransactionRepository, never()).save(any(BorrowingTransaction.class));
    }

    @Test
    void borrowBook_WhenBookNotAvailable_ShouldThrowInvalidOperationException() {
        // Given
        testBook.setAvailable(false);
        testBook.setAvailableCopies(0);
        when(bookService.getBookById(1L)).thenReturn(Optional.of(testBook));
        when(memberService.getMemberById(1L)).thenReturn(Optional.of(testMember));
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThrows(InvalidOperationException.class, () -> {
            borrowingService.borrowBook(borrowRequest);
        });

        verify(borrowingTransactionRepository, never()).save(any(BorrowingTransaction.class));
    }

    @Test
    void borrowBook_WhenMemberNotActive_ShouldThrowInvalidOperationException() {
        // Given
        testMember.setActive(false);
        when(bookService.getBookById(1L)).thenReturn(Optional.of(testBook));
        when(memberService.getMemberById(1L)).thenReturn(Optional.of(testMember));
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThrows(InvalidOperationException.class, () -> {
            borrowingService.borrowBook(borrowRequest);
        });

        verify(borrowingTransactionRepository, never()).save(any(BorrowingTransaction.class));
    }

    @Test
    void borrowBook_WhenMemberHasMaxBooks_ShouldThrowInvalidOperationException() {
        // Given
        when(bookService.getBookById(1L)).thenReturn(Optional.of(testBook));
        when(memberService.getMemberById(1L)).thenReturn(Optional.of(testMember));
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(borrowingTransactionRepository.countActiveBorrowingsByMember(testMember)).thenReturn(5L);

        // When & Then
        assertThrows(InvalidOperationException.class, () -> {
            borrowingService.borrowBook(borrowRequest);
        });

        verify(borrowingTransactionRepository, never()).save(any(BorrowingTransaction.class));
    }

    @Test
    void returnBook_WhenValidTransaction_ShouldReturnSuccessfully() {
        // Given
        when(borrowingTransactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(borrowingTransactionRepository.save(any(BorrowingTransaction.class)))
                .thenReturn(testTransaction);

        // When
        BorrowingTransaction result = borrowingService.returnBook(1L, 1L);

        // Then
        assertNotNull(result);
        assertEquals(TransactionStatus.RETURNED, result.getStatus());
        assertNotNull(result.getReturnDate());
        assertEquals(testUser, result.getReturnedBy());
        verify(borrowingTransactionRepository).save(any(BorrowingTransaction.class));
        verify(bookService).updateBookAvailability(eq(1L), eq(6), eq(true));
        verify(userActivityService).logActivity(eq(testUser), eq("BOOK_RETURNED"), anyString());
    }

    @Test
    void returnBook_WhenTransactionNotFound_ShouldThrowResourceNotFoundException() {
        // Given
        when(borrowingTransactionRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            borrowingService.returnBook(1L, 1L);
        });

        verify(borrowingTransactionRepository, never()).save(any(BorrowingTransaction.class));
    }

    @Test
    void returnBook_WhenBookNotBorrowed_ShouldThrowInvalidOperationException() {
        // Given
        testTransaction.setStatus(TransactionStatus.RETURNED);
        when(borrowingTransactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThrows(InvalidOperationException.class, () -> {
            borrowingService.returnBook(1L, 1L);
        });

        verify(borrowingTransactionRepository, never()).save(any(BorrowingTransaction.class));
    }

    @Test
    void renewBook_WhenValidTransaction_ShouldRenewSuccessfully() {
        // Given
        when(borrowingTransactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(borrowingTransactionRepository.save(any(BorrowingTransaction.class)))
                .thenReturn(testTransaction);

        // When
        BorrowingTransaction result = borrowingService.renewBook(1L, 14);

        // Then
        assertNotNull(result);
        assertEquals(LocalDate.now().plusDays(28), result.getDueDate());
        verify(borrowingTransactionRepository).save(any(BorrowingTransaction.class));
    }

    @Test
    void renewBook_WhenTransactionNotFound_ShouldThrowResourceNotFoundException() {
        // Given
        when(borrowingTransactionRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            borrowingService.renewBook(1L, 14);
        });

        verify(borrowingTransactionRepository, never()).save(any(BorrowingTransaction.class));
    }

    @Test
    void getOverdueTransactions_ShouldReturnOverdueTransactions() {
        // Given
        List<BorrowingTransaction> overdueTransactions = Arrays.asList(testTransaction);
        when(borrowingTransactionRepository.findByDueDateBeforeAndStatus(
                any(LocalDate.class), eq(TransactionStatus.BORROWED)))
                .thenReturn(overdueTransactions);

        // When
        List<BorrowingTransaction> result = borrowingService.getOverdueTransactions();

        // Then
        assertEquals(overdueTransactions, result);
        verify(borrowingTransactionRepository).findByDueDateBeforeAndStatus(
                any(LocalDate.class), eq(TransactionStatus.BORROWED));
    }

    @Test
    void updateOverdueStatus_ShouldUpdateOverdueTransactions() {
        // Given
        List<BorrowingTransaction> overdueTransactions = Arrays.asList(testTransaction);
        when(borrowingTransactionRepository.findByDueDateBeforeAndStatus(
                any(LocalDate.class), eq(TransactionStatus.BORROWED)))
                .thenReturn(overdueTransactions);

        // When
        borrowingService.updateOverdueStatus();

        // Then
        verify(borrowingTransactionRepository).findByDueDateBeforeAndStatus(
                any(LocalDate.class), eq(TransactionStatus.BORROWED));
        verify(borrowingTransactionRepository).save(testTransaction);
        assertEquals(TransactionStatus.OVERDUE, testTransaction.getStatus());
    }
}