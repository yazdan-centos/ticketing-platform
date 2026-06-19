import React, { createContext, useContext, useState, useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate, useNavigate, useLocation } from 'react-router-dom';

// ==================== Auth Context ====================
const AuthContext = createContext(null);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
};

const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Load user data from sessionStorage on mount
    const storedUser = sessionStorage.getItem('user');
    const storedToken = sessionStorage.getItem('token');
    const storedRoles = sessionStorage.getItem('roles');
    const storedPermissions = sessionStorage.getItem('permissions');

    if (storedUser && storedToken) {
      setUser({
        ...JSON.parse(storedUser),
        token: storedToken,
        roles: storedRoles ? JSON.parse(storedRoles) : [],
        permissions: storedPermissions ? JSON.parse(storedPermissions) : []
      });
    }
    setLoading(false);
  }, []);

  const login = async (username, password) => {
    try {
      const response = await fetch('http://localhost:8080/api/auth/authenticate', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
      });

      if (!response.ok) throw new Error('Authentication failed');

      const data = await response.json();
      
      // Store in sessionStorage
      sessionStorage.setItem('user', JSON.stringify(data.user));
      sessionStorage.setItem('token', data.token);
      sessionStorage.setItem('roles', JSON.stringify(data.roles || []));
      sessionStorage.setItem('permissions', JSON.stringify(data.permissions || []));

      setUser({
        ...data.user,
        token: data.token,
        roles: data.roles || [],
        permissions: data.permissions || []
      });

      return { success: true };
    } catch (error) {
      return { success: false, error: error.message };
    }
  };

  const logout = async () => {
    try {
      await fetch('http://localhost:8080/api/auth/signout', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${user?.token}`
        }
      });
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      // Clear sessionStorage
      sessionStorage.removeItem('user');
      sessionStorage.removeItem('token');
      sessionStorage.removeItem('roles');
      sessionStorage.removeItem('permissions');
      setUser(null);
    }
  };

  const hasRole = (role) => {
    return user?.roles?.includes(role) || false;
  };

  const hasPermission = (permission) => {
    return user?.permissions?.includes(permission) || false;
  };

  const hasAnyRole = (roles) => {
    return roles.some(role => hasRole(role));
  };

  const hasAnyPermission = (permissions) => {
    return permissions.some(permission => hasPermission(permission));
  };

  return (
    <AuthContext.Provider value={{
      user,
      login,
      logout,
      hasRole,
      hasPermission,
      hasAnyRole,
      hasAnyPermission,
      loading
    }}>
      {children}
    </AuthContext.Provider>
  );
};

// ==================== Protected Route Components ====================
const ProtectedRoute = ({ children, requiredRoles = [], requiredPermissions = [] }) => {
  const { user, hasAnyRole, hasAnyPermission, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return <div>Loading...</div>;
  }

  if (!user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (requiredRoles.length > 0 && !hasAnyRole(requiredRoles)) {
    return <Navigate to="/unauthorized" replace />;
  }

  if (requiredPermissions.length > 0 && !hasAnyPermission(requiredPermissions)) {
    return <Navigate to="/unauthorized" replace />;
  }

  return children;
};

const PublicRoute = ({ children }) => {
  const { user } = useAuth();
  const location = useLocation();

  if (user) {
    const from = location.state?.from?.pathname || '/dashboard';
    return <Navigate to={from} replace />;
  }

  return children;
};

// ==================== Page Components ====================
const LoginPage = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    const result = await login(username, password);
    
    if (result.success) {
      const from = location.state?.from?.pathname || '/dashboard';
      navigate(from, { replace: true });
    } else {
      setError(result.error || 'Login failed');
    }
  };

  return (
    <div style={{ maxWidth: '400px', margin: '100px auto', padding: '20px' }}>
      <h2>Login</h2>
      <form onSubmit={handleSubmit}>
        <div style={{ marginBottom: '15px' }}>
          <label>Username:</label>
          <input
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            style={{ width: '100%', padding: '8px', marginTop: '5px' }}
            required
          />
        </div>
        <div style={{ marginBottom: '15px' }}>
          <label>Password:</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            style={{ width: '100%', padding: '8px', marginTop: '5px' }}
            required
          />
        </div>
        {error && <div style={{ color: 'red', marginBottom: '10px' }}>{error}</div>}
        <button type="submit" style={{ width: '100%', padding: '10px' }}>
          Login
        </button>
      </form>
    </div>
  );
};

const DashboardPage = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <div style={{ padding: '20px' }}>
      <h1>Dashboard</h1>
      <p>Welcome, {user?.username}!</p>
      <p>Roles: {user?.roles?.join(', ') || 'None'}</p>
      <p>Permissions: {user?.permissions?.join(', ') || 'None'}</p>
      <button onClick={handleLogout} style={{ padding: '10px 20px', marginTop: '20px' }}>
        Logout
      </button>
    </div>
  );
};

const UnauthorizedPage = () => {
  const navigate = useNavigate();

  return (
    <div style={{ padding: '20px', textAlign: 'center' }}>
      <h1>403 - Unauthorized</h1>
      <p>You don't have permission to access this page.</p>
      <button onClick={() => navigate('/dashboard')} style={{ padding: '10px 20px', marginTop: '20px' }}>
        Go to Dashboard
      </button>
    </div>
  );
};

// ==================== Admin Pages ====================
const AdminAccessPage = () => {
  return (
    <div style={{ padding: '20px' }}>
      <h1>Admin Access Management</h1>
      <p>Manage user permissions, roles, and scopes.</p>
    </div>
  );
};

const PermissionsPage = () => {
  return (
    <div style={{ padding: '20px' }}>
      <h1>Permissions Management</h1>
      <p>View and manage system permissions.</p>
    </div>
  );
};

// ==================== Ticket Pages ====================
const TicketsListPage = () => {
  return (
    <div style={{ padding: '20px' }}>
      <h1>Tickets</h1>
      <p>View and manage tickets.</p>
    </div>
  );
};

const TicketDetailPage = () => {
  return (
    <div style={{ padding: '20px' }}>
      <h1>Ticket Details</h1>
      <p>View ticket details and messages.</p>
    </div>
  );
};

const CreateTicketPage = () => {
  return (
    <div style={{ padding: '20px' }}>
      <h1>Create Ticket</h1>
      <p>Create a new ticket.</p>
    </div>
  );
};

// ==================== Customer Pages ====================
const CustomersListPage = () => {
  return (
    <div style={{ padding: '20px' }}>
      <h1>Customers</h1>
      <p>View and manage customers.</p>
    </div>
  );
};

const CustomerDetailPage = () => {
  return (
    <div style={{ padding: '20px' }}>
      <h1>Customer Details</h1>
      <p>View and edit customer information.</p>
    </div>
  );
};

const CreateCustomerPage = () => {
  return (
    <div style={{ padding: '20px' }}>
      <h1>Create Customer</h1>
      <p>Create a new customer account.</p>
    </div>
  );
};

// ==================== Team Member Pages ====================
const TeamMembersListPage = () => {
  return (
    <div style={{ padding: '20px' }}>
      <h1>Team Members</h1>
      <p>View and manage team members.</p>
    </div>
  );
};

const TeamMemberDetailPage = () => {
  return (
    <div style={{ padding: '20px' }}>
      <h1>Team Member Details</h1>
      <p>View and edit team member information.</p>
    </div>
  );
};

const CreateTeamMemberPage = () => {
  return (
    <div style={{ padding: '20px' }}>
      <h1>Create Team Member</h1>
      <p>Create a new team member account.</p>
    </div>
  );
};

// ==================== Team Manager Pages ====================
const TeamManagersListPage = () => {
  return (
    <div style={{ padding: '20px' }}>
      <h1>Team Managers</h1>
      <p>View and manage team managers.</p>
    </div>
  );
};

const TeamManagerDetailPage = () => {
  return (
    <div style={{ padding: '20px' }}>
      <h1>Team Manager Details</h1>
      <p>View and edit team manager information.</p>
    </div>
  );
};

const CreateTeamManagerPage = () => {
  return (
    <div style={{ padding: '20px' }}>
      <h1>Create Team Manager</h1>
      <p>Create a new team manager account.</p>
    </div>
  );
};

// ==================== SLA Contract Pages ====================
const SlaContractsListPage = () => {
  return (
    <div style={{ padding: '20px' }}>
      <h1>SLA Contracts</h1>
      <p>View and manage SLA contracts.</p>
    </div>
  );
};

const SlaContractDetailPage = () => {
  return (
    <div style={{ padding: '20px' }}>
      <h1>SLA Contract Details</h1>
      <p>View and edit SLA contract information.</p>
    </div>
  );
};

const CreateSlaContractPage = () => {
  return (
    <div style={{ padding: '20px' }}>
      <h1>Create SLA Contract</h1>
      <p>Create a new SLA contract.</p>
    </div>
  );
};

// ==================== Main App Component ====================
function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          {/* Public Routes */}
          <Route path="/login" element={
            <PublicRoute>
              <LoginPage />
            </PublicRoute>
          } />
          
          <Route path="/unauthorized" element={<UnauthorizedPage />} />

          {/* Protected Routes - Dashboard */}
          <Route path="/dashboard" element={
            <ProtectedRoute>
              <DashboardPage />
            </ProtectedRoute>
          } />

          {/* Admin Routes - Access Management */}
          <Route path="/admin/access" element={
            <ProtectedRoute requiredRoles={['ADMIN']}>
              <AdminAccessPage />
            </ProtectedRoute>
          } />

          <Route path="/admin/permissions" element={
            <ProtectedRoute requiredRoles={['ADMIN']}>
              <PermissionsPage />
            </ProtectedRoute>
          } />

          {/* Ticket Routes */}
          <Route path="/tickets" element={
            <ProtectedRoute requiredRoles={['ADMIN', 'TEAM_MANAGER', 'TEAM_MEMBER', 'CUSTOMER']}>
              <TicketsListPage />
            </ProtectedRoute>
          } />

          <Route path="/tickets/create" element={
            <ProtectedRoute requiredRoles={['ADMIN', 'TEAM_MANAGER', 'TEAM_MEMBER', 'CUSTOMER']}>
              <CreateTicketPage />
            </ProtectedRoute>
          } />

          <Route path="/tickets/:id" element={
            <ProtectedRoute requiredRoles={['ADMIN', 'TEAM_MANAGER', 'TEAM_MEMBER', 'CUSTOMER']}>
              <TicketDetailPage />
            </ProtectedRoute>
          } />

          {/* Customer Routes */}
          <Route path="/customers" element={
            <ProtectedRoute requiredRoles={['ADMIN', 'TEAM_MANAGER']}>
              <CustomersListPage />
            </ProtectedRoute>
          } />

          <Route path="/customers/create" element={
            <ProtectedRoute requiredRoles={['ADMIN']}>
              <CreateCustomerPage />
            </ProtectedRoute>
          } />

          <Route path="/customers/:id" element={
            <ProtectedRoute requiredRoles={['ADMIN', 'TEAM_MANAGER']}>
              <CustomerDetailPage />
            </ProtectedRoute>
          } />

          {/* Team Member Routes */}
          <Route path="/team-members" element={
            <ProtectedRoute requiredRoles={['ADMIN', 'TEAM_MANAGER']}>
              <TeamMembersListPage />
            </ProtectedRoute>
          } />

          <Route path="/team-members/create" element={
            <ProtectedRoute requiredRoles={['ADMIN', 'TEAM_MANAGER']}>
              <CreateTeamMemberPage />
            </ProtectedRoute>
          } />

          <Route path="/team-members/:id" element={
            <ProtectedRoute requiredRoles={['ADMIN', 'TEAM_MANAGER']}>
              <TeamMemberDetailPage />
            </ProtectedRoute>
          } />

          {/* Team Manager Routes */}
          <Route path="/team-managers" element={
            <ProtectedRoute requiredRoles={['ADMIN']}>
              <TeamManagersListPage />
            </ProtectedRoute>
          } />

          <Route path="/team-managers/create" element={
            <ProtectedRoute requiredRoles={['ADMIN']}>
              <CreateTeamManagerPage />
            </ProtectedRoute>
          } />

          <Route path="/team-managers/:id" element={
            <ProtectedRoute requiredRoles={['ADMIN']}>
              <TeamManagerDetailPage />
            </ProtectedRoute>
          } />

          {/* SLA Contract Routes */}
          <Route path="/sla-contracts" element={
            <ProtectedRoute requiredRoles={['ADMIN', 'TEAM_MANAGER']}>
              <SlaContractsListPage />
            </ProtectedRoute>
          } />

          <Route path="/sla-contracts/create" element={
            <ProtectedRoute requiredRoles={['ADMIN', 'TEAM_MANAGER']}>
              <CreateSlaContractPage />
            </ProtectedRoute>
          } />

          <Route path="/sla-contracts/:id" element={
            <ProtectedRoute requiredRoles={['ADMIN', 'TEAM_MANAGER']}>
              <SlaContractDetailPage />
            </ProtectedRoute>
          } />

          {/* Default Route */}
          <Route path="/" element={<Navigate to="/dashboard" replace />} />

          {/* 404 Not Found */}
          <Route path="*" element={
            <div style={{ padding: '20px', textAlign: 'center' }}>
              <h1>404 - Page Not Found</h1>
              <p>The page you're looking for doesn't exist.</p>
            </div>
          } />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
