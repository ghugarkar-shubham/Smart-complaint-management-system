export default function ComplaintTable({ complaints, onStatusChange, onDelete, admin = false }) {
  return (
    <div className="table-card card">
      <div className="section-heading">
        <div>
          <p className="eyebrow">Records</p>
          <h2>{admin ? 'All complaints' : 'My complaints'}</h2>
        </div>
      </div>

      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Title</th>
              <th>Category</th>
              <th>Status</th>
              <th>Created</th>
              {admin && <th>Actions</th>}
            </tr>
          </thead>
          <tbody>
            {complaints.map((complaint) => (
              <tr key={complaint.id}>
                <td>
                  <strong>{complaint.title}</strong>
                  <p className="muted">{complaint.description}</p>
                </td>
                <td>{complaint.category}</td>
                <td>
                  <span className={`status-chip ${complaint.status.toLowerCase().replaceAll(' ', '-')}`}>{complaint.status}</span>
                </td>
                <td>{new Date(complaint.createdAt).toLocaleString()}</td>
                {admin && (
                  <td>
                    <div className="table-actions">
                      <select value={complaint.status} onChange={(event) => onStatusChange(complaint.id, event.target.value)}>
                        <option>OPEN</option>
                        <option>IN_PROGRESS</option>
                        <option>RESOLVED</option>
                      </select>
                      <button className="btn btn-ghost danger" type="button" onClick={() => onDelete(complaint.id)}>
                        Delete
                      </button>
                    </div>
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
