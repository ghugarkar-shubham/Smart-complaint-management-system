import { NavLink, useNavigate } from 'react-router-dom';

export default function Sidebar({ role, onLogout }) {
  const navigate = useNavigate();

  return (
    <aside className="sidebar">
      <div className="brand-block">
        <div className="brand-mark">OCI</div>
        <div>
          <p className="brand-name">Complaint Tracker</p>
          <p className="brand-subtitle">Modern issue management</p>
        </div>
      </div>

      <nav className="sidebar-nav">
        {role === 'ADMIN' ? (
          <NavLink to="/admin" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
            Admin Dashboard
          </NavLink>
        ) : (
          <NavLink to="/dashboard" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
            User Dashboard
          </NavLink>
        )}
        <button
          className="nav-link nav-button"
          type="button"
          onClick={() => {
            navigate('/dashboard');
            onLogout();
          }}
        >
          Sign out
        </button>
      </nav>
    </aside>
  );
}
