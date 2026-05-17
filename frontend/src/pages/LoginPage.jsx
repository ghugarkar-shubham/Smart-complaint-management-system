import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { login } from '../services/authService';
import MessageBanner from '../components/MessageBanner';

export default function LoginPage({ onAuth }) {
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: '', password: '' });
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [type, setType] = useState('success');
  const [showPassword, setShowPassword] = useState(false);

  const submit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setMessage('');
    try {
      const response = await login(form);
      onAuth?.();
      navigate(response.role === 'ADMIN' ? '/admin' : '/dashboard');
    } catch (error) {
      setType('error');
      setMessage(error.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="auth-layout">
      <div className="auth-panel hero-panel">
        <p className="eyebrow">Issue tracking platform</p>
        <h1>Unified Complaint Resolution Platform</h1>
        <p className="muted">Track issues, resolve requests faster, and keep every complaint visible from submission to closure.</p>
      </div>
      <form className="auth-panel card auth-form" onSubmit={submit}>
        <p className="eyebrow">Welcome back</p>
        <h2>Sign in</h2>
        <MessageBanner type={type} message={message} />
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
          {loading ? 'Signing in...' : 'Login'}
        </button>
        <p className="auth-link">No account yet? <Link to="/register">Create one</Link></p>
      </form>
    </section>
  );
}
