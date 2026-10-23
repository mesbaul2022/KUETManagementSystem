/**
 * KUET Management System - Core Frontend Application
 * Handles JWT Authentication, RBAC UI Enforcement, CRUD Operations, and Live API Logging
 */

const API_BASE = '/api';

const state = {
  token: localStorage.getItem('kuet_token') || null,
  user: null, // { id, username, email, role, fullName }
  activeTab: 'dashboard',
  admins: [],
  teachers: [],
  students: []
};

// ================= API Helper with Automatic JWT and Console Logging =================
async function apiCall(endpoint, options = {}) {
  const url = `${API_BASE}${endpoint}`;
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {})
  };

  if (state.token) {
    headers['Authorization'] = `Bearer ${state.token}`;
  }

  const startTime = performance.now();
  const method = options.method || 'GET';

  try {
    const response = await fetch(url, { ...options, headers });
    const duration = Math.round(performance.now() - startTime);

    let data = null;
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      data = await response.json();
    } else {
      const text = await response.text();
      data = text ? { message: text } : {};
    }

    // Log to the Live API Console
    logToApiConsole(method, endpoint, response.status, data, duration);

    if (response.status === 401) {
      handleUnauthorized();
      throw new Error(data.message || 'Unauthorized: Please log in again.');
    }

    if (response.status === 403) {
      showToast(`🔒 Access Denied (403 Forbidden): Your current role (${state.user ? state.user.role : 'Guest'}) does not have permission to access ${endpoint}.`, 'error');
      throw new Error('Access Denied: Forbidden by Role-Based Access Control');
    }

    if (!response.ok) {
      const errMsg = data.message || `Request failed with status ${response.status}`;
      showToast(errMsg, 'error');
      throw new Error(errMsg);
    }

    return data;
  } catch (err) {
    console.error(`API Error on ${method} ${endpoint}:`, err);
    throw err;
  }
}

// ================= Live API Console Logger =================
function logToApiConsole(method, endpoint, status, data, duration) {
  const consoleEl = document.getElementById('apiConsoleOutput');
  if (!consoleEl) return;

  const entry = document.createElement('div');
  entry.className = 'console-log-entry';

  const time = new Date().toLocaleTimeString();
  const statusClass = `status-${status}`;

  entry.innerHTML = `
    <span style="color:#64748b">[${time}]</span>
    <span class="method-badge method-${method}">${method}</span>
    <span style="color:#f8fafc; font-weight:600">${endpoint}</span>
    <span class="status-badge-code ${statusClass}">${status}</span>
    <span style="color:#64748b; font-size:0.75rem">${duration}ms</span>
    <div style="color:#94a3b8; margin-top:4px; max-height:80px; overflow-y:auto; background:rgba(0,0,0,0.3); padding:4px 8px; border-radius:4px">
      ${JSON.stringify(data)}
    </div>
  `;

  consoleEl.insertBefore(entry, consoleEl.firstChild);
}

function clearApiConsole() {
  const consoleEl = document.getElementById('apiConsoleOutput');
  if (consoleEl) {
    consoleEl.innerHTML = '<div style="color:#64748b; font-style:italic">Console cleared. Waiting for API calls...</div>';
  }
}

// ================= Authentication Functions =================
async function login(username, password) {
  try {
    const res = await apiCall('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password })
    });

    state.token = res.accessToken;
    localStorage.setItem('kuet_token', res.accessToken);
    state.user = {
      id: res.userId,
      username: res.username,
      email: res.email,
      role: res.role,
      fullName: res.fullName
    };

    showToast(`Welcome back, ${res.fullName}! Authenticated as ${res.role}`, 'success');
    closeModal('loginModal');
    updateAuthUI();
    loadDashboardData();
    switchTab(state.activeTab);
  } catch (err) {
    showToast('Invalid credentials. Please check your username and password.', 'error');
  }
}

async function quickLoginAs(role) {
  let u = 'admin', p = 'admin123';
  if (role === 'TEACHER') {
    u = 'teacher_rokib';
    p = 'teacher123';
  } else if (role === 'STUDENT') {
    u = 'student_tanvir';
    p = 'student123';
  }
  await login(u, p);
}

async function logout() {
  try {
    if (state.token) {
      await apiCall('/auth/logout', { method: 'POST' });
    }
  } catch (e) {
    // Ignore network error on logout
  } finally {
    state.token = null;
    state.user = null;
    localStorage.removeItem('kuet_token');
    showToast('Logged out successfully.', 'info');
    updateAuthUI();
    switchTab('dashboard');
  }
}

function handleUnauthorized() {
  state.token = null;
  state.user = null;
  localStorage.removeItem('kuet_token');
  updateAuthUI();
}

async function checkSession() {
  if (!state.token) {
    // If not logged in, auto-login as admin by default for best demo experience
    await quickLoginAs('ADMIN');
    return;
  }

  try {
    const me = await apiCall('/auth/me');
    state.user = {
      id: me.userId,
      username: me.username,
      email: me.email,
      role: me.role,
      fullName: me.fullName
    };
    updateAuthUI();
    loadDashboardData();
  } catch (err) {
    state.token = null;
    localStorage.removeItem('kuet_token');
    await quickLoginAs('ADMIN');
  }
}

// ================= UI Updates & RBAC Navigation =================
function updateAuthUI() {
  const profileContainer = document.getElementById('userProfileArea');
  const quickBtns = document.querySelectorAll('.btn-quick');

  quickBtns.forEach(btn => btn.classList.remove('active'));

  if (state.user) {
    const role = state.user.role;
    if (role === 'ROLE_ADMIN') document.getElementById('btnRoleAdmin')?.classList.add('active');
    if (role === 'ROLE_TEACHER') document.getElementById('btnRoleTeacher')?.classList.add('active');
    if (role === 'ROLE_STUDENT') document.getElementById('btnRoleStudent')?.classList.add('active');

    profileContainer.innerHTML = `
      <div class="user-profile-badge">
        <div class="user-avatar">${state.user.fullName.charAt(0)}</div>
        <div>
          <div style="font-weight:600">${escapeHtml(state.user.fullName)}</div>
          <span class="role-tag ${role}">${role.replace('ROLE_', '')}</span>
        </div>
      </div>
      <button class="btn btn-secondary btn-sm" onclick="logout()">Logout</button>
    `;
  } else {
    profileContainer.innerHTML = `
      <button class="btn btn-primary btn-sm" onclick="openModal('loginModal')">Sign In</button>
    `;
  }

  // Update lock badges on navigation tabs according to RBAC rules
  updateNavigationLocks();
}

function updateNavigationLocks() {
  const role = state.user ? state.user.role : null;

  const adminNav = document.getElementById('navAdmins');
  const teacherNav = document.getElementById('navTeachers');
  const studentNav = document.getElementById('navStudents');

  if (adminNav) {
    const isLocked = role !== 'ROLE_ADMIN';
    adminNav.querySelector('.nav-lock-badge').style.display = isLocked ? 'inline-block' : 'none';
  }

  if (teacherNav) {
    const isLocked = !(role === 'ROLE_ADMIN' || role === 'ROLE_TEACHER');
    teacherNav.querySelector('.nav-lock-badge').style.display = isLocked ? 'inline-block' : 'none';
  }

  if (studentNav) {
    const isLocked = !(role === 'ROLE_ADMIN' || role === 'ROLE_STUDENT');
    studentNav.querySelector('.nav-lock-badge').style.display = isLocked ? 'inline-block' : 'none';
  }
}

function switchTab(tabId) {
  state.activeTab = tabId;

  // Update nav link active classes
  document.querySelectorAll('.nav-item').forEach(el => el.classList.remove('active'));
  document.getElementById(`nav${capitalize(tabId)}`)?.classList.add('active');

  // Hide all sections
  document.querySelectorAll('.tab-section').forEach(el => el.style.display = 'none');

  // Show active section
  const activeSection = document.getElementById(`section${capitalize(tabId)}`);
  if (activeSection) {
    activeSection.style.display = 'block';
  }

  // Load section-specific data
  if (tabId === 'dashboard') loadDashboardData();
  if (tabId === 'admins') loadAdmins();
  if (tabId === 'teachers') loadTeachers();
  if (tabId === 'students') loadStudents();
}

// ================= Data Loaders =================
async function loadDashboardData() {
  try {
    const role = state.user ? state.user.role : '';

    // Load available resources based on permissions
    if (role === 'ROLE_ADMIN') {
      const [admins, teachers, students] = await Promise.all([
        apiCall('/admins').catch(() => []),
        apiCall('/teachers').catch(() => []),
        apiCall('/students').catch(() => [])
      ]);
      state.admins = admins;
      state.teachers = teachers;
      state.students = students;
      document.getElementById('statAdmins').textContent = admins.length;
      document.getElementById('statTeachers').textContent = teachers.length;
      document.getElementById('statStudents').textContent = students.length;
    } else if (role === 'ROLE_TEACHER') {
      const teachers = await apiCall('/teachers').catch(() => []);
      state.teachers = teachers;
      document.getElementById('statAdmins').textContent = '🔒 Locked';
      document.getElementById('statTeachers').textContent = teachers.length;
      document.getElementById('statStudents').textContent = '🔒 Locked';
    } else if (role === 'ROLE_STUDENT') {
      const students = await apiCall('/students').catch(() => []);
      state.students = students;
      document.getElementById('statAdmins').textContent = '🔒 Locked';
      document.getElementById('statTeachers').textContent = '🔒 Locked';
      document.getElementById('statStudents').textContent = students.length;
    }

    renderActiveUserPermissions();
  } catch (err) {
    console.error('Error loading dashboard:', err);
  }
}

function renderActiveUserPermissions() {
  const container = document.getElementById('userPermissionsMatrix');
  if (!container) return;

  const role = state.user ? state.user.role : 'GUEST';

  let adminAccess = role === 'ROLE_ADMIN' ? '<span class="perm-allow">✅ Full Access (CRUD)</span>' : '<span class="perm-deny">❌ Forbidden (403)</span>';
  let teacherAccess = (role === 'ROLE_ADMIN' || role === 'ROLE_TEACHER') ? '<span class="perm-allow">✅ Full Access (CRUD)</span>' : '<span class="perm-deny">❌ Forbidden (403)</span>';
  let studentAccess = (role === 'ROLE_ADMIN' || role === 'ROLE_STUDENT') ? '<span class="perm-allow">✅ Full Access (CRUD)</span>' : '<span class="perm-deny">❌ Forbidden (403)</span>';

  container.innerHTML = `
    <tr>
      <td><strong>/api/admins/**</strong> (Admin Resources)</td>
      <td>${adminAccess}</td>
      <td>Restricted strictly to ROLE_ADMIN</td>
    </tr>
    <tr>
      <td><strong>/api/teachers/**</strong> (Teacher Resources)</td>
      <td>${teacherAccess}</td>
      <td>Accessible by ROLE_ADMIN and ROLE_TEACHER</td>
    </tr>
    <tr>
      <td><strong>/api/students/**</strong> (Student Resources)</td>
      <td>${studentAccess}</td>
      <td>Accessible by ROLE_ADMIN and ROLE_STUDENT</td>
    </tr>
  `;
}

// ================= Admin Management CRUD =================
async function loadAdmins() {
  const tableBody = document.getElementById('adminsTableBody');
  const lockedWarning = document.getElementById('adminsLockedWarning');

  if (state.user && state.user.role !== 'ROLE_ADMIN') {
    if (lockedWarning) lockedWarning.style.display = 'flex';
    if (tableBody) tableBody.innerHTML = '';
    return;
  }

  if (lockedWarning) lockedWarning.style.display = 'none';

  try {
    const admins = await apiCall('/admins');
    state.admins = admins;
    renderAdminsTable(admins);
  } catch (err) {
    if (tableBody) tableBody.innerHTML = `<tr><td colspan="6" style="text-align:center; color:#f87171">Failed to load admin records: ${err.message}</td></tr>`;
  }
}

function renderAdminsTable(admins) {
  const tableBody = document.getElementById('adminsTableBody');
  if (!tableBody) return;

  if (admins.length === 0) {
    tableBody.innerHTML = `<tr><td colspan="6" style="text-align:center; color:#94a3b8">No administrators found.</td></tr>`;
    return;
  }

  tableBody.innerHTML = admins.map(a => `
    <tr>
      <td>
        <div class="user-cell">
          <div class="user-avatar" style="background:#ec4899">${escapeHtml(a.fullName.charAt(0))}</div>
          <div>
            <div class="name">${escapeHtml(a.fullName)}</div>
            <div class="sub">@${escapeHtml(a.username)} • ID: ${escapeHtml(a.employeeId)}</div>
          </div>
        </div>
      </td>
      <td><span class="pill-badge">${escapeHtml(a.department)}</span></td>
      <td>${escapeHtml(a.designation)}</td>
      <td>${escapeHtml(a.email)}</td>
      <td>${escapeHtml(a.phone || 'N/A')}</td>
      <td>
        <div class="action-buttons">
          <button class="btn btn-secondary btn-sm" onclick="openEditAdminModal(${a.id})">Edit</button>
          <button class="btn btn-danger btn-sm" onclick="confirmDelete('admin', ${a.id}, '${escapeHtml(a.fullName)}')">Delete</button>
        </div>
      </td>
    </tr>
  `).join('');
}

// ================= Teacher Management CRUD =================
async function loadTeachers() {
  const tableBody = document.getElementById('teachersTableBody');
  const lockedWarning = document.getElementById('teachersLockedWarning');

  const canAccess = state.user && (state.user.role === 'ROLE_ADMIN' || state.user.role === 'ROLE_TEACHER');
  if (!canAccess) {
    if (lockedWarning) lockedWarning.style.display = 'flex';
    if (tableBody) tableBody.innerHTML = '';
    return;
  }

  if (lockedWarning) lockedWarning.style.display = 'none';

  try {
    const teachers = await apiCall('/teachers');
    state.teachers = teachers;
    renderTeachersTable(teachers);
  } catch (err) {
    if (tableBody) tableBody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:#f87171">Failed to load teacher records: ${err.message}</td></tr>`;
  }
}

function renderTeachersTable(teachers) {
  const tableBody = document.getElementById('teachersTableBody');
  if (!tableBody) return;

  if (teachers.length === 0) {
    tableBody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:#94a3b8">No teachers found.</td></tr>`;
    return;
  }

  tableBody.innerHTML = teachers.map(t => `
    <tr>
      <td>
        <div class="user-cell">
          <div class="user-avatar" style="background:#06b6d4">${escapeHtml(t.fullName.charAt(0))}</div>
          <div>
            <div class="name">${escapeHtml(t.fullName)}</div>
            <div class="sub">@${escapeHtml(t.username)} • ID: ${escapeHtml(t.teacherId)}</div>
          </div>
        </div>
      </td>
      <td><span class="pill-badge">${escapeHtml(t.department)}</span></td>
      <td>${escapeHtml(t.designation)}</td>
      <td><span style="font-size:0.8rem; color:#94a3b8">${escapeHtml(t.specialization || 'N/A')}</span></td>
      <td>${escapeHtml(t.email)}</td>
      <td>${escapeHtml(t.phone || 'N/A')}</td>
      <td>
        <div class="action-buttons">
          <button class="btn btn-secondary btn-sm" onclick="openEditTeacherModal(${t.id})">Edit</button>
          <button class="btn btn-danger btn-sm" onclick="confirmDelete('teacher', ${t.id}, '${escapeHtml(t.fullName)}')">Delete</button>
        </div>
      </td>
    </tr>
  `).join('');
}

// ================= Student Management CRUD =================
async function loadStudents() {
  const tableBody = document.getElementById('studentsTableBody');
  const lockedWarning = document.getElementById('studentsLockedWarning');

  const canAccess = state.user && (state.user.role === 'ROLE_ADMIN' || state.user.role === 'ROLE_STUDENT');
  if (!canAccess) {
    if (lockedWarning) lockedWarning.style.display = 'flex';
    if (tableBody) tableBody.innerHTML = '';
    return;
  }

  if (lockedWarning) lockedWarning.style.display = 'none';

  try {
    const students = await apiCall('/students');
    state.students = students;
    renderStudentsTable(students);
  } catch (err) {
    if (tableBody) tableBody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:#f87171">Failed to load student records: ${err.message}</td></tr>`;
  }
}

function renderStudentsTable(students) {
  const tableBody = document.getElementById('studentsTableBody');
  if (!tableBody) return;

  if (students.length === 0) {
    tableBody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:#94a3b8">No students found.</td></tr>`;
    return;
  }

  tableBody.innerHTML = students.map(s => `
    <tr>
      <td>
        <div class="user-cell">
          <div class="user-avatar" style="background:#10b981">${escapeHtml(s.fullName.charAt(0))}</div>
          <div>
            <div class="name">${escapeHtml(s.fullName)}</div>
            <div class="sub">@${escapeHtml(s.username)} • Roll: ${escapeHtml(s.rollNumber)}</div>
          </div>
        </div>
      </td>
      <td><span class="pill-badge">${escapeHtml(s.department)}</span></td>
      <td><span class="pill-badge" style="background:rgba(229,178,62,0.15); color:var(--kuet-gold)">${escapeHtml(s.series)}</span></td>
      <td><span class="cgpa-badge">${s.cgpa !== null && s.cgpa !== undefined ? s.cgpa.toFixed(2) : 'N/A'}</span></td>
      <td>${escapeHtml(s.email)}</td>
      <td>${escapeHtml(s.phone || 'N/A')}</td>
      <td>
        <div class="action-buttons">
          <button class="btn btn-secondary btn-sm" onclick="openEditStudentModal(${s.id})">Edit</button>
          <button class="btn btn-danger btn-sm" onclick="confirmDelete('student', ${s.id}, '${escapeHtml(s.fullName)}')">Delete</button>
        </div>
      </td>
    </tr>
  `).join('');
}

// ================= Search / Filters =================
function filterTable(type, query) {
  const q = query.toLowerCase().trim();
  if (type === 'admin') {
    const filtered = state.admins.filter(a =>
      a.fullName.toLowerCase().includes(q) ||
      a.employeeId.toLowerCase().includes(q) ||
      a.department.toLowerCase().includes(q) ||
      a.email.toLowerCase().includes(q)
    );
    renderAdminsTable(filtered);
  } else if (type === 'teacher') {
    const filtered = state.teachers.filter(t =>
      t.fullName.toLowerCase().includes(q) ||
      t.teacherId.toLowerCase().includes(q) ||
      t.department.toLowerCase().includes(q) ||
      t.email.toLowerCase().includes(q)
    );
    renderTeachersTable(filtered);
  } else if (type === 'student') {
    const filtered = state.students.filter(s =>
      s.fullName.toLowerCase().includes(q) ||
      s.rollNumber.toLowerCase().includes(q) ||
      s.department.toLowerCase().includes(q) ||
      s.series.toLowerCase().includes(q)
    );
    renderStudentsTable(filtered);
  }
}

// ================= Modal & Form Actions =================
function openModal(modalId) {
  document.getElementById(modalId)?.classList.add('active');
}

function closeModal(modalId) {
  document.getElementById(modalId)?.classList.remove('active');
}

function openCreateAdminModal() {
  document.getElementById('adminModalTitle').textContent = 'Add New Administrator';
  document.getElementById('adminId').value = '';
  document.getElementById('adminUsernameGroup').style.display = 'block';
  document.getElementById('adminPasswordHelp').textContent = 'Required (at least 6 characters)';
  document.getElementById('adminForm').reset();
  openModal('adminModal');
}

function openEditAdminModal(id) {
  const a = state.admins.find(item => item.id === id);
  if (!a) return;

  document.getElementById('adminModalTitle').textContent = `Edit Administrator (${a.fullName})`;
  document.getElementById('adminId').value = a.id;
  document.getElementById('adminUsernameGroup').style.display = 'none';
  document.getElementById('adminPasswordHelp').textContent = 'Leave blank to keep existing password';

  document.getElementById('adminFullName').value = a.fullName;
  document.getElementById('adminEmployeeId').value = a.employeeId;
  document.getElementById('adminDepartment').value = a.department;
  document.getElementById('adminDesignation').value = a.designation;
  document.getElementById('adminEmail').value = a.email;
  document.getElementById('adminPhone').value = a.phone || '';
  document.getElementById('adminPassword').value = '';

  openModal('adminModal');
}

function openCreateTeacherModal() {
  document.getElementById('teacherModalTitle').textContent = 'Add Faculty Member (Teacher)';
  document.getElementById('teacherId').value = '';
  document.getElementById('teacherUsernameGroup').style.display = 'block';
  document.getElementById('teacherPasswordHelp').textContent = 'Required (at least 6 characters)';
  document.getElementById('teacherForm').reset();
  openModal('teacherModal');
}

function openEditTeacherModal(id) {
  const t = state.teachers.find(item => item.id === id);
  if (!t) return;

  document.getElementById('teacherModalTitle').textContent = `Edit Faculty Member (${t.fullName})`;
  document.getElementById('teacherId').value = t.id;
  document.getElementById('teacherUsernameGroup').style.display = 'none';
  document.getElementById('teacherPasswordHelp').textContent = 'Leave blank to keep existing password';

  document.getElementById('teacherFullName').value = t.fullName;
  document.getElementById('teacherEmployeeId').value = t.teacherId;
  document.getElementById('teacherDepartment').value = t.department;
  document.getElementById('teacherDesignation').value = t.designation;
  document.getElementById('teacherSpecialization').value = t.specialization || '';
  document.getElementById('teacherEmail').value = t.email;
  document.getElementById('teacherPhone').value = t.phone || '';
  document.getElementById('teacherPassword').value = '';

  openModal('teacherModal');
}

function openCreateStudentModal() {
  document.getElementById('studentModalTitle').textContent = 'Register New Student';
  document.getElementById('studentId').value = '';
  document.getElementById('studentUsernameGroup').style.display = 'block';
  document.getElementById('studentPasswordHelp').textContent = 'Required (at least 6 characters)';
  document.getElementById('studentForm').reset();
  openModal('studentModal');
}

function openEditStudentModal(id) {
  const s = state.students.find(item => item.id === id);
  if (!s) return;

  document.getElementById('studentModalTitle').textContent = `Edit Student (${s.fullName})`;
  document.getElementById('studentId').value = s.id;
  document.getElementById('studentUsernameGroup').style.display = 'none';
  document.getElementById('studentPasswordHelp').textContent = 'Leave blank to keep existing password';

  document.getElementById('studentFullName').value = s.fullName;
  document.getElementById('studentRollNumber').value = s.rollNumber;
  document.getElementById('studentDepartment').value = s.department;
  document.getElementById('studentSeries').value = s.series;
  document.getElementById('studentCgpa').value = s.cgpa || '';
  document.getElementById('studentEmail').value = s.email;
  document.getElementById('studentPhone').value = s.phone || '';
  document.getElementById('studentPassword').value = '';

  openModal('studentModal');
}

// ================= Form Submissions =================
async function submitAdminForm(e) {
  e.preventDefault();
  const id = document.getElementById('adminId').value;
  const isEdit = Boolean(id);

  const payload = {
    fullName: document.getElementById('adminFullName').value,
    employeeId: document.getElementById('adminEmployeeId').value,
    department: document.getElementById('adminDepartment').value,
    designation: document.getElementById('adminDesignation').value,
    email: document.getElementById('adminEmail').value,
    phone: document.getElementById('adminPhone').value
  };

  const password = document.getElementById('adminPassword').value;
  if (password) payload.password = password;

  if (!isEdit) {
    payload.username = document.getElementById('adminUsername').value;
    if (!password) {
      showToast('Password is required for new accounts', 'error');
      return;
    }
  }

  try {
    if (isEdit) {
      await apiCall(`/admins/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
      showToast('Administrator updated successfully!', 'success');
    } else {
      await apiCall('/admins', { method: 'POST', body: JSON.stringify(payload) });
      showToast('Administrator created successfully!', 'success');
    }
    closeModal('adminModal');
    loadAdmins();
  } catch (err) {
    // Handled by apiCall
  }
}

async function submitTeacherForm(e) {
  e.preventDefault();
  const id = document.getElementById('teacherId').value;
  const isEdit = Boolean(id);

  const payload = {
    fullName: document.getElementById('teacherFullName').value,
    teacherId: document.getElementById('teacherEmployeeId').value,
    department: document.getElementById('teacherDepartment').value,
    designation: document.getElementById('teacherDesignation').value,
    specialization: document.getElementById('teacherSpecialization').value,
    email: document.getElementById('teacherEmail').value,
    phone: document.getElementById('teacherPhone').value
  };

  const password = document.getElementById('teacherPassword').value;
  if (password) payload.password = password;

  if (!isEdit) {
    payload.username = document.getElementById('teacherUsername').value;
    if (!password) {
      showToast('Password is required for new accounts', 'error');
      return;
    }
  }

  try {
    if (isEdit) {
      await apiCall(`/teachers/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
      showToast('Teacher record updated successfully!', 'success');
    } else {
      await apiCall('/teachers', { method: 'POST', body: JSON.stringify(payload) });
      showToast('Teacher created successfully!', 'success');
    }
    closeModal('teacherModal');
    loadTeachers();
  } catch (err) {
    // Handled by apiCall
  }
}

async function submitStudentForm(e) {
  e.preventDefault();
  const id = document.getElementById('studentId').value;
  const isEdit = Boolean(id);

  const payload = {
    fullName: document.getElementById('studentFullName').value,
    rollNumber: document.getElementById('studentRollNumber').value,
    department: document.getElementById('studentDepartment').value,
    series: document.getElementById('studentSeries').value,
    cgpa: parseFloat(document.getElementById('studentCgpa').value) || 0.0,
    email: document.getElementById('studentEmail').value,
    phone: document.getElementById('studentPhone').value
  };

  const password = document.getElementById('studentPassword').value;
  if (password) payload.password = password;

  if (!isEdit) {
    payload.username = document.getElementById('studentUsername').value;
    if (!password) {
      showToast('Password is required for new accounts', 'error');
      return;
    }
  }

  try {
    if (isEdit) {
      await apiCall(`/students/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
      showToast('Student record updated successfully!', 'success');
    } else {
      await apiCall('/students', { method: 'POST', body: JSON.stringify(payload) });
      showToast('Student registered successfully!', 'success');
    }
    closeModal('studentModal');
    loadStudents();
  } catch (err) {
    // Handled by apiCall
  }
}

// ================= Deletion Flow =================
let pendingDelete = null;

function confirmDelete(type, id, name) {
  pendingDelete = { type, id, name };
  document.getElementById('deleteConfirmMessage').textContent =
    `Are you sure you want to delete ${capitalize(type)} record "${name}"? This action is irreversible.`;
  openModal('deleteConfirmModal');
}

async function executeDelete() {
  if (!pendingDelete) return;
  const { type, id } = pendingDelete;

  try {
    await apiCall(`/${type}s/${id}`, { method: 'DELETE' });
    showToast(`${capitalize(type)} deleted successfully!`, 'success');
    closeModal('deleteConfirmModal');

    if (type === 'admin') loadAdmins();
    if (type === 'teacher') loadTeachers();
    if (type === 'student') loadStudents();
  } catch (err) {
    // Handled by apiCall
  } finally {
    pendingDelete = null;
  }
}

// ================= Live REST API Sandbox / Tester =================
async function executeCustomApiTest() {
  const method = document.getElementById('testApiMethod').value;
  const endpoint = document.getElementById('testApiEndpoint').value;
  const bodyText = document.getElementById('testApiBody').value;
  const outputEl = document.getElementById('testApiResult');

  outputEl.textContent = 'Executing request...';

  try {
    const options = { method };
    if (method !== 'GET' && bodyText.trim()) {
      options.body = bodyText;
    }

    const res = await apiCall(endpoint, options);
    outputEl.textContent = JSON.stringify(res, null, 2);
  } catch (err) {
    outputEl.textContent = `Error: ${err.message}`;
  }
}

// ================= Toast Notification System =================
function showToast(message, type = 'info') {
  const container = document.getElementById('toastContainer');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className = `toast ${type}`;

  const icon = type === 'success' ? '✅' : type === 'error' ? '❌' : type === 'warning' ? '⚠️' : 'ℹ️';

  toast.innerHTML = `
    <div style="display:flex; align-items:center; gap:0.6rem">
      <span>${icon}</span>
      <span>${escapeHtml(message)}</span>
    </div>
    <button style="background:none; border:none; color:#94a3b8; cursor:pointer" onclick="this.parentElement.remove()">✕</button>
  `;

  container.appendChild(toast);
  setTimeout(() => {
    toast.remove();
  }, 4500);
}

// ================= Utilities =================
function capitalize(str) {
  if (!str) return '';
  return str.charAt(0).toUpperCase() + str.slice(1);
}

function escapeHtml(text) {
  if (text === null || text === undefined) return '';
  return String(text)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

// ================= App Initialization =================
document.addEventListener('DOMContentLoaded', () => {
  // Setup form event listeners
  document.getElementById('adminForm')?.addEventListener('submit', submitAdminForm);
  document.getElementById('teacherForm')?.addEventListener('submit', submitTeacherForm);
  document.getElementById('studentForm')?.addEventListener('submit', submitStudentForm);
  document.getElementById('customLoginForm')?.addEventListener('submit', (e) => {
    e.preventDefault();
    login(document.getElementById('loginUsername').value, document.getElementById('loginPassword').value);
  });

  // Check active session or auto-login with default demo credentials
  checkSession();
});
