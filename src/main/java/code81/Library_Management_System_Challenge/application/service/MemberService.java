package code81.Library_Management_System_Challenge.application.service;
import code81.Library_Management_System_Challenge.application.exception.ResourceNotFoundException;
import code81.Library_Management_System_Challenge.application.exception.InvalidOperationException;
import code81.Library_Management_System_Challenge.domain.model.Member;
import code81.Library_Management_System_Challenge.domain.model.User;
import code81.Library_Management_System_Challenge.domain.repository.MemberRepository;
import code81.Library_Management_System_Challenge.web.dto.MemberDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    UserActivityService userActivityService ;
    @Autowired
    UserService userService ;

    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    public Optional<Member> getMemberById(Long id) {
        return memberRepository.findById(id);
    }

    public Optional<Member> getMemberByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    public MemberDTO createMember(MemberDTO memberDto) {
        if (memberRepository.existsByEmail(memberDto.getEmail())) {
            throw new InvalidOperationException("Member with email '" + memberDto.getEmail() + "' already exists");
        }

        Member member = new Member();
        member.setFirstName(memberDto.getFirstName());
        member.setLastName(memberDto.getLastName());
        member.setEmail(memberDto.getEmail());
        member.setPhone(memberDto.getPhone());
        member.setAddress(memberDto.getAddress());
        Member saved = memberRepository.save(member);
        // Log activity
        User currentUser = userService.getCurrentUser();
        if (currentUser != null) {
            userActivityService.logActivity(currentUser, "MEMBER_CREATED",
                    String.format("Created new member: %s %s (Email: %s)",
                            saved.getFirstName(), saved.getLastName(), saved.getEmail()));
        }

        // Convert back to DTO
        MemberDTO response = new MemberDTO();
        response.setFirstName(saved.getFirstName());
        response.setLastName(saved.getLastName());
        response.setEmail(saved.getEmail());
        response.setPhone(saved.getPhone());
        response.setAddress(saved.getAddress());

        return response;
    }

    public Member updateMember(Long id, Member memberDetails) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + id));
        String originalName = member.getFullName();
        String originalEmail = member.getEmail();

        member.setFirstName(memberDetails.getFirstName());
        member.setLastName(memberDetails.getLastName());
        member.setEmail(memberDetails.getEmail());

        if (memberDetails.getPhone() != null)
            member.setPhone(memberDetails.getPhone());

        if (memberDetails.getAddress() != null)
            member.setAddress(memberDetails.getAddress());

        if (memberDetails.getExpiryDate() != null)
            member.setExpiryDate(memberDetails.getExpiryDate());

        member.setActive(memberDetails.isActive());

        Member updatedMember = memberRepository.save(member);

        // Log activity
        User currentUser = userService.getCurrentUser();
        if (currentUser != null) {
            userActivityService.logActivity(currentUser, "MEMBER_UPDATED",
                    String.format("Updated member: %s (ID: %d) - Previous: %s (%s)",
                            updatedMember.getFullName(), id, originalName, originalEmail));
        }

        return updatedMember;
    }

    public void deleteMember(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + id));
        String memberName = member.getFullName();
        String memberEmail = member.getEmail();
        memberRepository.delete(member);
        // Log activity
        User currentUser = userService.getCurrentUser();
        if (currentUser != null) {
            userActivityService.logActivity(currentUser, "MEMBER_DELETED",
                    String.format("Deleted member: %s (Email: %s)", memberName, memberEmail));
        }
    }

    public List<Member> getActiveMembers() {
        return memberRepository.findByActiveTrue();
    }

    public List<Member> searchMembers(String name) {
        return memberRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(name, name, name);
    }

    public void renewMembership(Long id, int months) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with ID: " + id));
        LocalDate newExpiryDate = member.getExpiryDate().plusMonths(months);
        member.setExpiryDate(newExpiryDate);
        member.setActive(true);
        memberRepository.save(member);
    }
}
