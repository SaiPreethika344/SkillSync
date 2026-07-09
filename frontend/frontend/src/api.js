export const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'
export const BACKEND_BASE_URL = API_BASE_URL.replace(/\/api\/?$/, '')

const getToken = () => localStorage.getItem('token')

const getAuthHeaders = () => {
  const token = getToken()
  return token ? { 'Authorization': `Bearer ${token}` } : {}
}

const headers = () => ({
  'Content-Type': 'application/json',
  ...getAuthHeaders()
})

export const registerUser = (name, email, password) =>
  fetch(`${API_BASE_URL}/auth/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name, email, password })
  }).then(r => r.json())

export const loginUser = (email, password) =>
  fetch(`${API_BASE_URL}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  }).then(r => r.json())

export const runAnalysis = (field, skills) =>
  fetch(`${API_BASE_URL}/analysis/run`, {
    method: 'POST',
    headers: headers(),
    body: JSON.stringify({ field, skills })
  }).then(r => r.json())

export const uploadResume = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return fetch(`${API_BASE_URL}/analysis/upload-resume`, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: formData
  }).then(r => r.json())
}

export const getDashboard = () =>
  fetch(`${API_BASE_URL}/dashboard`, { headers: headers() }).then(r => r.json())

export const getRoadmap = () =>
  fetch(`${API_BASE_URL}/roadmap`, { headers: headers() }).then(r => r.json())

export const completeStep = (id) =>
  fetch(`${API_BASE_URL}/roadmap/${id}/complete`, {
    method: 'PUT',
    headers: headers()
  }).then(r => r.json())

export const undoStep = (id) =>
  fetch(`${API_BASE_URL}/roadmap/${id}/undo`, {
    method: 'PUT',
    headers: headers()
  }).then(r => r.json())

export const getJobs = () =>
  fetch(`${API_BASE_URL}/jobs`, { headers: headers() }).then(r => r.json())

export const sendChatMessage = (message, context) =>
  fetch(`${API_BASE_URL}/chat`, {
    method: 'POST',
    headers: headers(),
    body: JSON.stringify({ message, context })
  }).then(r => r.json())

export const upgradeUser = () =>
  fetch(`${API_BASE_URL}/user/upgrade`, {
    method: 'POST',
    headers: headers()
  }).then(r => r.json())
