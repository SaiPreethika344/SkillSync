import { useState, useEffect } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import useWindowWidth from '../hooks/useWindowWidth'
import Navbar from '../components/Navbar'
import { loginUser, runAnalysis } from '../api'

export default function LoginPage() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()
  const width = useWindowWidth()
  const isMobile = width < 768

  useEffect(() => {
    const token = localStorage.getItem('token')
    if (token) {
      navigate('/dashboard')
    }
  }, [navigate])

  const handleLogin = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const res = await loginUser(email, password)
      if (res.token) {
        localStorage.setItem('token', res.token)
        if (res.isPremium) localStorage.setItem('isPremium', 'true')
        else localStorage.setItem('isPremium', 'false')

        const pendingField = localStorage.getItem('pendingField')
        const pendingSkills = localStorage.getItem('pendingSkills')

        if (pendingField && pendingSkills) {
          localStorage.removeItem('pendingField')
          localStorage.removeItem('pendingSkills')
          const skills = JSON.parse(pendingSkills)
          try {
            const results = await runAnalysis(pendingField, skills)
            navigate('/results', { state: { results } })
          } catch {
            navigate('/dashboard')
          }
        } else {
          navigate('/dashboard')
        }
      } else {
        setError(res.message || 'Invalid email or password. Please try again.')
      }
    } catch (err) {
      if (err.message && err.message.includes('401')) {
        setError('Invalid email or password. Please try again.')
      } else if (err.message && err.message.includes('404')) {
        setError('Account not found. Please sign up first.')
      } else if (err.message && err.message.includes('Failed to fetch')) {
        setError('Cannot connect to server. Please try again later.')
      } else {
        setError('Invalid email or password. Please try again.')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{minHeight:'100vh', background:'#f9f9f9'}}>
      <Navbar />
      <div style={{maxWidth:440, width:'100%', margin:'0 auto', padding: isMobile ? '80px 16px 60px' : '100px 24px 80px'}}>
        <div style={{background:'white', borderRadius:20, border:'1px solid #eee', padding: isMobile ? 24 : 40}}>
          <h1 style={{fontSize: isMobile ? 22 : 26, fontWeight:700, color:'#111', marginBottom:4, textAlign: isMobile ? 'center' : 'left'}}>Welcome back</h1>
          <p style={{color:'#666', fontSize:14, marginBottom: isMobile ? 24 : 32, textAlign: isMobile ? 'center' : 'left'}}>Log in to access your career dashboard.</p>

          {error && (
            <div style={{background:'#FFF1F2', border:'1px solid #FDA4AF', borderRadius:10, padding:'12px 16px', marginBottom:16}}>
              <p style={{color:'#E11D48', fontSize:13, fontWeight:500, margin:0}}>⚠️ {error}</p>
            </div>
          )}

          <div style={{display:'flex', flexDirection:'column', gap:16}}>
            <div>
              <label style={{display:'block', fontSize:13, fontWeight:500, color:'#333', marginBottom:6}}>Email</label>
              <input
                type="email"
                value={email}
                onChange={e => setEmail(e.target.value)}
                onKeyDown={e => e.key === 'Enter' && handleLogin(e)}
                placeholder="you@example.com"
                style={{width:'100%', height: isMobile ? 48 : 'auto', border:'1px solid #eee', borderRadius:10, padding:'12px 16px', fontSize:14, outline:'none', boxSizing:'border-box'}}
              />
            </div>
            <div>
              <label style={{display:'block', fontSize:13, fontWeight:500, color:'#333', marginBottom:6}}>Password</label>
              <input
                type="password"
                value={password}
                onChange={e => setPassword(e.target.value)}
                onKeyDown={e => e.key === 'Enter' && handleLogin(e)}
                placeholder="••••••••"
                style={{width:'100%', height: isMobile ? 48 : 'auto', border:'1px solid #eee', borderRadius:10, padding:'12px 16px', fontSize:14, outline:'none', boxSizing:'border-box'}}
              />
            </div>
            <p style={{textAlign:'right', marginTop:4}}>
              <Link to="/forgot-password" style={{fontSize:13, color:'#185FA5', textDecoration:'none'}}>Forgot password?</Link>
            </p>
            <button
              onClick={handleLogin}
              disabled={loading}
              style={{width:'100%', background:'#185FA5', color:'white', border:'none', padding:'14px', borderRadius:12, fontSize:14, fontWeight:600, cursor: loading ? 'not-allowed' : 'pointer', opacity: loading ? 0.7 : 1}}>
              {loading ? 'Logging in...' : 'Log in'}
            </button>
          </div>

          <p style={{textAlign:'center', fontSize:13, color:'#666', marginTop:24}}>
            Don't have an account? <Link to="/signup" style={{color:'#185FA5', fontWeight:500, textDecoration:'none'}}>Sign up free</Link>
          </p>
        </div>
      </div>
    </div>
  )
}
