package com.example.attendance.admin;


import com.example.attendance.attendance.Attendance;
import com.example.attendance.attendance.AttendanceRepository;
import com.example.attendance.late.LateRequestModel;
import com.example.attendance.late.LateRequestStatus;
import com.example.attendance.leave.LeaveRequestModel;
import com.example.attendance.leave.LeaveRequestRepository;
import com.example.attendance.leave.LeaveRequestStatus;
import com.example.attendance.model.ForgotPasswordDto;
import com.example.attendance.model.UsersModel;
import com.example.attendance.model.UsersService;
import com.example.attendance.qr.QRCodeService;
import com.example.attendance.qr.ScanDto;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping(path = "/AdminReg")
public class AdminRegistrationController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UsersService usersService;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private QRCodeService qrCodeService;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private AdminRepository adminRepository;

    @PostMapping(path = "/reg")
    public ResponseEntity<?> registration(@RequestBody AdminDto adminDto) {
        try {
            return adminService.adminRegistration(adminDto);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @PostMapping(path = "/login")
    public ResponseEntity<?> loginAdmin(@RequestBody ALoginDto aLoginDto) {
        try {
            Optional<AdminModel> adminOpt = adminService.findByEmailAndPassword(aLoginDto.getEmail(), aLoginDto.getPassword());
            if (adminOpt.isPresent()) {
                AdminModel adminModel = adminOpt.get();

                // Generate a token for the admin
                String token = UUID.randomUUID().toString();
                adminModel.setToken(token); // Set the token in the admin model
                adminService.updateAdminToken(adminModel); // Save the token in the database

                ALoginResponseDto responseDto = new ALoginResponseDto(
                        adminModel.getId(),
                        adminModel.getEmail(),
                        adminModel.getName(),
                         adminModel.getToken(),
//                        token, // Include the token in the response
                        "Login Successfully"
                );

                return new ResponseEntity<>(responseDto, HttpStatus.ACCEPTED);
            } else {
                return new ResponseEntity<>("No details found", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PutMapping("/updateAdmin/{id}")
    public ResponseEntity<?> updateAdmin(@PathVariable int id, @RequestBody AdminDto adminDto) {
        try {
            return adminService.updateAdmin(id, adminDto);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error updating admin", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @DeleteMapping(path = "/delete/{id}")
    public ResponseEntity<?> deleteAdmin(@PathVariable Long id) {
        try {
            boolean isDeleted = adminService.deleteAdmin(id);
            if (isDeleted) {
                return new ResponseEntity<>("User deleted successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PutMapping(path = "/updatePassword/{id}")
    public ResponseEntity<?> updateUserPassword(@PathVariable Long id, @RequestBody AdminDto adminDto) {
        try {
            AdminModel updatedAdmin = adminService.updateAdminPassword(id, adminDto);
            return new ResponseEntity<>(updatedAdmin, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(path = "/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordDto forgotPasswordDto) {
        try {
            // Validate request data
            if (!forgotPasswordDto.getNewPassword().equals(forgotPasswordDto.getConfirmPassword())) {
                return new ResponseEntity<>("Passwords do not match", HttpStatus.BAD_REQUEST);
            }

            // Check if user exists
            Optional<AdminModel> adminOptional = adminRepository.findByEmail(forgotPasswordDto.getEmail());
            if (adminOptional.isEmpty()) {
                return new ResponseEntity<>("Admin not found with the provided email", HttpStatus.NOT_FOUND);
            }

            // Update admin password
            AdminModel admin = adminOptional.get();
            admin.setPassword(forgotPasswordDto.getNewPassword()); // Ensure password is securely hashed
            adminRepository.save(admin);

            return new ResponseEntity<>("Password updated successfully", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    // Add method to generate QR code after scanning
//    @GetMapping(path = "/generateQr")
//    public ResponseEntity<?> generateQr(HttpServletResponse response) {
//        try {
//            String qrContent = UUID.randomUUID().toString(); // Generate unique content for each scan
//            generateQrCode(qrContent, response);
//            return new ResponseEntity<>("QR code generated successfully", HttpStatus.OK);
//        } catch (WriterException | IOException e) {
//            e.printStackTrace();
//            return new ResponseEntity<>("Error generating QR code", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    // Method to generate and write QR code to response
//    private void generateQrCode(String content, HttpServletResponse response) throws WriterException, IOException {
//        QRCodeWriter qrCodeWriter = new QRCodeWriter();
//        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 200, 200);
//
//        // Set response headers for the image output
//        response.setContentType("image/png");
//        OutputStream out = response.getOutputStream();
//        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", out);
//        out.flush();
//        out.close();
//    }




//new


//    // Add method to generate QR code with date and time
//    @GetMapping(path = "/generateQr")
//    public ResponseEntity<?> generateQr(HttpServletResponse response) {
//        try {
//            String qrContent = generateQrContent(); // Generate unique content with date and time
//            generateQrCode(qrContent, response);
//            return new ResponseEntity<>("QR code generated successfully", HttpStatus.OK);
//        } catch (WriterException | IOException e) {
//            e.printStackTrace();
//            return new ResponseEntity<>("Error generating QR code", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    // Method to generate content with UUID and date-time
//    private String generateQrContent() {
//        String uuid = UUID.randomUUID().toString();
//        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//        return "UUID: " + uuid + " | Timestamp: " + timestamp;
//    }
//
//    // Method to generate and write QR code to response
//    private void generateQrCode(String content, HttpServletResponse response) throws WriterException, IOException {
//        QRCodeWriter qrCodeWriter = new QRCodeWriter();
//        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 200, 200);
//
//        // Set response headers for the image output
//        response.setContentType("image/png");
//        try (OutputStream out = response.getOutputStream()) {
//            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", out);
//        }
//    }


    // Endpoint to retrieve the current QR code
    @GetMapping(path = "/generateQr")
    public ResponseEntity<?> getQRCode() {
        try {
            // Retrieve the pre-generated QR code
            String currentQRCode = qrCodeService.getCurrentQRCode();
            return new ResponseEntity<>(currentQRCode, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error retrieving QR code", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    // Method to generate and write QR code to response
    private void generateQrCode(String content, HttpServletResponse response) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 200, 200);

        // Set response headers for the image output
        response.setContentType("image/png");
        try (OutputStream out = response.getOutputStream()) {
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", out);
        }
    }

    @PostMapping(path = "/scan")
    public ResponseEntity<?> handleScan(@RequestBody ScanDto scanDto) {
        try {
            // Handle the scan logic here, such as saving to the database
            adminService.handleScan(scanDto.getToken());  // Use the token or user ID to identify the scan

            // Return a response indicating successful scan
            return new ResponseEntity<>("Scan recorded successfully", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error handling scan", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/getAllUsers")
    public ResponseEntity<List<UsersModel>> getAllUsers() {
        try {
            List<UsersModel> users = usersService.getAllUsers();
            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }





    @GetMapping("/get/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            Optional<UsersModel> user = usersService.getUserById(id);
            if (user.isPresent()) {
                return new ResponseEntity<>(user.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }






    // Fetch all leave requests for today
    @GetMapping("/getLeaveRequestsForToday")
    public ResponseEntity<?> getLeaveRequestsForToday() {
        try {
            List<LeaveRequestModel> leaveRequests = adminService.getLeaveRequestsForToday();

            if (leaveRequests.isEmpty()) {
                return new ResponseEntity<>("No leave requests for today", HttpStatus.OK);
            }

            return new ResponseEntity<>(leaveRequests, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Approve leave request
    @PostMapping("/approveLeaveRequest/{leaveRequestId}")
    public ResponseEntity<?> approveLeaveRequest(@PathVariable Long leaveRequestId) {
        try {
            return adminService.approveLeaveRequest(leaveRequestId);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Fetch leave requests based on status (PENDING, APPROVED, REJECTED)
    @GetMapping("/getLeaveRequestsByStatus")
    public ResponseEntity<?> getLeaveRequestsByStatus(@RequestParam LeaveRequestStatus status) {
        try {
            List<LeaveRequestModel> leaveRequests = adminService.getLeaveRequestsByStatus(status);

            if (leaveRequests.isEmpty()) {
                return new ResponseEntity<>("No leave requests found for the given status", HttpStatus.OK);
            }

            return new ResponseEntity<>(leaveRequests, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    // Reject leave request
    @PostMapping("/rejectLeaveRequest/{leaveRequestId}")
    public ResponseEntity<?> rejectLeaveRequest(@PathVariable Long leaveRequestId) {
        try {
            return adminService.rejectLeaveRequest(leaveRequestId);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    // Fetch all late requests for today
    @GetMapping("/getLateRequestsForToday")
    public ResponseEntity<?> getLateRequestsForToday() {
        try {
            List<LateRequestModel> lateRequests = adminService.getLateRequestsForToday();

            if (lateRequests.isEmpty()) {
                return new ResponseEntity<>("No late requests for today", HttpStatus.OK);
            }

            return new ResponseEntity<>(lateRequests, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Fetch late requests based on status (PENDING, APPROVED, REJECTED)
    @GetMapping("/getLateRequestsByStatus")
    public ResponseEntity<?> getLateRequestsByStatus(@RequestParam LateRequestStatus status) {
        try {
            List<LateRequestModel> lateRequests = adminService.getLateRequestsByStatus(status);

            if (lateRequests.isEmpty()) {
                return new ResponseEntity<>("No late requests found for the given status", HttpStatus.OK);
            }

            return new ResponseEntity<>(lateRequests, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Approve late request
    @PostMapping("/approveLateRequest/{lateRequestId}")
    public ResponseEntity<?> approveLateRequest(@PathVariable Long lateRequestId) {
        try {
            return adminService.approveLateRequest(lateRequestId);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Reject late request
    @PostMapping("/rejectLateRequest/{lateRequestId}")
    public ResponseEntity<?> rejectLateRequest(@PathVariable Long lateRequestId) {
        try {
            return adminService.rejectLateRequest(lateRequestId);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    // New method to get attendance for all users on the current date
    @GetMapping("/attendance/today")
    public ResponseEntity<?> getAllUserAttendanceToday() {
        LocalDate currentDate = LocalDate.now();
        List<Attendance> allAttendance = attendanceRepository.findByAttendanceDate(currentDate);

        if (allAttendance.isEmpty()) {
            return new ResponseEntity<>("No attendance records found for today", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(allAttendance, HttpStatus.OK);
    }

    // Get attendance for all users on a particular date
    @GetMapping("/attendance/date/{date}")
    public ResponseEntity<?> getAllUserAttendance(@PathVariable String date) {
        LocalDate attendanceDate = LocalDate.parse(date);
        List<Attendance> allAttendance = attendanceRepository.findByAttendanceDate(attendanceDate);

        if (allAttendance.isEmpty()) {
            return new ResponseEntity<>("No attendance records found for this date", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(allAttendance, HttpStatus.OK);
    }




}
