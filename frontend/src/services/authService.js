import { api } from './api';

/**
 * User login with email and password.
 * Stores authentication token and user details in localStorage.
 *
 * @param {Object} payload - Login credentials {email, password}
 * @returns {Promise} Response with user details and role
 */
export async function login(payload) {
  const data = await api.request('/auth/login', {
    method: 'POST',
    body: JSON.stringify(payload),
  });

  localStorage.setItem('token', data.token);
  localStorage.setItem('role', data.role);
  localStorage.setItem('name', data.fullName);
  localStorage.setItem('email', data.email);

  return data;
}

/**
 * Register new user with email-based OTP verification.
 * User must complete email OTP verification before registration.
 *
 * @param {Object} payload - Registration data {fullName, email, password}
 * @returns {Promise} Registration confirmation
 */
export async function register(payload) {
  return api.request('/auth/register', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

/**
 * Send OTP to user's email for verification.
 * Called as first step of registration process.
 *
 * @param {Object} payload - Request data {email}
 * @returns {Promise} OTP response with status
 */
export async function sendOtp(payload) {
  return api.request('/auth/send-otp', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

/**
 * Verify OTP sent to user's email.
 * User provides the OTP they received in their email.
 *
 * @param {Object} payload - Verification data {email, otp}
 * @returns {Promise} Verification response with status
 */
export async function verifyOtp(payload) {
  return api.request('/auth/verify-otp', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

/**
 * Resend OTP to user's email.
 * Used when user didn't receive OTP or it expired.
 *
 * @param {Object} payload - Request data {email}
 * @returns {Promise} OTP response with status
 */
export async function resendOtp(payload) {
  return api.request('/auth/resend-otp', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}
