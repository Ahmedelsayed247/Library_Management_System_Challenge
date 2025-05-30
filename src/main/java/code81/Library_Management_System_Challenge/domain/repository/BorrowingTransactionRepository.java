package code81.Library_Management_System_Challenge.domain.repository;
import code81.Library_Management_System_Challenge.domain.model.BorrowingTransaction;
import code81.Library_Management_System_Challenge.domain.model.Member;
import code81.Library_Management_System_Challenge.domain.model.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BorrowingTransactionRepository extends JpaRepository<BorrowingTransaction, Long> {
    List<BorrowingTransaction> findByMember(Member member);
    List<BorrowingTransaction> findByStatus(TransactionStatus status);
    List<BorrowingTransaction> findByDueDateBeforeAndStatus(LocalDate date, TransactionStatus status);

    @Query("SELECT bt FROM BorrowingTransaction bt WHERE bt.member.id = :memberId AND bt.status = :status")
    List<BorrowingTransaction> findByMemberIdAndStatus(@Param("memberId") Long memberId, @Param("status") TransactionStatus status);

    @Query("SELECT COUNT(bt) FROM BorrowingTransaction bt WHERE bt.member = :member AND bt.status = 'BORROWED'")
    Long countActiveBorrowingsByMember(@Param("member") Member member);
}
