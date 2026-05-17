import { Navigate, Route, Routes, useNavigate } from 'react-router-dom';
import { useEffect, useMemo, useState } from 'react';
import Navbar from './components/Navbar';
import Sidebar from './components/Sidebar';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import UserDashboard from './pages/UserDashboard';
import AdminDashboard from './pages/AdminDashboard';

function ProtectedRoute({ children, roles }) {
  const token = localStorage.getItem('token');
  const role = localStorage.getItem('role');

  if (!token) {
    return <Navigate to="/login" replace />;
  }

  if (roles && !roles.includes(role)) {
    return <Navigate to="/dashboard" replace />;
  }

  return children;
}

function Shell({ children, onLogout }) {
  const role = localStorage.getItem('role') || 'USER';

  return (
    <div className="app-shell">
      <Sidebar role={role} onLogout={onLogout} />
      <div className="app-main">
        <Navbar role={role} onLogout={onLogout} />
        <main className="page-content">{children}</main>
      </div>
    </div>
  );
}

export default function App() {
  const navigate = useNavigate();
  const [authTick, setAuthTick] = useState(0);
  const isLoggedIn = useMemo(() => Boolean(localStorage.getItem('token')), [authTick]);

  useEffect(() => {
    const onStorage = () => setAuthTick((value) => value + 1);
    window.addEventListener('storage', onStorage);
    return () => window.removeEventListener('storage', onStorage);
  }, []);

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('name');
    localStorage.removeItem('email');
    setAuthTick((value) => value + 1);
    navigate('/login');
  };

  return (
    <Routes>
      <Route path="/" element={<Navigate to={isLoggedIn ? '/dashboard' : '/login'} replace />} />
      <Route path="/login" element={<LoginPage onAuth={() => setAuthTick((value) => value + 1)} />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route
        path="/dashboard"
        element={
          <ProtectedRoute>
            <Shell onLogout={handleLogout}>
              <UserDashboard />
            </Shell>
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin"
        element={
          <ProtectedRoute roles={['ADMIN']}>
            <Shell onLogout={handleLogout}>
              <AdminDashboard />
            </Shell>
          </ProtectedRoute>
        }
      />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
