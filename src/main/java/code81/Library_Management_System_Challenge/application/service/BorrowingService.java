package code81.Library_Management_System_Challenge.application.service;

import code81.Library_Management_System_Challenge.application.exception.InvalidOperationException;
import code81.Library_Management_System_Challenge.application.exception.ResourceNotFoundException;
import code81.Library_Management_System_Challenge.domain.model.*;
import code81.Library_Management_System_Challenge.domain.repository.BorrowingTransactionRepository;
import code81.Library_Management_System_Challenge.web.dto.BorrowBookRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class BorrowingService {

    @Autowired
    private BorrowingTransactionRepository borrowingTransactionRepository;

    @Autowired
    private BookService bookService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserActivityService userActivityService;
    private static final int MAX_BOOKS_PER_MEMBER = 5;
    private static final double NORMAL_FINE_PER_DAY = 1.0;
    private static final double OVERDUE_FINE_PER_DAY = NORMAL_FINE_PER_DAY * 2;


    public List<BorrowingTransaction> getAllTransactions() {
        return borrowingTransactionRepository.findAll();
    }

    public Optional<BorrowingTransaction> getTransactionById(Long id) {
        return borrowingTransactionRepository.findById(id);
    }

    public List<BorrowingTransaction> getTransactionsByMember(Long memberId) {
        Member member = memberService.getMemberById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + memberId));
        return borrowingTransactionRepository.findByMember(member);
    }

    public List<BorrowingTransaction> getOverdueTransactions() {
        return borrowingTransactionRepository.findByDueDateBeforeAndStatus(
                LocalDate.now(), TransactionStatus.BORROWED);
    }

    @Transactional
    public BorrowingTransaction borrowBook(BorrowBookRequestDTO request) {
        Book book = bookService.getBookById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + request.getBookId()));

        Member member = memberService.getMemberById(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + request.getMemberId()));

        User user = userService.getUserById(request.getIssuedByUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getIssuedByUserId()));

        System.out.println("Found book: " + book.getTitle() + ", member: " + member.getFullName() + ", user: " + user.getId());

        // Check if book is available
        if (!book.isAvailable() || book.getAvailableCopies() <= 0) {
            throw new InvalidOperationException("Book is not available for borrowing");
        }

        if (!member.isActive()) {
            throw new InvalidOperationException("Member account is not active");
        }

        Long activeBorrowings = borrowingTransactionRepository.countActiveBorrowingsByMember(member);
        if (activeBorrowings >= MAX_BOOKS_PER_MEMBER) {
            throw new InvalidOperationException("Member has reached maximum borrowing limit");
        }

        List<BorrowingTransaction> memberOverdue = borrowingTransactionRepository
                .findByMemberIdAndStatus(request.getMemberId(), TransactionStatus.OVERDUE);
        if (!memberOverdue.isEmpty()) {
            throw new InvalidOperationException("Member has overdue books. Please return them first.");
        }

        // Create borrowing transaction
        BorrowingTransaction transaction = new BorrowingTransaction(book, member, user);
        transaction = borrowingTransactionRepository.save(transaction);

        // Update book availability
        int newAvailableCopies = book.getAvailableCopies() - 1;
        boolean newAvailability = newAvailableCopies > 0;

        bookService.updateBookAvailability(book.getId(), newAvailableCopies, newAvailability);
        userActivityService.logActivity(user, "BOOK_BORROWED", "Book '" + book.getTitle() + "' borrowed by member " + member.getFullName());

        return transaction;
    }


    @Transactional
    public BorrowingTransaction returnBook(Long transactionId, Long userId) {
        BorrowingTransaction transaction = borrowingTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + transactionId));

        User user = userService.getUserById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (transaction.getStatus() != TransactionStatus.BORROWED) {
            throw new InvalidOperationException("Book is not currently borrowed");
        }

        LocalDate borrowDate = transaction.getBorrowDate();
        LocalDate dueDate = transaction.getDueDate();
        LocalDate returnDate = LocalDate.now();

        transaction.setReturnDate(returnDate);
        transaction.setReturnedBy(user);
        transaction.setStatus(TransactionStatus.RETURNED);

        double fine = 0.0;

        if (returnDate.isAfter(dueDate)) {
            long normalDays = ChronoUnit.DAYS.between(borrowDate, dueDate);
            long overdueDays = ChronoUnit.DAYS.between(dueDate, returnDate);
            fine = (normalDays * NORMAL_FINE_PER_DAY) + (overdueDays * OVERDUE_FINE_PER_DAY);
        } else {
            long totalDays = ChronoUnit.DAYS.between(borrowDate, returnDate);
            fine = totalDays * NORMAL_FINE_PER_DAY;
        }

        transaction.setFineAmount(fine);

        transaction = borrowingTransactionRepository.save(transaction);

        // Update book availability
        Book book = transaction.getBook();
        int newAvailableCopies = book.getAvailableCopies() + 1;
        bookService.updateBookAvailability(book.getId(), newAvailableCopies, true);

        // Log the return activity
        userActivityService.logActivity(user, "BOOK_RETURNED",
                "Book '" + book.getTitle() + "' returned by member " + transaction.getMember().getFullName());

        return transaction;
    }

    public BorrowingTransaction renewBook(Long transactionId, int days) {
        BorrowingTransaction transaction = borrowingTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + transactionId));

        if (transaction.getStatus() != TransactionStatus.BORROWED) {
            throw new InvalidOperationException("Book is not currently borrowed");
        }

        if (transaction.isOverdue()) {
            throw new InvalidOperationException("Cannot renew overdue book");
        }
        // Extend due date
        transaction.setDueDate(transaction.getDueDate().plusDays(days));
        return borrowingTransactionRepository.save(transaction);
    }
    @Scheduled(cron = "0 0 0 * * *") // run every day at midnight
    public void updateOverdueStatus() {
        List<BorrowingTransaction> overdueTransactions = borrowingTransactionRepository
                .findByDueDateBeforeAndStatus(LocalDate.now(), TransactionStatus.BORROWED);

        for (BorrowingTransaction transaction : overdueTransactions) {
            transaction.setStatus(TransactionStatus.OVERDUE);
            borrowingTransactionRepository.save(transaction);
        }
    }

    public List<BorrowingTransaction> getTransactionsByStatus(TransactionStatus status) {
        return borrowingTransactionRepository.findByStatus(status);
    }
}
