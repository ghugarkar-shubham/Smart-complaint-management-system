import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { register, sendOtp, verifyOtp, resendOtp } from '../services/authService';
import MessageBanner from '../components/MessageBanner';

/**
 * User Registration Page with Email OTP Verification
 * 
 * Registration Flow:
 * 1. User enters email and clicks "Send OTP"
 * 2. OTP is sent to the user's email
 * 3. User enters the OTP they received
 * 4. After verification, user enters fullName and password
 * 5. User submits the complete registration form
 */
export default function RegisterPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ fullName: '', email: '', password: '' });
  const [otp, setOtp] = useState('');
  const [otpSent, setOtpSent] = useState(false);
  const [otpVerified, setOtpVerified] = useState(false);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [type, setType] = useState('success');
  const [showPassword, setShowPassword] = useState(false);

  /**
   * Reset OTP-related state when email changes
   */
  const resetOtpState = () => {
    setOtp('');
    setOtpSent(false);
    setOtpVerified(false);
  };

  /**
   * Send OTP to user's email
   */
  const sendOtpCode = async () => {
    if (!form.email.trim()) {
      setType('error');
      setMessage('Email is required');
      return;
    }

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())) {
      setType('error');
      setMessage('Please enter a valid email address');
      return;
    }

    setLoading(true);
    setMessage('');
    try {
      await sendOtp({ email: form.email.trim() });
      setType('success');
      setMessage('OTP sent to your email. Check your inbox.');
      setOtpSent(true);
    } catch (error) {
      setType('error');
      setMessage(error.message);
      setOtpSent(false);
      resetOtpState();
    } finally {
      setLoading(false);
    }
  };

  /**
   * Verify OTP entered by user
   */
  const verifyOtpCode = async () => {
    if (!otp.trim()) {
      setType('error');
      setMessage('Please enter the OTP from your email');
      return;
    }

    if (!/^\d{6}$/.test(otp.trim())) {
      setType('error');
      setMessage('OTP must be 6 digits');
      return;
    }

    setLoading(true);
    setMessage('');
    try {
      await verifyOtp({ email: form.email.trim(), otp: otp.trim() });
      setType('success');
      setMessage('Email verified successfully.');
      setOtpVerified(true);
    } catch (error) {
      setType('error');
      setMessage(error.message);
      setOtpVerified(false);
    } finally {
      setLoading(false);
    }
  };

  /**
   * Resend OTP if user didn't receive it
   */
  const resendOtpCode = async () => {
    setLoading(true);
    setMessage('');
    try {
      await resendOtp({ email: form.email.trim() });
      setType('success');
      setMessage('OTP resent to your email.');
      setOtp('');
    } catch (error) {
      setType('error');
      setMessage(error.message);
    } finally {
      setLoading(false);
    }
  };

  /**
   * Submit registration form after email verification
   */
  const submit = async (event) => {
    event.preventDefault();

    // Validation
    if (!form.fullName.trim()) {
      setType('error');
      setMessage('Full name is required');
      return;
    }

    if (form.password.length < 6) {
      setType('error');
      setMessage('Password must be at least 6 characters long');
      return;
    }

    if (!otpVerified) {
      setType('error');
      setMessage('Please verify your email with OTP before registering');
      return;
    }

    setLoading(true);
    setMessage('');
    try {
      await register({
        fullName: form.fullName.trim(),
        email: form.email.trim(),
        password: form.password,
      });
      setType('success');
      setMessage('Registration successful. Redirecting to login...');
      setTimeout(() => navigate('/login'), 1200);
    } catch (error) {
      setType('error');
      setMessage(error.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="auth-layout">
      <div className="auth-panel hero-panel accent">
        <p className="eyebrow">Create account</p>
        <h1>Register once and start tracking complaints instantly.</h1>
        <p className="muted">A streamlined workflow designed for responsive support operations and transparent resolution tracking.</p>
      </div>
      <form className="auth-panel card auth-form" onSubmit={submit}>
        <p className="eyebrow">Join now</p>
        <h2>Create your account</h2>
        <MessageBanner type={type} message={message} />

        {/* Email Field - Always visible */}
        <label>
          Email
          <div className="password-field">
            <input
              type="email"
              value={form.email}
              onChange={(event) => {
                setForm({ ...form, email: event.target.value });
                if (otpSent || otpVerified) {
                  resetOtpState();
                }
              }}
              placeholder="your@email.com"
              disabled={otpVerified}
              required
            />
            {!otpVerified && (
              <button
                type="button"
                className="password-toggle"
                onClick={sendOtpCode}
                disabled={loading || !form.email.trim()}
              >
                {otpSent ? 'Resend' : 'Send OTP'}
              </button>
            )}
            {otpVerified && (
              <span style={{ color: '#4caf50', fontSize: '0.9em' }}>✓ Verified</span>
            )}
          </div>
        </label>

        {/* OTP Field - Visible after OTP is sent */}
        {otpSent && !otpVerified && (
          <label>
            Enter OTP
            <div className="password-field">
              <input
                type="text"
                inputMode="numeric"
                maxLength="6"
                value={otp}
                onChange={(event) => setOtp(event.target.value)}
                placeholder="6-digit OTP"
                required
              />
              <button
                type="button"
                className="password-toggle"
                onClick={verifyOtpCode}
                disabled={loading || !otp.trim()}
              >
                Verify
              </button>
            </div>
            <p style={{ fontSize: '0.85em', color: '#666', marginTop: '0.5rem' }}>
              Didn't receive? <button
                type="button"
                style={{ background: 'none', border: 'none', color: '#007bff', cursor: 'pointer', textDecoration: 'underline' }}
                onClick={resendOtpCode}
                disabled={loading}
              >
                Resend OTP
              </button>
            </p>
          </label>
        )}

        {/* Full Name & Password - Visible after email verification */}
        {otpVerified && (
          <>
            <label>
              Full Name
              <input
                value={form.fullName}
                onChange={(event) => setForm({ ...form, fullName: event.target.value })}
                placeholder="John Doe"
                required
              />
            </label>

            <label>
              Password
              <div className="password-field">
                <input
                  type={showPassword ? 'text' : 'password'}
                  value={form.password}
                  onChange={(event) => setForm({ ...form, password: event.target.value })}
                  placeholder="At least 6 characters"
                  required
                />
                <button
                  type="button"
                  className="password-toggle"
                  aria-label={showPassword ? 'Hide password' : 'Show password'}
                  onClick={() => setShowPassword((s) => !s)}
                >
                  {showPassword ? (
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden>
                      <path d="M3 3l18 18" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
                      <path d="M10.59 10.59a3 3 0 104.82 4.82" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
                      <path d="M2.25 12c1.36-4.1 5.01-7.5 9.75-7.5 2.08 0 4 .44 5.75 1.2" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
                    </svg>
                  ) : (
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden>
                      <path d="M2.25 12c1.36-4.1 5.01-7.5 9.75-7.5S20.14 7.9 21.5 12c-1.36 4.1-5.01 7.5-9.75 7.5S3.61 16.1 2.25 12z" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
                      <path d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
                    </svg>
                  )}
                </button>
              </div>
            </label>
          </>
        )}

        <button className="btn btn-primary" type="submit" disabled={loading || !otpVerified}>
          {loading ? 'Creating account...' : 'Register'}
        </button>
        <p className="auth-link">Already registered? <Link to="/login">Sign in</Link></p>
      </form>
    </section>
  );
}
