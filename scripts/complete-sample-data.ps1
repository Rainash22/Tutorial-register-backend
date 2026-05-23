$BaseUrl = "http://localhost:8080"

function Send-ApiJson {
    param (
        [string] $Method,
        [string] $Path,
        [hashtable] $Body
    )

    $Json = $Body | ConvertTo-Json -Depth 10
    Invoke-RestMethod -Method $Method -Uri "$BaseUrl$Path" -ContentType "application/json" -Body $Json
}

$Student = Send-ApiJson "Put" "/api/students/1" @{
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
    assignedStaffId = 1
    userAccountId = 2
}

$Attendance = Send-ApiJson "Post" "/api/attendance" @{
    studentId = $Student.id
    markedById = 1
    attendanceDate = "2026-05-21"
    status = "PRESENT"
    remarks = "On time"
}

$Fee = Send-ApiJson "Post" "/api/fees" @{
    studentId = $Student.id
    amountDue = 5000.00
    amountPaid = 2000.00
    dueDate = "2026-06-10"
    paidDate = "2026-05-21"
    status = "PARTIAL"
    paymentReference = "CASH-001"
    remarks = "First installment paid"
}

$Assessment = Send-ApiJson "Post" "/api/assessments" @{
    studentId = $Student.id
    evaluatedById = 1
    title = "Algebra Test 1"
    type = "TEST"
    maxMarks = 100.00
    marksObtained = 86.50
    assessmentDate = "2026-05-21"
    remarks = "Good performance"
}

$StudentNotification = Send-ApiJson "Post" "/api/notifications" @{
    studentId = $Student.id
    title = "Fee Reminder"
    message = "Your next fee installment is due on 2026-06-10."
    channel = "EMAIL"
    scheduledAt = "2026-06-01T09:00:00"
    status = "QUEUED"
}

@{
    student = $Student.id
    attendance = $Attendance.id
    fee = $Fee.id
    assessment = $Assessment.id
    studentNotification = $StudentNotification.id
} | ConvertTo-Json -Depth 10
