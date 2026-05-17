import { useEffect, useMemo, useState } from 'react';
import ComplaintTable from '../components/ComplaintTable';
import LoadingSpinner from '../components/LoadingSpinner';
import MessageBanner from '../components/MessageBanner';
import StatCard from '../components/StatCard';
import { complaintService } from '../services/complaintService';

export default function AdminDashboard() {
  const [complaints, setComplaints] = useState([]);
  const [summary, setSummary] = useState({ total: 0, open: 0, inProgress: 0, resolved: 0 });
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');
  const [type, setType] = useState('success');
  const [search, setSearch] = useState('');
  const [status, setStatus] = useState('ALL');

  const query = useMemo(() => {
    const params = new URLSearchParams();
    if (search) params.set('search', search);
    if (status !== 'ALL') params.set('status', status);
    const qs = params.toString();
    return qs ? `?${qs}` : '';
  }, [search, status]);

  const loadData = async () => {
    setLoading(true);
    try {
      const [records, totals] = await Promise.all([complaintService.getAdminComplaints(query), complaintService.getSummary()]);
      setComplaints(records.items || records);
      setSummary(totals);
    } catch (error) {
      setType('error');
      setMessage(error.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [query]);

  const updateStatus = async (id, nextStatus) => {
    try {
      await complaintService.updateStatus(id, { status: nextStatus });
      setType('success');
      setMessage('Complaint status updated.');
      await loadData();
    } catch (error) {
      setType('error');
      setMessage(error.message);
    }
  };

  const deleteComplaint = async (id) => {
    if (!window.confirm('Delete this complaint permanently?')) return;
    try {
      await complaintService.deleteComplaint(id);
      setType('success');
      setMessage('Complaint deleted.');
      await loadData();
    } catch (error) {
      setType('error');
      setMessage(error.message);
    }
  };

  return (
    <div className="dashboard-grid">
      <MessageBanner type={type} message={message} />
      <section className="stats-row">
        <StatCard label="Total" value={summary.total} accent="accent-1" />
        <StatCard label="Open" value={summary.open} accent="accent-2" />
        <StatCard label="In progress" value={summary.inProgress} accent="accent-3" />
        <StatCard label="Resolved" value={summary.resolved} accent="accent-4" />
      </section>

      <section className="filters-row card">
        <input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search complaints" />
        <select value={status} onChange={(event) => setStatus(event.target.value)}>
          <option value="ALL">All status</option>
          <option value="OPEN">Open</option>
          <option value="IN_PROGRESS">In progress</option>
          <option value="RESOLVED">Resolved</option>
        </select>
      </section>

      {loading ? (
        <LoadingSpinner label="Loading complaints..." />
      ) : (
        <ComplaintTable admin complaints={complaints} onStatusChange={updateStatus} onDelete={deleteComplaint} />
      )}
    </div>
  );
}
