package edu.dosw.application.ports;


import edu.dosw.application.dto.UserCredentialsResponseDTO;
import edu.dosw.application.dto.UserInfoDTO;
import edu.dosw.domain.model.ValueObject.Email;

public interface UserInfoUseCase {
    UserCredentialsResponseDTO getUserCredentials(Email email);
    UserInfoDTO getCurrentUserInfo(String email);
    boolean canAccessUserData(String currentUserEmail, String targetUserId);
    boolean canAccessStudentData(String currentUserEmail, String studentId);
}