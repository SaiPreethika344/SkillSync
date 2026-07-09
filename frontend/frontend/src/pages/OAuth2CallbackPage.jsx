import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'

export default function OAuth2CallbackPage() {
  const navigate = useNavigate()

  useEffect(() => {
    const queryParams = new URLSearchParams(window.location.search)
    const hashParams = new URLSearchParams(window.location.hash.replace(/^#/, ''))
    const token = queryParams.get('token') || hashParams.get('token')
    const isPremium = queryParams.get('isPremium') || hashParams.get('isPremium') || 'false'

    if (token) {
      localStorage.setItem('token', token)
      localStorage.setItem('isPremium', isPremium)

      const pendingField = localStorage.getItem('pendingField')
      const pendingSkills = localStorage.getItem('pendingSkills')

      if (pendingField && pendingSkills) {
        localStorage.removeItem('pendingField')
        localStorage.removeItem('pendingSkills')
        const skills = JSON.parse(pendingSkills)
        fetch('http://localhost:8080/api/analysis/run', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          },
          body: JSON.stringify({ field: pendingField, skills })
        })
          .then(r => r.json())
          .then(results => {
            navigate('/results', { state: { results } })
          })
          .catch(() => navigate('/dashboard'))
      } else {
        navigate('/dashboard')
      }
    } else {
      navigate('/login')
    }
  }, [navigate])

  return (
    <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#f9f9f9' }}>
      <p style={{ fontSize: 16, color: '#666' }}>Signing you in...</p>
    </div>
  )
}
