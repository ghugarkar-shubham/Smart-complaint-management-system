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
  const [confirmPassword, setConfirmPassword] = useState('');
  const [otp, setOtp] = useState('');
  const [otpSent, setOtpSent] = useState(false);
  const [otpVerified, setOtpVerified] = useState(false);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [type, setType] = useState('success');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  /**
   * Reset OTP-related state when email changes
   */
  const resetOtpState = () => {
    setOtp('');
    setOtpSent(false);
    setOtpVerified(false);
    setConfirmPassword('');
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

    if (form.password !== confirmPassword) {
      setType('error');
      setMessage('Passwords do not match');
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

      {/* Step 1: Email Entry & OTP Send */}
      {!otpSent && (
        <form className="auth-panel card auth-form" onSubmit={(e) => { e.preventDefault(); sendOtpCode(); }} autoComplete="off">
          <p className="eyebrow">Step 1 of 3</p>
          <h2>Enter your email</h2>
          <MessageBanner type={type} message={message} />

          <label>
            Email
            <input
              type="email"
              value={form.email}
              onChange={(event) => setForm({ ...form, email: event.target.value })}
              placeholder="your@email.com"
              required
              autoFocus
              autoComplete="off"
            />
          </label>

          <button className="btn btn-primary" type="submit" disabled={loading || !form.email.trim()}>
            {loading ? 'Sending OTP...' : 'Send OTP'}
          </button>

          <p className="auth-link">Already registered? <Link to="/login">Sign in</Link></p>
        </form>
      )}

      {/* Step 2: OTP Verification */}
      {otpSent && !otpVerified && (
        <form className="auth-panel card auth-form" onSubmit={(e) => { e.preventDefault(); verifyOtpCode(); }}>
          <p className="eyebrow">Step 2 of 3</p>
          <h2>Verify your email</h2>
          <MessageBanner type={type} message={message} />

          <div style={{ backgroundColor: '#f5f5f5', padding: '1rem', borderRadius: '8px', marginBottom: '1.5rem', textAlign: 'center' }}>
            <p style={{ margin: '0', color: '#666', fontSize: '0.9em' }}>We sent a 6-digit code to:</p>
            <p style={{ margin: '0.5rem 0 0 0', fontWeight: '600', color: '#333' }}>{form.email}</p>
          </div>

          <label>
            Enter OTP
            <input
              type="text"
              inputMode="numeric"
              maxLength="6"
              value={otp}
              onChange={(event) => setOtp(event.target.value.replace(/[^0-9]/g, ''))}
              placeholder="000000"
              required
              autoFocus
              style={{ fontSize: '1.5em', letterSpacing: '0.5em', textAlign: 'center' }}
            />
          </label>

          <button className="btn btn-primary" type="submit" disabled={loading || !otp.trim() || otp.length !== 6}>
            {loading ? 'Verifying...' : 'Verify OTP'}
          </button>

          <p style={{ fontSize: '0.85em', color: '#666', marginTop: '1rem', textAlign: 'center' }}>
            Didn't receive the code? <button
              type="button"
              style={{ background: 'none', border: 'none', color: '#007bff', cursor: 'pointer', textDecoration: 'underline', fontSize: 'inherit', padding: 0 }}
              onClick={resendOtpCode}
              disabled={loading}
            >
              Resend OTP
            </button>
          </p>

          <button 
            type="button"
            style={{ background: 'none', border: 'none', color: '#007bff', cursor: 'pointer', textDecoration: 'underline', marginTop: '1rem', width: '100%', padding: '0.5rem', fontSize: '0.9em' }}
            onClick={() => {
              resetOtpState();
              setForm({ fullName: '', email: '', password: '' });
            }}
          >
            Change email
          </button>
        </form>
      )}

      {/* Step 3: Registration Form */}
      {otpVerified && (
        <form className="auth-panel card auth-form" onSubmit={submit} autoComplete="off">
          <p className="eyebrow">Step 3 of 3</p>
          <h2>Create your account</h2>
          <MessageBanner type={type} message={message} />

          <div style={{ backgroundColor: '#f0f8f0', padding: '1rem', borderRadius: '8px', marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <span style={{ color: '#4caf50', fontSize: '1.2em' }}>✓</span>
            <div>
              <p style={{ margin: '0', fontWeight: '600', color: '#333', fontSize: '0.9em' }}>Email verified</p>
              <p style={{ margin: '0.25rem 0 0 0', color: '#666', fontSize: '0.85em' }}>{form.email}</p>
            </div>
          </div>

          <label>
            Full Name
            <input
              type="text"
              value={form.fullName}
              onChange={(event) => setForm({ ...form, fullName: event.target.value })}
              placeholder="John Doe"
              required
              autoFocus
              autoComplete="off"
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
                autoComplete="new-password"
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

          <label>
            Confirm Password
            <div className="password-field">
              <input
                type={showConfirmPassword ? 'text' : 'password'}
                value={confirmPassword}
                onChange={(event) => setConfirmPassword(event.target.value)}
                placeholder="Re-enter your password"
                required
                autoComplete="new-password"
              />
              <button
                type="button"
                className="password-toggle"
                aria-label={showConfirmPassword ? 'Hide password' : 'Show password'}
                onClick={() => setShowConfirmPassword((s) => !s)}
              >
                {showConfirmPassword ? (
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

          <button className="btn btn-primary" type="submit" disabled={loading}>
            {loading ? 'Creating account...' : 'Register'}
          </button>

          <button 
            type="button"
            style={{ background: 'none', border: 'none', color: '#007bff', cursor: 'pointer', textDecoration: 'underline', marginTop: '1rem', width: '100%', padding: '0.5rem', fontSize: '0.9em' }}
            onClick={() => {
              resetOtpState();
              setForm({ fullName: '', email: '', password: '' });
            }}
          >
            Start over with different email
          </button>

          <p className="auth-link">Already registered? <Link to="/login">Sign in</Link></p>
        </form>
      )}
    </section>
  );
}
