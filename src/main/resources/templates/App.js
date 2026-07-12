import React, { createContext, useContext, useState, useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate, useNavigate, useLocation, useParams } from 'react-router-dom';

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
      try {
        const parsedUser = {
          ...JSON.parse(storedUser),
          token: storedToken,
          roles: storedRoles ? JSON.parse(storedRoles) : [],
          permissions: storedPermissions ? JSON.parse(storedPermissions) : []
        };
        setUser(parsedUser);
        authorizedFetch('/api/auth/me')
          .then((response) => response.ok ? response.json() : null)
          .then((profile) => {
            if (!profile) {
              return;
            }
            const normalizedProfile = {
              ...parsedUser,
              ...profile,
              token: storedToken,
              roles: profile.roles || parsedUser.roles,
              permissions: profile.permissions || parsedUser.permissions
            };
            sessionStorage.setItem('user', JSON.stringify(normalizedProfile));
            sessionStorage.setItem('roles', JSON.stringify(normalizedProfile.roles || []));
            sessionStorage.setItem('permissions', JSON.stringify(normalizedProfile.permissions || []));
            setUser(normalizedProfile);
          })
          .catch(() => {});
      } catch {
        sessionStorage.removeItem('user');
        sessionStorage.removeItem('token');
        sessionStorage.removeItem('roles');
        sessionStorage.removeItem('permissions');
      }
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
      
      const token = data.token || data.accessToken;
      const roles = data.roles || (data.role ? [String(data.role).replace(/^ROLE_/, '')] : []);
      const permissions = data.permissions || [];
      const user = data.user || {
        username: data.currentUser || username,
        email: '',
        avatarUrl: null,
        roles
      };

      sessionStorage.setItem('user', JSON.stringify(user));
      sessionStorage.setItem('token', token);
      sessionStorage.setItem('roles', JSON.stringify(roles));
      sessionStorage.setItem('permissions', JSON.stringify(permissions));

      setUser({
        ...user,
        token,
        roles,
        permissions
      });

      authorizedFetch('/api/auth/me')
        .then((response) => response.ok ? response.json() : null)
        .then((profile) => {
          if (!profile) {
            return;
          }
          const normalizedProfile = {
            ...user,
            ...profile,
            token,
            roles: profile.roles || roles,
            permissions: profile.permissions || permissions
          };
          sessionStorage.setItem('user', JSON.stringify(normalizedProfile));
          sessionStorage.setItem('roles', JSON.stringify(normalizedProfile.roles || []));
          sessionStorage.setItem('permissions', JSON.stringify(normalizedProfile.permissions || []));
          setUser(normalizedProfile);
        })
        .catch(() => {});

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

const API_BASE = 'http://localhost:8080';

const apiUrl = (path) => `${API_BASE}${path}`;

const authorizedFetch = (path, options = {}) => {
  const token = sessionStorage.getItem('token');
  const headers = {
    ...(options.headers || {})
  };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  return fetch(apiUrl(path), {
    ...options,
    headers
  });
};

const resolveAvatarUrl = (avatarUrl) => {
  if (!avatarUrl) {
    return null;
  }
  if (avatarUrl.startsWith('http://') || avatarUrl.startsWith('https://')) {
    return avatarUrl;
  }
  return apiUrl(avatarUrl);
};

const getAvatarLabel = (entity) => {
  const candidate = [entity?.firstName, entity?.lastName].filter(Boolean).join(' ').trim() || entity?.username || '?';
  const parts = candidate.split(/\s+/).filter(Boolean);
  const initials = parts.slice(0, 2).map((part) => part[0]).join('').toUpperCase();
  return initials || '?';
};

const Avatar = ({ entity, size = 56 }) => {
  const avatarUrl = resolveAvatarUrl(entity?.avatarUrl);
  const label = getAvatarLabel(entity);

  const style = {
    width: size,
    height: size,
    borderRadius: '50%',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    overflow: 'hidden',
    background: 'linear-gradient(135deg, #334155, #0f172a)',
    color: '#fff',
    flexShrink: 0,
    fontWeight: 700,
    letterSpacing: '0.04em'
  };

  if (avatarUrl) {
    return <img src={avatarUrl} alt={entity?.username || 'avatar'} style={{ ...style, objectFit: 'cover' }} />;
  }

  return <div style={style}>{label}</div>;
};

const EntityCard = ({ entity, subtitle, onClick }) => {
  return (
    <div style={{
      display: 'flex',
      gap: '16px',
      alignItems: 'center',
      padding: '16px',
      border: '1px solid #e2e8f0',
      borderRadius: '16px',
      background: '#fff',
      boxShadow: '0 10px 24px rgba(15, 23, 42, 0.06)'
    }}>
      <Avatar entity={entity} />
      <div style={{ flex: 1 }}>
        <div style={{ fontWeight: 700 }}>{entity?.username}</div>
        <div style={{ color: '#475569', fontSize: '14px' }}>{subtitle}</div>
        <div style={{ color: '#64748b', fontSize: '13px', marginTop: '4px' }}>{entity?.email}</div>
      </div>
      <button onClick={onClick} style={{ padding: '10px 14px' }}>
        View
      </button>
    </div>
  );
};

const AvatarUpload = ({ onUpload, busy, label = 'Update Avatar' }) => {
  const [file, setFile] = useState(null);

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!file) {
      return;
    }
    await onUpload(file);
    setFile(null);
  };

  return (
    <form onSubmit={handleSubmit} style={{ display: 'flex', gap: '12px', alignItems: 'center', flexWrap: 'wrap' }}>
      <input type="file" accept="image/*" onChange={(event) => setFile(event.target.files?.[0] || null)} />
      <button type="submit" disabled={busy || !file} style={{ padding: '10px 14px' }}>
        {busy ? 'Uploading...' : label}
      </button>
    </form>
  );
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
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState('');

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  const handleAvatarUpload = async (file) => {
    if (!user?.id) {
      setError('Profile is not loaded yet');
      return;
    }

    const role = user?.roles?.[0]?.replace(/^ROLE_/, '') || '';
    const endpointByRole = {
      TEAM_MANAGER: `/api/team-managers/${user.id}/avatar`,
      TEAM_MEMBER: `/api/team-members/${user.id}/avatar`,
      CUSTOMER: `/api/customers/${user.id}/avatar`
    };

    const endpoint = endpointByRole[role];
    if (!endpoint) {
      setError('Your role cannot update avatars here');
      return;
    }

    setUploading(true);
    setError('');
    try {
      const formData = new FormData();
      formData.append('file', file);
      const response = await authorizedFetch(endpoint, {
        method: 'POST',
        body: formData
      });
      if (!response.ok) {
        throw new Error('Avatar upload failed');
      }
      const updated = await response.json();
      const normalizedUser = {
        ...user,
        ...updated,
        token: user.token,
        roles: user.roles,
        permissions: user.permissions
      };
      sessionStorage.setItem('user', JSON.stringify(normalizedUser));
      setUser(normalizedUser);
    } catch (uploadError) {
      setError(uploadError.message);
    } finally {
      setUploading(false);
    }
  };

  return (
    <div style={{ padding: '20px' }}>
      <h1>Dashboard</h1>
      <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '12px' }}>
        <Avatar entity={user} />
        <div>
          <p style={{ margin: 0 }}>Welcome, {user?.username}!</p>
          <p style={{ margin: 0 }}>Roles: {user?.roles?.join(', ') || 'None'}</p>
        </div>
      </div>
      <p>Roles: {user?.roles?.join(', ') || 'None'}</p>
      <p>Permissions: {user?.permissions?.join(', ') || 'None'}</p>
      <AvatarUpload onUpload={handleAvatarUpload} busy={uploading} label="Update My Avatar" />
      {error && <p style={{ color: 'crimson' }}>{error}</p>}
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
  const navigate = useNavigate();
  const [members, setMembers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const loadMembers = async () => {
      try {
        const response = await authorizedFetch('/api/team-members');
        if (!response.ok) {
          throw new Error('Failed to load team members');
        }
        setMembers(await response.json());
      } catch (loadError) {
        setError(loadError.message);
      } finally {
        setLoading(false);
      }
    };

    loadMembers();
  }, []);

  return (
    <div style={{ padding: '20px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h1>Team Members</h1>
        <button onClick={() => navigate('/team-members/create')} style={{ padding: '10px 14px' }}>
          New Member
        </button>
      </div>
      {loading && <p>Loading...</p>}
      {error && <p style={{ color: 'crimson' }}>{error}</p>}
      <div style={{ display: 'grid', gap: '12px' }}>
        {members.map((member) => (
          <EntityCard
            key={member.id}
            entity={member}
            subtitle={`${member.availabilityStatus || 'UNKNOWN'}${member.jobTitle ? ` • ${member.jobTitle}` : ''}`}
            onClick={() => navigate(`/team-members/${member.id}`)}
          />
        ))}
      </div>
    </div>
  );
};

const TeamMemberDetailPage = () => {
  const { id } = useParams();
  const [member, setMember] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [uploading, setUploading] = useState(false);

  useEffect(() => {
    const loadMember = async () => {
      try {
        const response = await authorizedFetch(`/api/team-members/${id}`);
        if (!response.ok) {
          throw new Error('Failed to load team member');
        }
        setMember(await response.json());
      } catch (loadError) {
        setError(loadError.message);
      } finally {
        setLoading(false);
      }
    };

    loadMember();
  }, [id]);

  const handleAvatarUpload = async (file) => {
    setUploading(true);
    setError('');
    try {
      const formData = new FormData();
      formData.append('file', file);
      const response = await authorizedFetch(`/api/team-members/${id}/avatar`, {
        method: 'POST',
        body: formData
      });

      if (!response.ok) {
        throw new Error('Avatar upload failed');
      }

      setMember(await response.json());
    } catch (uploadError) {
      setError(uploadError.message);
    } finally {
      setUploading(false);
    }
  };

  return (
    <div style={{ padding: '20px' }}>
      <h1>Team Member Details</h1>
      {loading && <p>Loading...</p>}
      {error && <p style={{ color: 'crimson' }}>{error}</p>}
      {member && (
        <div style={{ display: 'grid', gap: '16px', maxWidth: '640px' }}>
          <div style={{ display: 'flex', gap: '16px', alignItems: 'center' }}>
            <Avatar entity={member} size={88} />
            <div>
              <h2 style={{ margin: 0 }}>{member.username}</h2>
              <p style={{ margin: 0 }}>{member.email}</p>
              <p style={{ margin: 0 }}>{member.jobTitle || 'No job title'}</p>
            </div>
          </div>
          <div>
            <AvatarUpload onUpload={handleAvatarUpload} busy={uploading} />
          </div>
        </div>
      )}
    </div>
  );
};

const CreateTeamMemberPage = () => {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    username: '',
    password: '',
    email: '',
    availabilityStatus: 'AVAILABLE',
    jobTitle: '',
    managerId: ''
  });
  const [avatarFile, setAvatarFile] = useState(null);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const updateField = (field, value) => {
    setForm((current) => ({ ...current, [field]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    try {
      const payload = {
        ...form,
        managerId: form.managerId ? Number(form.managerId) : null
      };

      const response = await authorizedFetch('/api/team-members', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (!response.ok) {
        throw new Error('Failed to create team member');
      }

      const createdMember = await response.json();

      if (avatarFile) {
        const formData = new FormData();
        formData.append('file', avatarFile);
        const avatarResponse = await authorizedFetch(`/api/team-members/${createdMember.id}/avatar`, {
          method: 'POST',
          body: formData
        });
        if (avatarResponse.ok) {
          await avatarResponse.json();
        }
      }

      navigate(`/team-members/${createdMember.id}`);
    } catch (submitError) {
      setError(submitError.message);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div style={{ padding: '20px' }}>
      <h1>Create Team Member</h1>
      {error && <p style={{ color: 'crimson' }}>{error}</p>}
      <form onSubmit={handleSubmit} style={{ display: 'grid', gap: '12px', maxWidth: '480px' }}>
        <input placeholder="Username" value={form.username} onChange={(event) => updateField('username', event.target.value)} />
        <input placeholder="Password" type="password" value={form.password} onChange={(event) => updateField('password', event.target.value)} />
        <input placeholder="Email" type="email" value={form.email} onChange={(event) => updateField('email', event.target.value)} />
        <input placeholder="Job title" value={form.jobTitle} onChange={(event) => updateField('jobTitle', event.target.value)} />
        <select value={form.availabilityStatus} onChange={(event) => updateField('availabilityStatus', event.target.value)}>
          <option value="AVAILABLE">AVAILABLE</option>
          <option value="BUSY">BUSY</option>
          <option value="OFF_DUTY">OFF_DUTY</option>
          <option value="UNAVAILABLE">UNAVAILABLE</option>
        </select>
        <input placeholder="Manager ID" value={form.managerId} onChange={(event) => updateField('managerId', event.target.value)} />
        <input type="file" accept="image/*" onChange={(event) => setAvatarFile(event.target.files?.[0] || null)} />
        <button type="submit" disabled={saving}>{saving ? 'Saving...' : 'Create Member'}</button>
      </form>
    </div>
  );
};

// ==================== Team Manager Pages ====================
const TeamManagersListPage = () => {
  const navigate = useNavigate();
  const [managers, setManagers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const loadManagers = async () => {
      try {
        const response = await authorizedFetch('/api/team-managers');
        if (!response.ok) {
          throw new Error('Failed to load team managers');
        }
        setManagers(await response.json());
      } catch (loadError) {
        setError(loadError.message);
      } finally {
        setLoading(false);
      }
    };

    loadManagers();
  }, []);

  return (
    <div style={{ padding: '20px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h1>Team Managers</h1>
        <button onClick={() => navigate('/team-managers/create')} style={{ padding: '10px 14px' }}>
          New Manager
        </button>
      </div>
      {loading && <p>Loading...</p>}
      {error && <p style={{ color: 'crimson' }}>{error}</p>}
      <div style={{ display: 'grid', gap: '12px' }}>
        {managers.map((manager) => (
          <EntityCard
            key={manager.id}
            entity={manager}
            subtitle={manager.department || 'No department'}
            onClick={() => navigate(`/team-managers/${manager.id}`)}
          />
        ))}
      </div>
    </div>
  );
};

const TeamManagerDetailPage = () => {
  const { id } = useParams();
  const [manager, setManager] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [uploading, setUploading] = useState(false);

  useEffect(() => {
    const loadManager = async () => {
      try {
        const response = await authorizedFetch(`/api/team-managers/${id}`);
        if (!response.ok) {
          throw new Error('Failed to load team manager');
        }
        setManager(await response.json());
      } catch (loadError) {
        setError(loadError.message);
      } finally {
        setLoading(false);
      }
    };

    loadManager();
  }, [id]);

  const handleAvatarUpload = async (file) => {
    setUploading(true);
    setError('');
    try {
      const formData = new FormData();
      formData.append('file', file);
      const response = await authorizedFetch(`/api/team-managers/${id}/avatar`, {
        method: 'POST',
        body: formData
      });

      if (!response.ok) {
        throw new Error('Avatar upload failed');
      }

      setManager(await response.json());
    } catch (uploadError) {
      setError(uploadError.message);
    } finally {
      setUploading(false);
    }
  };

  return (
    <div style={{ padding: '20px' }}>
      <h1>Team Manager Details</h1>
      {loading && <p>Loading...</p>}
      {error && <p style={{ color: 'crimson' }}>{error}</p>}
      {manager && (
        <div style={{ display: 'grid', gap: '16px', maxWidth: '640px' }}>
          <div style={{ display: 'flex', gap: '16px', alignItems: 'center' }}>
            <Avatar entity={manager} size={88} />
            <div>
              <h2 style={{ margin: 0 }}>{manager.username}</h2>
              <p style={{ margin: 0 }}>{manager.email}</p>
              <p style={{ margin: 0 }}>{manager.department || 'No department'}</p>
            </div>
          </div>
          <AvatarUpload onUpload={handleAvatarUpload} busy={uploading} />
        </div>
      )}
    </div>
  );
};

const CreateTeamManagerPage = () => {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    username: '',
    password: '',
    email: '',
    department: ''
  });
  const [avatarFile, setAvatarFile] = useState(null);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const updateField = (field, value) => {
    setForm((current) => ({ ...current, [field]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    try {
      const response = await authorizedFetch('/api/team-managers', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form)
      });

      if (!response.ok) {
        throw new Error('Failed to create team manager');
      }

      const createdManager = await response.json();

      if (avatarFile) {
        const formData = new FormData();
        formData.append('file', avatarFile);
        const avatarResponse = await authorizedFetch(`/api/team-managers/${createdManager.id}/avatar`, {
          method: 'POST',
          body: formData
        });
        if (avatarResponse.ok) {
          await avatarResponse.json();
        }
      }

      navigate(`/team-managers/${createdManager.id}`);
    } catch (submitError) {
      setError(submitError.message);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div style={{ padding: '20px' }}>
      <h1>Create Team Manager</h1>
      {error && <p style={{ color: 'crimson' }}>{error}</p>}
      <form onSubmit={handleSubmit} style={{ display: 'grid', gap: '12px', maxWidth: '480px' }}>
        <input placeholder="Username" value={form.username} onChange={(event) => updateField('username', event.target.value)} />
        <input placeholder="Password" type="password" value={form.password} onChange={(event) => updateField('password', event.target.value)} />
        <input placeholder="Email" type="email" value={form.email} onChange={(event) => updateField('email', event.target.value)} />
        <input placeholder="Department" value={form.department} onChange={(event) => updateField('department', event.target.value)} />
        <input type="file" accept="image/*" onChange={(event) => setAvatarFile(event.target.files?.[0] || null)} />
        <button type="submit" disabled={saving}>{saving ? 'Saving...' : 'Create Manager'}</button>
      </form>
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
