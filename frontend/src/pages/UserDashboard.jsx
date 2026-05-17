import { useEffect, useState } from 'react';
import ComplaintForm from '../components/ComplaintForm';
import ComplaintTable from '../components/ComplaintTable';
import LoadingSpinner from '../components/LoadingSpinner';
import MessageBanner from '../components/MessageBanner';
import StatCard from '../components/StatCard';
import { complaintService } from '../services/complaintService';

export default function UserDashboard() {
  const [complaints, setComplaints] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState('');
  const [type, setType] = useState('success');

  const summary = complaints.reduce(
    (accumulator, complaint) => {
      accumulator.total += 1;
      if (complaint.status === 'OPEN') accumulator.open += 1;
      if (complaint.status === 'IN_PROGRESS') accumulator.inProgress += 1;
      if (complaint.status === 'RESOLVED') accumulator.resolved += 1;
      return accumulator;
    },
    { total: 0, open: 0, inProgress: 0, resolved: 0 },
  );

  const loadData = async () => {
    setLoading(true);
    try {
      const data = await complaintService.getMyComplaints();
      setComplaints(data.items || data);
    } catch (error) {
      setType('error');
      setMessage(error.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const submitComplaint = async (payload) => {
    setSaving(true);
    try {
      await complaintService.createComplaint(payload);
      setType('success');
      setMessage('Complaint submitted successfully.');
      await loadData();
    } catch (error) {
      setType('error');
      setMessage(error.message);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="dashboard-grid">
      <MessageBanner type={type} message={message} />
      <section className="stats-row">
        <StatCard label="Total complaints" value={summary.total} accent="accent-1" />
        <StatCard label="Open" value={summary.open} accent="accent-2" />
        <StatCard label="In progress" value={summary.inProgress} accent="accent-3" />
        <StatCard label="Resolved" value={summary.resolved} accent="accent-4" />
      </section>

      <section className="dashboard-layout">
        <ComplaintForm onSubmit={submitComplaint} loading={saving} />
        {loading ? <LoadingSpinner label="Fetching your complaints..." /> : <ComplaintTable complaints={complaints} />}
      </section>
    </div>
  );
}
