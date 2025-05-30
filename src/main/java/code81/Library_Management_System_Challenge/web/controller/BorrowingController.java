package code81.Library_Management_System_Challenge.web.controller;

import code81.Library_Management_System_Challenge.application.service.BorrowingService;
import code81.Library_Management_System_Challenge.domain.model.BorrowingTransaction;
import code81.Library_Management_System_Challenge.domain.model.TransactionStatus;
import code81.Library_Management_System_Challenge.web.dto.BorrowBookRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class BorrowingController {

    @Autowired
    private BorrowingService borrowingService;

    @GetMapping
    public ResponseEntity<List<BorrowingTransaction>> getAllTransactions() {
        List<BorrowingTransaction> transactions = borrowingService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BorrowingTransaction> getTransactionById(@PathVariable Long id) {
        return borrowingService.getTransactionById(id)
                .map(transaction -> ResponseEntity.ok(transaction))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<BorrowingTransaction>> getTransactionsByMember(@PathVariable Long memberId) {
        try {
            List<BorrowingTransaction> transactions = borrowingService.getTransactionsByMember(memberId);
            return ResponseEntity.ok(transactions);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<BorrowingTransaction>> getOverdueTransactions() {
        List<BorrowingTransaction> transactions = borrowingService.getOverdueTransactions();
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<BorrowingTransaction>> getTransactionsByStatus(@PathVariable TransactionStatus status) {
        List<BorrowingTransaction> transactions = borrowingService.getTransactionsByStatus(status);
        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/borrow")
    public ResponseEntity<BorrowingTransaction> borrowBook(
            @RequestBody BorrowBookRequestDTO requestDTO) {
        try {
            BorrowingTransaction transaction = borrowingService.borrowBook(requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }


    @PutMapping("/{id}/return")
    public ResponseEntity<BorrowingTransaction> returnBook(@PathVariable Long id, @RequestParam Long userId) {
        try {
            BorrowingTransaction transaction = borrowingService.returnBook(id, userId);
            return ResponseEntity.ok(transaction);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/renew")
    public ResponseEntity<BorrowingTransaction> renewBook(@PathVariable Long id, @RequestParam(defaultValue = "14") int days) {
        try {
            BorrowingTransaction transaction = borrowingService.renewBook(id, days);
            return ResponseEntity.ok(transaction);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // This end-point run automatically at midnight
    @PostMapping("/update-overdue")
    @Scheduled(cron = "0 0 0 * * *")
    public ResponseEntity<Void> updateOverdueStatus() {
        borrowingService.updateOverdueStatus();
        return ResponseEntity.ok().build();
    }
}
