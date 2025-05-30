package code81.Library_Management_System_Challenge.application.service;

import code81.Library_Management_System_Challenge.domain.model.User;
import code81.Library_Management_System_Challenge.domain.model.UserActivity;
import code81.Library_Management_System_Challenge.domain.repository.UserActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserActivityService {

    @Autowired
    private UserActivityRepository userActivityRepository;

    public void logActivity(User user, String action, String description) {
        UserActivity activity = new UserActivity(user, action, description);
        userActivityRepository.save(activity);
    }

    public List<UserActivity> getUserActivities(Long userId) {
        return userActivityRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<UserActivity> getAllActivities() {
        return userActivityRepository.findAllByOrderByCreatedAtDesc();
    }
}
