export default function Navbar({ role, onLogout }) {
  return (
    <header className="topbar">
      <div>
        <p className="eyebrow">Online Complaint & Issue Tracking System</p>
        <h1>{role === 'ADMIN' ? 'Admin Command Center' : 'User Dashboard'}</h1>
      </div>
      <div className="topbar-actions">
        <span className="role-pill">{role}</span>
        <button className="btn btn-secondary" onClick={onLogout} type="button">
          Logout
        </button>
      </div>
    </header>
  );
}
