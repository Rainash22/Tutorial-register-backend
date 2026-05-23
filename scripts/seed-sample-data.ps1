param (
    [string] $BaseUrl = "http://localhost:8080"
)

function New-ApiResource {
    param (
        [string] $Path,
        [hashtable] $Body
    )

    $Json = $Body | ConvertTo-Json -Depth 10
    Invoke-RestMethod -Method Post -Uri "$BaseUrl$Path" -ContentType "application/json" -Body $Json
}

$AdminRole = New-ApiResource "/api/roles" @{
    name = "ADMIN"
    description = "Application administrator"
}

$StaffRole = New-ApiResource "/api/roles" @{
    name = "STAFF"
    description = "Teacher or staff member"
}

$StudentRole = New-ApiResource "/api/roles" @{
    name = "STUDENT"
    description = "Student login user"
}

$StaffUser = New-ApiResource "/api/users" @{
    username = "teacher1"
    email = "teacher1@example.com"
    passwordHash = "temporary-password-hash"
    enabled = $true
    roleIds = @($StaffRole.id)
}

$StudentUser = New-ApiResource "/api/users" @{
    username = "student1"
    email = "student1@example.com"
    passwordHash = "temporary-password-hash"
    enabled = $true
    roleIds = @($StudentRole.id)
}

$Staff = New-ApiResource "/api/staff" @{
    fullName = "Priya Nair"
    email = "teacher1.staff@example.com"
    phone = "9876543210"
    designation = "Maths Teacher"
    gender = "FEMALE"
    joinedDate = "2026-05-21"
    userAccountId = $StaffUser.id
}

$Student = New-ApiResource "/api/students" @{
    fullName = "Anita Sharma"
    admissionNumber = "STU-001"
    dateOfBirth = "2010-04-12"
    gender = "FEMALE"
    className = "Grade 9"
    courseName = "Maths Tuition"
    guardianName = "Ravi Sharma"
    guardianPhone = "9876543210"
    email = "student1.profile@example.com"
    phone = "9876500000"
    address = "Chennai"
    admissionDate = "2026-05-21"
    status = "ACTIVE"
    assignedStaffId = $Staff.id
    userAccountId = $StudentUser.id
}

$Attendance = New-ApiResource "/api/attendance" @{
    studentId = $Student.id
    markedById = $Staff.id
    attendanceDate = "2026-05-21"
    status = "PRESENT"
    remarks = "On time"
}

$Fee = New-ApiResource "/api/fees" @{
    studentId = $Student.id
    amountDue = 5000.00
    amountPaid = 2000.00
    dueDate = "2026-06-10"
    paidDate = "2026-05-21"
    status = "PARTIAL"
    paymentReference = "CASH-001"
    remarks = "First installment paid"
}

$Assessment = New-ApiResource "/api/assessments" @{
    studentId = $Student.id
    evaluatedById = $Staff.id
    title = "Algebra Test 1"
    type = "TEST"
    maxMarks = 100.00
    marksObtained = 86.50
    assessmentDate = "2026-05-21"
    remarks = "Good performance"
}

$StudentNotification = New-ApiResource "/api/notifications" @{
    studentId = $Student.id
    title = "Fee Reminder"
    message = "Your next fee installment is due on 2026-06-10."
    channel = "EMAIL"
    scheduledAt = "2026-06-01T09:00:00"
    status = "QUEUED"
}

$StaffNotification = New-ApiResource "/api/notifications" @{
    staffId = $Staff.id
    title = "Attendance Pending"
    message = "Please mark today's attendance."
    channel = "APP"
    scheduledAt = "2026-05-21T18:00:00"
    status = "QUEUED"
}

@{
    roles = @{
        admin = $AdminRole.id
        staff = $StaffRole.id
        student = $StudentRole.id
    }
    users = @{
        staffUser = $StaffUser.id
        studentUser = $StudentUser.id
    }
    staff = $Staff.id
    student = $Student.id
    attendance = $Attendance.id
    fee = $Fee.id
    assessment = $Assessment.id
    notifications = @{
        studentNotification = $StudentNotification.id
        staffNotification = $StaffNotification.id
    }
} | ConvertTo-Json -Depth 10
