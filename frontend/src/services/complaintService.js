import { api } from './api';

export const complaintService = {
  getMyComplaints: () => api.request('/complaints'),
  createComplaint: (payload) => api.request('/complaints', { method: 'POST', body: payload }),
  getComplaint: (id) => api.request(`/complaints/${id}`),
  getAdminComplaints: (query = '') => api.request(`/admin/complaints${query}`),
  updateStatus: (id, payload) => api.request(`/admin/complaints/${id}/status`, { method: 'PUT', body: JSON.stringify(payload) }),
  deleteComplaint: (id) => api.request(`/admin/complaints/${id}`, { method: 'DELETE' }),
  getSummary: () => api.request('/admin/summary'),
};
