package com.example.attendance.model;


import com.example.attendance.attendance.Attendance;
import com.example.attendance.attendance.AttendanceRepository;
import com.example.attendance.late.LateRequestDto;
import com.example.attendance.late.LateRequestModel;
import com.example.attendance.late.LateRequestRepository;
import com.example.attendance.late.LateRequestStatus;
import com.example.attendance.leave.LeaveRequestDto;
import com.example.attendance.leave.LeaveRequestModel;
import com.example.attendance.leave.LeaveRequestRepository;
import com.example.attendance.leave.LeaveRequestStatus;
import com.example.attendance.qr.QRCodeService;
import com.example.attendance.qr.ScanResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;


@Service

public class UsersService {
    @Autowired
    private UsersRepository usersRepository;


    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private LateRequestRepository lateRequestRepository;

    @Autowired
    private JavaMailSender mailSender;


    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private QRCodeService qrCodeService;

    @Autowired





    public ResponseEntity<?> userRegistration(UserDto userDto) {
        Optional<UsersModel> existingUser = usersRepository.findByEmail(userDto.getEmail());
        if (existingUser.isPresent()) {
            return new ResponseEntity<>("User already registered", HttpStatus.CONFLICT);
        }

        UsersModel user = new UsersModel();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPassword(userDto.getPassword());
        user.setBatch(userDto.getBatch());
        user.setPhoneNumber(userDto.getPhoneNumber());
        UsersModel savedUser = usersRepository.save(user);

        sendRegistrationEmail(savedUser.getEmail());

        URegistrationResponse responce = new URegistrationResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getBatch(),
                savedUser.getPhoneNumber()
        );

        return new ResponseEntity<>(responce, HttpStatus.CREATED);
    }

    private void sendRegistrationEmail(String toEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Registration Confirmation");
        message.setText("Thank you for registering PTF application!");

        mailSender.send(message);
    }

    public ResponseEntity<?> scanInAndOut(Long userId, InScanDto inScanDto) {
        Optional<UsersModel> usersModelOptional = usersRepository.findById(userId);
        if (usersModelOptional.isPresent()){
            UsersModel usersModel = usersModelOptional.get();
            String batchType = usersModel.getBatch();
            if (inScanDto.getType().equalsIgnoreCase("in")){
                LocalDate currentDate = LocalDate.now();
                if (currentDate.equals(inScanDto.getPresentDate())){
                    if (batchType.equalsIgnoreCase("morning batch")){
                        LocalTime allowedTime = LocalTime.of(9,41);
                        if (allowedTime.isBefore(inScanDto.getPresentTime())){
                            Attendance attendance = new Attendance();
                            attendance.setAttendanceDate(inScanDto.getPresentDate());
                            attendance.setUserId(userId);
                            attendance.setScanInTime(inScanDto.getPresentTime());
                            attendanceRepository.save(attendance);
                            return new ResponseEntity<>(attendance,HttpStatus.OK);
                        }else {
                            Optional<LateRequestModel> lateRequestModelOptional = lateRequestRepository.findByUserIdAndDate(userId,inScanDto.getPresentDate());
                                if (lateRequestModelOptional.isPresent()){
                                    Attendance attendance = new Attendance();
                                    attendance.setScanInTime(inScanDto.getPresentTime());
                                    attendance.setAttendanceDate(inScanDto.getPresentDate());
                                    attendance.setUserId(userId);
                                    attendanceRepository.save(attendance);
                                    return new ResponseEntity<>("Attendance marked",HttpStatus.OK);
                                }
                            }
                        } else if (batchType.equalsIgnoreCase("evening batch")) {
                        LocalTime allowedTime = LocalTime.of(13,41);
                            if (allowedTime.isBefore(inScanDto.getPresentTime())){
                                Attendance attendance = new Attendance();
                                attendance.setScanInTime(inScanDto.getPresentTime());
                                attendance.setAttendanceDate(inScanDto.getPresentDate());
                                attendance.setUserId(userId);
                                attendanceRepository.save(attendance);
                                return new ResponseEntity<>(attendance,HttpStatus.OK);
                            }
                        }else {
                        return new ResponseEntity<>("Batch type is n't valid",HttpStatus.NOT_FOUND);
                    }
                    }else if (inScanDto.getType().equalsIgnoreCase("out")) {
                LocalDate currentDate = LocalDate.now();
                if (currentDate.equals(inScanDto.getPresentDate())){
                    Optional<Attendance> attendanceOptional = attendanceRepository.findByUserId(userId);
                    if (attendanceOptional.isPresent()){
                        Attendance attendance = attendanceOptional.get();
                        attendance.setScanOutTime(inScanDto.getPresentTime());
                        attendanceRepository.save(attendance);
                        return new ResponseEntity<>("Scan Out Time : "+inScanDto.getPresentTime(),HttpStatus.OK);
                    }else {
                        return new ResponseEntity<>("userId is not valid",HttpStatus.NOT_FOUND);
                    }
                }else {
                    return new ResponseEntity<>("Scan out Date is not present",HttpStatus.NOT_FOUND);
                }
            }else {
                return new ResponseEntity<>("Scan Type is not mentioned",HttpStatus.NOT_FOUND);
            }


                }else {
            return new ResponseEntity<>("Invalid UserId ",HttpStatus.NOT_FOUND);}




        return new ResponseEntity<>("Something went wrong",HttpStatus.INTERNAL_SERVER_ERROR);
        }


//    public ResponseEntity<ScanResponseDto> handleScan(Long userId, boolean isInScan) {
//        Optional<UsersModel> user = usersRepository.findById(userId);
//        if (!user.isPresent()) {
//            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
//        }
//
//        LocalDate currentDate = LocalDate.now();
//        LocalTime currentTime = LocalTime.now();
//        String scanType = isInScan ? "inScan" : "outScan";
//
//        // Check if the user already has an attendance record for today
//        Optional<Attendance> existingAttendance = attendanceRepository.findByUserIdAndAttendanceDate(userId, currentDate);
//
//        // If there is an existing attendance record, update it with the scan time
//        if (existingAttendance.isPresent()) {
//            Attendance attendance = existingAttendance.get();
//
//            if (isInScan && attendance.getScanInTime() == null) {
//                attendance.setScanInTime(currentTime);
//                attendance.setStatus("Present"); // User is marked as present
//                attendanceRepository.save(attendance);
//            } else if (!isInScan && attendance.getScanOutTime() == null) {
//                attendance.setScanOutTime(currentTime);
//                attendance.setStatus("out"); // User is marked as absent when scanning out
//                attendanceRepository.save(attendance);
//            } else {
//                // If the user already scanned in or out, prevent duplicate scans
//                return new ResponseEntity<>(new ScanResponseDto(
//                        userId,
//                        scanType,
//                        currentDate.toString(),
//                        currentTime.toString(),
//                        "You have already scanned today." // User already scanned in or out
//                ), HttpStatus.FORBIDDEN);
//            }
//
//            // Prepare response DTO
//            ScanResponseDto scanResponse = new ScanResponseDto(
//                    userId,
//                    scanType,
//                    currentDate.toString(),
//                    currentTime.toString(),
//                    "Scan success"
//            );
//            return new ResponseEntity<>(scanResponse, HttpStatus.OK);
//        }
//
//        // If no attendance record exists, create a new one
//        Attendance attendance = new Attendance();
//        attendance.setUserId(userId);
//        attendance.setAttendanceDate(currentDate);
//
//        if (isInScan) {
//            attendance.setScanInTime(currentTime); // Set scan-in time
//            attendance.setStatus("in");
//        } else {
//            attendance.setScanOutTime(currentTime); // Set scan-out time
//            attendance.setStatus("out");
//        }
//
//        attendanceRepository.save(attendance);
//
//        // Regenerate the QR code after scanning
//        qrCodeService.regenerateQRCode();
//
//        // Prepare response DTO
//        ScanResponseDto scanResponse = new ScanResponseDto(
//                userId,
//                scanType,
//                currentDate.toString(),
//                currentTime.toString(),
//                "Scan success"
//        );
//
//        return new ResponseEntity<>(scanResponse, HttpStatus.OK);
//    }





    public List<Attendance> getAllAttendanceByDate(LocalDate date) {
        // Retrieve all attendance records for the given date
        return attendanceRepository.findByAttendanceDate(date);
    }



    public List<Attendance> getAttendanceForMonth(Long userId, int month, int year) {
        // Create a date range for the given month and year
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

        // Retrieve all attendance records for the user in the given month
        return attendanceRepository.findByUserIdAndAttendanceDateBetween(userId, startOfMonth, endOfMonth);
    }

    public Optional<UsersModel> findUserByToken(String token) {
        // Here you should implement the logic to retrieve the user associated with the token
        return usersRepository.findByToken(token); // You need to implement this in the repository
    }





    // Create leave request with date validation
    public ResponseEntity<?> createLeaveRequest(LeaveRequestDto leaveRequestDto, Long userId) {
        LocalDate currentDate = LocalDate.now();

        // Validate that the fromDate is today or a future date
        if (leaveRequestDto.getFromDate().isBefore(currentDate)) {
            return new ResponseEntity<>("Leave request cannot be made for past dates.", HttpStatus.BAD_REQUEST);
        }

        // Create and populate the leave request
        LeaveRequestModel leaveRequest = new LeaveRequestModel();
        leaveRequest.setUserId(userId);
        leaveRequest.setLeaveType(leaveRequestDto.getLeaveType());
        leaveRequest.setReason(leaveRequestDto.getReason());
        leaveRequest.setFromDate(leaveRequestDto.getFromDate());
        leaveRequest.setToDate(leaveRequestDto.getToDate());
        leaveRequest.setNumberOfDays(leaveRequestDto.getNumberOfDays());

        // Set the default status to PENDING
        leaveRequest.setStatus(LeaveRequestStatus.PENDING);

        // Save the leave request to the repository
        LeaveRequestModel savedRequest = leaveRequestRepository.save(leaveRequest);

        // Return the created leave request
        return new ResponseEntity<>(savedRequest, HttpStatus.CREATED);
    }







    public List<LeaveRequestModel> getLeaveRequestsByUserId(Long userId) {
        return leaveRequestRepository.findByUserId(userId); // Ensure this method is defined in the repository
    }




//    public Optional<UsersModel> findByEmail(String email) {
//        return usersRepository.findByEmail(email); // Ensure this method exists in your repository
//    }


    public Optional<UsersModel> findByEmailAndPassword(String email, String password) {
        return usersRepository.findByEmailAndPassword(email, password);
    }


    public void updateUserToken(UsersModel usersModel) {
        usersRepository.save(usersModel); // Save the updated admin model with the token
    }

    public List<UsersModel> getAllUsers() {
        return usersRepository.findAll();
    }

    public Optional<UsersModel> getUserById(Long id) {
        return usersRepository.findById(id);
    }

    //27/7/24
    public UsersModel updateUserPassword(Long id, UserDto userDto) throws Exception {
        Optional<UsersModel> existingUser = usersRepository.findById(id);
        if (!existingUser.isPresent()) {
            throw new Exception("User Not Found");
        }
        UsersModel user = existingUser.get();
        user.setPassword(userDto.getPassword());
        return usersRepository.save(user);
    }




    public boolean deleteUser(Long id) {
        Optional<UsersModel> existingUser = usersRepository.findById(id);
        if (!existingUser.isPresent()) {
            return false;
        }
        usersRepository.delete(existingUser.get());
        return true;
    }

    public List<LateRequestModel> getLateRequestsByUserId(Long userId) {
        return lateRequestRepository.findByUserId(userId);
    }


    public ResponseEntity<?> createLateRequest(LateRequestDto lateRequestDto) {
        Optional<UsersModel> usersModelOptional = usersRepository.findById(lateRequestDto.getUserId());
        if (usersModelOptional.isPresent()){
            UsersModel usersModel = usersModelOptional.get();
            if (usersModel.getBatch().equalsIgnoreCase("Morning batch")){
                LocalTime lateMaxTime = LocalTime.of(9,40);
                if (LocalTime.now().isBefore(lateMaxTime)){
                    LateRequestModel requestModel = new LateRequestModel();
                    requestModel.setUserId(lateRequestDto.getUserId());
                    requestModel.setDate(LocalDate.now());
                    requestModel.setReason(lateRequestDto.getReason());
                    requestModel.setStatus(LateRequestStatus.PENDING);
                    lateRequestRepository.save(requestModel);
                    return new ResponseEntity<>("Late request marked ",HttpStatus.OK);
                }else {
                    return new ResponseEntity<>("Late request is not allowed for this current time ",HttpStatus.CONFLICT);
                }
            } else if (usersModel.getBatch().equalsIgnoreCase("Evening Batch")) {
                LocalTime lateMaxTime = LocalTime.of(13,40);
                if (LocalTime.now().isBefore(lateMaxTime)){
                    LateRequestModel requestModel = new LateRequestModel();
                    requestModel.setUserId(lateRequestDto.getUserId());
                    requestModel.setDate(LocalDate.now());
                    requestModel.setReason(lateRequestDto.getReason());
                    requestModel.setStatus(LateRequestStatus.PENDING);
                    lateRequestRepository.save(requestModel);
                    return new ResponseEntity<>("Late request marked ",HttpStatus.OK);
                }else {
                    return new ResponseEntity<>("Late request is not allowed for this current time ",HttpStatus.CONFLICT);
                }
            }

        }return new ResponseEntity<>("UserId is not valid",HttpStatus.NOT_FOUND);
    }
}
