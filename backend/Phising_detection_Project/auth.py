import random
import os
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel, EmailStr
from fastapi_mail import FastMail, MessageSchema, ConnectionConfig

router = APIRouter()

# --- EMAIL CONFIGURATION ---
# Switching to Port 465 (SSL) to avoid "Connection Reset" errors on local networks
conf = ConnectionConfig(
    MAIL_USERNAME = os.getenv("MAIL_USERNAME", "dipakkumargupta893@gmail.com"),
    MAIL_PASSWORD = os.getenv("MAIL_PASSWORD", "eppy lvzc kkyw hjbx"),
    MAIL_FROM = os.getenv("MAIL_FROM", "dipakkumargupta893@gmail.com"),
    MAIL_PORT = int(os.getenv("MAIL_PORT", 465)),
    MAIL_SERVER = os.getenv("MAIL_SERVER", "smtp.gmail.com"),
    MAIL_STARTTLS = False,
    MAIL_SSL_TLS = True,
    USE_CREDENTIALS = True,
    VALIDATE_CERTS = True,
    TIMEOUT = 60
)

# Temporary storage for OTPs
otp_storage = {}

class OtpRequest(BaseModel):
    email: EmailStr

class OtpVerifyRequest(BaseModel):
    email: EmailStr
    otp: str

@router.post("/send-otp")
async def send_otp(request: OtpRequest):
    otp = str(random.randint(100000, 999999))
    otp_storage[request.email] = otp

    message = MessageSchema(
        subject="SafeGuard AI Verification Code",
        recipients=[request.email],
        body=f"""
        <div style="font-family: Arial, sans-serif; padding: 20px; border: 1px solid #eee; border-radius: 10px;">
            <h2 style="color: #3B82F6;">Welcome to SafeGuard AI</h2>
            <p>To complete your registration, please use the verification code below:</p>
            <div style="background: #F3F4F6; padding: 15px; text-align: center; font-size: 24px; font-weight: bold; letter-spacing: 5px; color: #1F2937;">
                {otp}
            </div>
            <p style="color: #6B7280; font-size: 12px; margin-top: 20px;">
                If you did not request this code, please ignore this email.
            </p>
        </div>
        """,
        subtype="html"
    )

    try:
        fm = FastMail(conf)
        await fm.send_message(message)
        return {"success": True, "message": "OTP sent successfully"}
    except Exception as e:
        print(f"Mail Error: {str(e)}")
        # We return success True if it's 000000 master code enabled,
        # but let's just log the error and allow the 000000 code to work.
        return {"success": True, "message": "Check your email (or use demo code 000000)"}

@router.post("/verify-otp")
async def verify_otp(request: OtpVerifyRequest):
    # Master code for demo/presentation purposes or check stored OTP
    if request.otp == "000000" or (request.email in otp_storage and otp_storage[request.email] == request.otp):
        if request.email in otp_storage:
            del otp_storage[request.email]
        return {"success": True, "message": "OTP verified successfully"}
    else:
        raise HTTPException(status_code=400, detail="Invalid OTP")
