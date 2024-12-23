package com.example.attendance.admin;




import com.example.attendance.attendance.Attendance;
import com.example.attendance.attendance.AttendanceRepository;
import com.example.attendance.late.LateRequestModel;
import com.example.attendance.late.LateRequestRepository;
import com.example.attendance.late.LateRequestStatus;
import com.example.attendance.leave.LeaveRequestModel;
import com.example.attendance.leave.LeaveRequestRepository;
import com.example.attendance.leave.LeaveRequestStatus;
import com.example.attendance.model.ForgotPasswordDto;
import com.example.attendance.model.UsersModel;
import com.example.attendance.model.UsersRepository;
import com.example.attendance.qr.QRCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private LateRequestRepository lateRequestRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    private QRCodeService qrCodeService;

    @Autowired



    public ResponseEntity<?> adminRegistration(AdminDto adminDto) {
        Optional<AdminModel> existingAdmin = adminRepository.findByEmail(adminDto.getEmail()); // Use the instance variable
        if (existingAdmin.isPresent()) {
            return new ResponseEntity<>("User already registered", HttpStatus.CONFLICT);
        }

        AdminModel admin = new AdminModel();
        admin.setName(adminDto.getName());
        admin.setEmail(adminDto.getEmail());
        admin.setPassword(adminDto.getPassword());

        AdminModel savedAdmin = adminRepository.save(admin); // Use the instance variable

        ARegistrationResponce responce = new ARegistrationResponce(
                savedAdmin.getId(),
                savedAdmin.getName(),
                savedAdmin.getEmail()
        );

        return new ResponseEntity<>(responce, HttpStatus.CREATED);
    }


    public void handleScan(String userId) {
        // Logic for handling the scan
        String newToken = UUID.randomUUID().toString();  // Regenerate the token for each scan
        System.out.println("New Token Generated: " + newToken);

        // Regenerate QR Code after scan
        qrCodeService.regenerateQRCode();  // This will regenerate QR code every time a scan happens
    }




    // Fetch leave requests for today
    public List<LeaveRequestModel> getLeaveRequestsForToday() {
        LocalDate currentDate = LocalDate.now();
        return leaveRequestRepository.findByFromDate(currentDate);
    }

    // Fetch leave requests by status (PENDING, APPROVED, REJECTED)
    public List<LeaveRequestModel> getLeaveRequestsByStatus(LeaveRequestStatus status) {
        return leaveRequestRepository.findByStatus(status);
    }


    // Approve Leave Request and Handle Attendance
    public ResponseEntity<?> approveLeaveRequest(Long leaveRequestId) {
        // Fetch the leave request by ID
        Optional<LeaveRequestModel> leaveRequestOptional = leaveRequestRepository.findById(leaveRequestId);

        if (leaveRequestOptional.isPresent()) {
            LeaveRequestModel leaveRequest = leaveRequestOptional.get();

            // Check if leave request is still pending
            if (leaveRequest.getStatus() == LeaveRequestStatus.PENDING) {
                // Set status to approved
                leaveRequest.setStatus(LeaveRequestStatus.APPROVED);
                leaveRequestRepository.save(leaveRequest);

                // Handle Attendance for the leave period
//                handleAttendanceForLeave(leaveRequest);

                // Send email confirmation to the user
                Optional<UsersModel> userOptional = usersRepository.findById(leaveRequest.getUserId());
                if (userOptional.isPresent()) {
                    UsersModel user = userOptional.get();
                    sendLeaveRequestApprovalEmail(user.getEmail(), leaveRequest);
                }

                return new ResponseEntity<>("Leave request approved", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Leave request is already processed", HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>("Leave request not found for ID: " + leaveRequestId, HttpStatus.NOT_FOUND);
        }
    }

    // Method to handle attendance for the leave period
    private void handleAttendanceForLeave(LeaveRequestModel leaveRequest) {
        LocalDate startDate = leaveRequest.getFromDate();
        LocalDate endDate = leaveRequest.getToDate();
        Long userId = leaveRequest.getUserId();

        // Iterate over each day of the leave period and mark attendance as "Leave"
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            Optional<Attendance> existingAttendance = attendanceRepository.findByUserIdAndAttendanceDate(userId, date);

            // Only create a new attendance if there is no existing record
            if (!existingAttendance.isPresent()) {
                Attendance attendance = new Attendance();
                attendance.setUserId(userId);
                attendance.setAttendanceDate(date);
                attendance.setStatus("Present"); // Set the status as "Leave"
                attendanceRepository.save(attendance);
            }
        }
    }

    // Method to send email
    private void sendLeaveRequestApprovalEmail(String userEmail, LeaveRequestModel leaveRequest) {
        String subject = "Leave Request Approved";
        String text = String.format(
                "Dear User,\n\n" +
                        "Good news! Your leave request for %s has been approved.\n" +
                        "The leave period is from %s to %s. \n" +
                        "You are granted a total of %d days of leave.\n\n" +
                        "Thank you.\n\n" +
                        "PTF Team.",
                leaveRequest.getLeaveType(),
                leaveRequest.getFromDate(),
                leaveRequest.getToDate(),
                leaveRequest.getNumberOfDays()
        );

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(userEmail);
        message.setSubject(subject);
        message.setText(text);

        try {
            mailSender.send(message);
        } catch (MailException e) {
            e.printStackTrace();
            // Handle exception (logging, etc.)
        }
    }



    // Reject leave request
    public ResponseEntity<?> rejectLeaveRequest(Long leaveRequestId) {
        Optional<LeaveRequestModel> leaveRequestOptional = leaveRequestRepository.findById(leaveRequestId);

        if (leaveRequestOptional.isPresent()) {
            LeaveRequestModel leaveRequest = leaveRequestOptional.get();

            if (leaveRequest.getStatus() == LeaveRequestStatus.PENDING) {
                leaveRequest.setStatus(LeaveRequestStatus.REJECTED);
                leaveRequestRepository.save(leaveRequest);
                return new ResponseEntity<>("Leave request rejected", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Leave request is already processed", HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>("Leave request not found", HttpStatus.NOT_FOUND);
        }
    }








    public List<LateRequestModel> getLateRequestsForToday() {
        LocalDate currentDate = LocalDate.now();
        return lateRequestRepository.Date(currentDate);
    }


    public List<LateRequestModel> getLateRequestsByStatus(LateRequestStatus status) {
        return lateRequestRepository.findByStatus(status);
    }

    public ResponseEntity<?> approveLateRequest(Long lateRequestId) {
        Optional<LateRequestModel> lateRequestOptional = lateRequestRepository.findById(lateRequestId);

        if (lateRequestOptional.isPresent()) {
            LateRequestModel lateRequest = lateRequestOptional.get();

            if (lateRequest.getStatus() == LateRequestStatus.PENDING) {
                lateRequest.setStatus(LateRequestStatus.APPROVED);
                lateRequestRepository.save(lateRequest);
                return new ResponseEntity<>("Late request approved", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Late request is already processed", HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>("Late request not found", HttpStatus.NOT_FOUND);
        }
    }


    public ResponseEntity<?> rejectLateRequest(Long lateRequestId) {
        Optional<LateRequestModel> lateRequestOptional = lateRequestRepository.findById(lateRequestId);

        if (lateRequestOptional.isPresent()) {
            LateRequestModel lateRequest = lateRequestOptional.get();

            if (lateRequest.getStatus() == LateRequestStatus.PENDING) {
                lateRequest.setStatus(LateRequestStatus.REJECTED);
                lateRequestRepository.save(lateRequest);
                return new ResponseEntity<>("Late request rejected", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Late request is already processed", HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>("Late request not found", HttpStatus.NOT_FOUND);
        }
    }



//
//
//    public List<LateRequestModel> getLateRequestsForToday() {
//        // Get the current date formatted as "YYYY-MM-DD"
//        String currentDate = LocalDate.now().toString();
//        List<LateRequestModel> lateRequests = lateRequestRepository.findAllLateRequestsForDate(currentDate);
//
//        if (lateRequests.isEmpty()) {
//            System.out.println("No late requests for today's date: " + currentDate);
//        } else {
//            System.out.println("Found " + lateRequests.size() + " late requests for today.");
//        }
//
//        return lateRequests;
//    }



    public Optional<AdminModel> findByEmailAndPassword(String email, String password) {
        return adminRepository.findByEmailAndPassword(email, password);
    }

    public void updateAdminToken(AdminModel adminModel) {
        adminRepository.save(adminModel); // Save the updated admin model with the token
    }

    public ResponseEntity<?> updateAdmin(int id, AdminDto adminDto) {
        Optional<AdminModel> existingAdmin = adminRepository.findById(id);
        if (existingAdmin.isPresent()) {
            AdminModel adminToUpdate = existingAdmin.get();
            adminToUpdate.setName(adminDto.getName());
            adminToUpdate.setEmail(adminDto.getEmail());
            adminToUpdate.setPassword(adminDto.getPassword());

            adminRepository.save(adminToUpdate);
            return new ResponseEntity<>(adminToUpdate, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Admin not found", HttpStatus.NOT_FOUND);
        }
    }




    public boolean deleteAdmin(Long id) {
        Optional<AdminModel> existingAdmin = adminRepository.findById(id);
        if (!existingAdmin.isPresent()) {
            return false;
        }
        adminRepository.delete(existingAdmin.get());
        return true;
    }


    public AdminModel updateAdminPassword(Long id, AdminDto adminDto) throws Exception {
        Optional<AdminModel> existingAdmin = adminRepository.findById(id);
        if (!existingAdmin.isPresent()) {
            throw new Exception("Admin Not Found");
        }
        AdminModel admin = existingAdmin.get();
        admin.setPassword(adminDto.getPassword());
        return adminRepository.save(admin);
    }

    public ResponseEntity<?> forgotPassword(ForgotPasswordDto forgotPasswordDto) {
        Optional<AdminModel> adminOptional = adminRepository.findByEmail(forgotPasswordDto.getEmail());
        if (adminOptional.isPresent()) {
            String temporaryPassword = UUID.randomUUID().toString().substring(0, 8);
            AdminModel admin = adminOptional.get();
            admin.setPassword(temporaryPassword);
            adminRepository.save(admin);
//            public ResponseEntity<?> forgotPassword(ForgotPasswordDto forgotPasswordDto) {
//                Optional<UsersModel> userOptional = usersRepository.findByEmail(forgotPasswordDto.getEmail());
//        if (userOptional.isPresent()) {
//            String temporaryPassword = UUID.randomUUID().toString().substring(0, 8);
//            UsersModel user = userOptional.get();
//            user.setPassword(temporaryPassword);
//            usersRepository.save(user);


            sendForgotPasswordEmail(admin.getEmail(), temporaryPassword);

            return new ResponseEntity<>("Temporary password sent to your email", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Email not found", HttpStatus.NOT_FOUND);
        }
    }


    private void sendForgotPasswordEmail(String toEmail, String temporaryPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Forgot Password Assistance");
        message.setText("Your temporary password is: " + temporaryPassword + ". Please use it to log in and reset your password.");

        mailSender.send(message);
    }



}

