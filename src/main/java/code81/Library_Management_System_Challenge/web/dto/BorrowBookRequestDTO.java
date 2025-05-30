package code81.Library_Management_System_Challenge.web.dto;

public class BorrowBookRequestDTO {
    private Long bookId;
    private Long memberId;
    private Long issuedByUserId;

    public BorrowBookRequestDTO() {}

    public BorrowBookRequestDTO(Long bookId, Long memberId, Long issuedByUserId) {
        this.bookId = bookId;
        this.memberId = memberId;
        this.issuedByUserId = issuedByUserId;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public Long getIssuedByUserId() {
        return issuedByUserId;
    }

    public void setIssuedByUserId(Long issuedByUserId) {
        this.issuedByUserId = issuedByUserId;
    }
}
