import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import useWindowWidth from '../hooks/useWindowWidth'
import Navbar from '../components/Navbar'

export default function ForgotPasswordPage() {
  const [step, setStep] = useState('email')
  const [email, setEmail] = useState('')
  const [otp, setOtp] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const navigate = useNavigate()
  const width = useWindowWidth()
  const isMobile = width < 768

  const sendOtp = async (e) => {
    e.preventDefault()
    setError('')
    setSuccess('')
    setLoading(true)

    try {
      const res = await fetch('http://localhost:8080/api/auth/forgot-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email })
      })
      const data = await res.json()

      if (!res.ok) {
        if (res.status === 404) {
          setError('No account found with this email')
        } else {
          setError(data.message || 'Unable to send OTP. Please try again.')
        }
      } else {
        setStep('otp')
      }
    } catch (err) {
      setError('Unable to send OTP. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  const resendOtp = async () => {
    setError('')
    setSuccess('')
    setLoading(true)

    try {
      const res = await fetch('http://localhost:8080/api/auth/forgot-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email })
      })
      const data = await res.json()

      if (!res.ok) {
        if (res.status === 404) {
          setError('No account found with this email')
        } else {
          setError(data.message || 'Unable to resend OTP. Please try again.')
        }
      } else {
        setSuccess('OTP resent. Check your email.')
      }
    } catch (err) {
      setError('Unable to resend OTP. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  const verifyOtp = (e) => {
    e.preventDefault()
    setError('')
    setSuccess('')
    setStep('reset')
  }

  const resetPassword = async (e) => {
    e.preventDefault()
    setError('')
    setSuccess('')

    if (newPassword !== confirmPassword) {
      setError('Passwords do not match')
      return
    }

    setLoading(true)

    try {
      const res = await fetch('http://localhost:8080/api/auth/reset-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, otp, newPassword })
      })
      const data = await res.json()

      if (!res.ok) {
        setError(data.message || 'Unable to reset password. Please try again.')
      } else {
        setSuccess('Password reset! Redirecting to login...')
        setTimeout(() => navigate('/login'), 2000)
      }
    } catch (err) {
      setError('Unable to reset password. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{ minHeight: '100vh', background: '#f9f9f9' }}>
      <Navbar />
      <div style={{ maxWidth: 440, width: '100%', margin: '0 auto', padding: isMobile ? '80px 16px 60px' : '100px 24px 80px' }}>
        <div style={{ background: 'white', borderRadius: 20, border: '1px solid #eee', padding: isMobile ? 24 : 40 }}>
          {step === 'email' && (
            <>
              <h1 style={{ fontSize: isMobile ? 22 : 26, fontWeight: 700, color: '#111', marginBottom: 4, textAlign: isMobile ? 'center' : 'left' }}>Reset your password</h1>
              <p style={{ color: '#666', fontSize: 14, marginBottom: isMobile ? 24 : 32, textAlign: isMobile ? 'center' : 'left' }}>Enter your email and we'll send you an OTP</p>

              {error && (
                <div style={{ background: '#FFF1F2', border: '1px solid #FDA4AF', borderRadius: 10, padding: '12px 16px', marginBottom: 16 }}>
                  <p style={{ color: '#E11D48', fontSize: 13, fontWeight: 500, margin: 0 }}>⚠️ {error}</p>
                </div>
              )}

              <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
                <div>
                  <label style={{ display: 'block', fontSize: 13, fontWeight: 500, color: '#333', marginBottom: 6 }}>Email</label>
                  <input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="you@example.com"
                    style={{ width: '100%', height: 48, border: '1px solid #eee', borderRadius: 10, padding: '12px 16px', fontSize: 14, outline: 'none', boxSizing: 'border-box' }}
                  />
                </div>
                <button
                  onClick={sendOtp}
                  disabled={loading}
                  style={{ width: '100%', background: '#185FA5', color: 'white', border: 'none', padding: '14px', borderRadius: 12, fontSize: 14, fontWeight: 600, cursor: loading ? 'not-allowed' : 'pointer', opacity: loading ? 0.7 : 1 }}>
                  {loading ? 'Sending OTP...' : 'Send OTP'}
                </button>
              </div>
            </>
          )}

          {step === 'otp' && (
            <>
              <h1 style={{ fontSize: isMobile ? 22 : 26, fontWeight: 700, color: '#111', marginBottom: 4, textAlign: isMobile ? 'center' : 'left' }}>Check your email</h1>
              <p style={{ color: '#666', fontSize: 14, marginBottom: isMobile ? 24 : 32, textAlign: isMobile ? 'center' : 'left' }}>We sent a 6-digit OTP to {email}</p>

              {error && (
                <div style={{ background: '#FFF1F2', border: '1px solid #FDA4AF', borderRadius: 10, padding: '12px 16px', marginBottom: 16 }}>
                  <p style={{ color: '#E11D48', fontSize: 13, fontWeight: 500, margin: 0 }}>⚠️ {error}</p>
                </div>
              )}
              {success && (
                <div style={{ background: '#f0f9f4', border: '1px solid #6ee7b7', borderRadius: 10, padding: '12px 16px', marginBottom: 16 }}>
                  <p style={{ color: '#065f46', fontSize: 13, fontWeight: 500, margin: 0 }}>✓ {success}</p>
                </div>
              )}

              <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
                <div>
                  <label style={{ display: 'block', fontSize: 13, fontWeight: 500, color: '#333', marginBottom: 6 }}>OTP</label>
                  <input
                    type="number"
                    value={otp}
                    inputMode="numeric"
                    onChange={(e) => {
                      const value = e.target.value.slice(0, 6)
                      setOtp(value)
                    }}
                    placeholder="Enter OTP"
                    style={{ width: '100%', height: 48, border: '1px solid #eee', borderRadius: 10, padding: '12px 16px', fontSize: 14, outline: 'none', boxSizing: 'border-box' }}
                  />
                </div>
                <button
                  onClick={verifyOtp}
                  style={{ width: '100%', background: '#185FA5', color: 'white', border: 'none', padding: '14px', borderRadius: 12, fontSize: 14, fontWeight: 600, cursor: 'pointer' }}>
                  Verify OTP
                </button>
                <p style={{ textAlign: 'right', marginTop: 4 }}>
                  <button
                    onClick={resendOtp}
                    disabled={loading}
                    style={{ background: 'none', border: 'none', color: '#185FA5', fontSize: 13, cursor: loading ? 'not-allowed' : 'pointer', padding: 0, textDecoration: 'underline' }}>
                    {loading ? 'Resending...' : "Didn't receive it? Resend OTP"}
                  </button>
                </p>
              </div>
            </>
          )}

          {step === 'reset' && (
            <>
              <h1 style={{ fontSize: isMobile ? 22 : 26, fontWeight: 700, color: '#111', marginBottom: 4, textAlign: isMobile ? 'center' : 'left' }}>Create new password</h1>
              <p style={{ color: '#666', fontSize: 14, marginBottom: isMobile ? 24 : 32, textAlign: isMobile ? 'center' : 'left' }}>Choose a strong password</p>

              {error && (
                <div style={{ background: '#FFF1F2', border: '1px solid #FDA4AF', borderRadius: 10, padding: '12px 16px', marginBottom: 16 }}>
                  <p style={{ color: '#E11D48', fontSize: 13, fontWeight: 500, margin: 0 }}>⚠️ {error}</p>
                </div>
              )}
              {success && (
                <div style={{ background: '#f0f9f4', border: '1px solid #6ee7b7', borderRadius: 10, padding: '12px 16px', marginBottom: 16 }}>
                  <p style={{ color: '#065f46', fontSize: 13, fontWeight: 500, margin: 0 }}>✓ {success}</p>
                </div>
              )}

              <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
                <div>
                  <label style={{ display: 'block', fontSize: 13, fontWeight: 500, color: '#333', marginBottom: 6 }}>New password</label>
                  <input
                    type="password"
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    placeholder="New password"
                    style={{ width: '100%', height: 48, border: '1px solid #eee', borderRadius: 10, padding: '12px 16px', fontSize: 14, outline: 'none', boxSizing: 'border-box' }}
                  />
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: 13, fontWeight: 500, color: '#333', marginBottom: 6 }}>Confirm password</label>
                  <input
                    type="password"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    placeholder="Confirm password"
                    style={{ width: '100%', height: 48, border: '1px solid #eee', borderRadius: 10, padding: '12px 16px', fontSize: 14, outline: 'none', boxSizing: 'border-box' }}
                  />
                </div>
                <button
                  onClick={resetPassword}
                  disabled={loading}
                  style={{ width: '100%', background: '#185FA5', color: 'white', border: 'none', padding: '14px', borderRadius: 12, fontSize: 14, fontWeight: 600, cursor: loading ? 'not-allowed' : 'pointer', opacity: loading ? 0.7 : 1 }}>
                  {loading ? 'Resetting password...' : 'Reset Password'}
                </button>
              </div>
            </>
          )}

          <p style={{ textAlign: 'center', fontSize: 13, color: '#666', marginTop: 24 }}>
            Remembered your password? <Link to="/login" style={{ color: '#185FA5', fontWeight: 500, textDecoration: 'none' }}>Log in</Link>
          </p>
        </div>
      </div>
    </div>
  )
}
