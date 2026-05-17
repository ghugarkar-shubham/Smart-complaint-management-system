import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { register } from '../services/authService';
import MessageBanner from '../components/MessageBanner';

export default function RegisterPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ fullName: '', email: '', password: '' });
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [type, setType] = useState('success');
  const [showPassword, setShowPassword] = useState(false);

  const submit = async (event) => {
    event.preventDefault();
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

    setLoading(true);
    setMessage('');
    try {
      await register({ ...form, fullName: form.fullName.trim() });
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
        <label>
          Full Name
          <input value={form.fullName} onChange={(event) => setForm({ ...form, fullName: event.target.value })} required />
        </label>
        <label>
          Email
          <input type="email" value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value })} required />
        </label>
        <label>
          Password
          <div className="password-field">
            <input
              type={showPassword ? 'text' : 'password'}
              value={form.password}
              onChange={(event) => setForm({ ...form, password: event.target.value })}
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
        <button className="btn btn-primary" type="submit" disabled={loading}>
          {loading ? 'Creating account...' : 'Register'}
        </button>
        <p className="auth-link">Already registered? <Link to="/login">Sign in</Link></p>
      </form>
    </section>
  );
}
