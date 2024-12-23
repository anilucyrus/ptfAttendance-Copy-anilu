package com.example.attendance.attendance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {


    Optional<Attendance> findByUserIdAndAttendanceDate(Long userId, LocalDate date);

    // Find attendance for all users on a particular date
    List<Attendance> findByAttendanceDate(LocalDate date);

    // New method to find attendance for a user in a particular date range (month)
    List<Attendance> findByUserIdAndAttendanceDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    Optional<Attendance> findByUserId(Long userId);
}
