param (
    [string] $DbUrl = "jdbc:postgresql://ep-floral-field-aqlwoh46.c-8.us-east-1.aws.neon.tech/neondb?sslmode=require",
    [string] $DbUsername = "neondb_owner"
)

$SecurePassword = Read-Host "Neon database password" -AsSecureString
$PasswordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($SecurePassword)

try {
    $env:SPRING_PROFILES_ACTIVE = "neon"
    $env:DB_URL = $DbUrl
    $env:DB_USERNAME = $DbUsername
    $env:DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($PasswordPointer)

    mvn spring-boot:run
}
finally {
    if ($PasswordPointer -ne [IntPtr]::Zero) {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($PasswordPointer)
    }
}
