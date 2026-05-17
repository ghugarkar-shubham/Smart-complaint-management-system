import { api } from './api';

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

export async function register(payload) {
  return api.request('/auth/register', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}
