import { useNavigate, useLocation } from 'react-router-dom'
import { useEffect, useState } from 'react'
import useWindowWidth from '../hooks/useWindowWidth'
import Navbar from '../components/Navbar'
import { API_BASE_URL } from '../api'

export default function ResultsPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const [careers, setCareers] = useState([])
  const [loading, setLoading] = useState(true)
  const [isLoggedIn, setIsLoggedIn] = useState(false)
  const width = useWindowWidth()
  const isMobile = width < 768

  useEffect(() => {
    const token = localStorage.getItem('token')

    if (location.state?.results?.careerMatches) {
      const filtered = location.state.results.careerMatches
        .filter(c => c.matchPercentage > 0)
        .slice(0, 5)
      setCareers(filtered)
      setIsLoggedIn(true)
      setLoading(false)
      return
    }

    if (!token) {
      setIsLoggedIn(false)
      setLoading(false)
      return
    }

    // Token exists — verify it works
    fetch(`${API_BASE_URL}/dashboard`, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    })
    .then(r => {
      if (r.status === 401) {
        // Token expired — clear and show login
        localStorage.clear()
        setIsLoggedIn(false)
        setLoading(false)
        return null
      }
      return r.json()
    })
    .then(dash => {
      if (!dash) return
      setIsLoggedIn(true)
      if (dash?.topCareerMatches?.length) {
        setCareers(dash.topCareerMatches.slice(0, 5))
      }
      setLoading(false)
    })
    .catch(() => {
      setIsLoggedIn(false)
      setLoading(false)
    })
  }, [])

  if (loading) return (
    <div style={{minHeight:'100vh', display:'flex', alignItems:'center', justifyContent:'center', background:'#f9f9f9'}}>
      <p style={{color:'#666', fontSize:16}}>Loading your results...</p>
    </div>
  )

  return (
    <div style={{minHeight:'100vh', background:'#f9f9f9'}}>
      <Navbar />
      <div style={{maxWidth:700, margin:'0 auto', padding: isMobile ? '90px 16px 60px' : '100px 24px 80px'}}>

        <div style={{textAlign:'center', marginBottom:40}}>
          <span style={{display:'inline-block', background:'#d1fae5', color:'#065f46', fontSize:12, fontWeight:500, padding:'4px 12px', borderRadius:20, marginBottom:16}}>Analysis complete</span>
          <h1 style={{fontSize: isMobile ? 24 : 34, fontWeight:700, color:'#111', marginBottom:8}}>Your career matches are ready</h1>
          <p style={{color:'#666', fontSize:16}}>
            {!isLoggedIn ? 'Log in to see your personalized career matches.' : 'Here are your top career matches based on your skills.'}
          </p>
        </div>

        {/* NOT LOGGED IN */}
        {!isLoggedIn && (
          <>
            <div style={{background:'white', borderRadius:24, border:'1px solid #eee', padding: isMobile ? 24 : 48, textAlign:'center', marginBottom:24, boxShadow:'0 4px 24px rgba(24,95,165,0.08)'}}>
              <div style={{width:72, height:72, background:'#E6F1FB', borderRadius:20, display:'flex', alignItems:'center', justifyContent:'center', margin:'0 auto 20px', fontSize:32}}>🔒</div>
              <h2 style={{fontSize:22, fontWeight:700, color:'#111', marginBottom:8}}>Log in to see your results</h2>
              <p style={{color:'#666', fontSize:15, marginBottom:8}}>Your career matches are ready — sign in to unlock them.</p>
              <p style={{color:'#999', fontSize:13, marginBottom:32}}>Takes 30 seconds — free forever.</p>
              <div style={{display:'flex', gap:12, justifyContent:'center', flexWrap:'wrap'}}>
                <button
                  onClick={() => navigate('/login')}
                  style={{background:'#185FA5', color:'white', border:'none', padding:'14px 36px', borderRadius:12, fontWeight:600, fontSize:15, cursor:'pointer', boxShadow:'0 4px 12px rgba(24,95,165,0.3)'}}>
                  Log in for results
                </button>
                <button
                  onClick={() => navigate('/signup')}
                  style={{background:'white', color:'#185FA5', border:'2px solid #185FA5', padding:'14px 36px', borderRadius:12, fontWeight:600, fontSize:15, cursor:'pointer'}}>
                  Sign up free
                </button>
              </div>
            </div>

            {/* Blurred preview */}
            <div style={{position:'relative', marginBottom:24}}>
              <div style={{filter:'blur(6px)', pointerEvents:'none', display:'flex', flexDirection:'column', gap:16}}>
                {[{title:'Career Match 1', pct:92},{title:'Career Match 2', pct:85},{title:'Career Match 3', pct:78}].map((c,i) => (
                  <div key={i} style={{background:'white', borderRadius:16, border: i===0 ? '2px solid #185FA5' : '1px solid #eee', padding:24}}>
                    <div style={{display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:8}}>
                      <h3 style={{fontWeight:600, color:'#111', fontSize:16}}>{c.title}</h3>
                      <span style={{fontSize:13, fontWeight:700, padding:'4px 12px', borderRadius:20, background: i===0 ? '#E6F1FB' : '#f0f0f0', color: i===0 ? '#185FA5' : '#666'}}>{c.pct}% match</span>
                    </div>
                    <p style={{fontSize:14, color:'#666'}}>Log in to see your personalized career description and skill gaps.</p>
                  </div>
                ))}
              </div>
              <div style={{position:'absolute', inset:0, display:'flex', alignItems:'center', justifyContent:'center'}}>
                <div style={{background:'white', borderRadius:16, padding:'20px 32px', textAlign:'center', boxShadow:'0 8px 32px rgba(0,0,0,0.12)', border:'1px solid #eee'}}>
                  <p style={{fontWeight:600, color:'#111', fontSize:15, marginBottom:4}}>🔍 Your results are hidden</p>
                  <p style={{color:'#999', fontSize:13}}>Log in above to unlock all matches</p>
                </div>
              </div>
            </div>
          </>
        )}

        {/* LOGGED IN — no careers */}
        {isLoggedIn && careers.length === 0 && (
          <div style={{background:'white', borderRadius:20, border:'1px solid #eee', padding:48, textAlign:'center', boxShadow:'0 4px 20px rgba(0,0,0,0.05)'}}>
            <p style={{fontSize:40, marginBottom:16}}>📊</p>
            <h2 style={{fontSize:18, fontWeight:700, color:'#111', marginBottom:8}}>No analysis found yet</h2>
            <p style={{color:'#666', fontSize:14, marginBottom:24}}>Run your first career analysis to see your matches here.</p>
            <button onClick={() => navigate('/analysis')} style={{background:'#185FA5', color:'white', border:'none', padding:'12px 28px', borderRadius:12, fontWeight:600, fontSize:14, cursor:'pointer'}}>
              Start Analysis →
            </button>
          </div>
        )}

        {/* LOGGED IN — show careers */}
        {isLoggedIn && careers.length > 0 && (
          <>
            <div style={{display:'flex', flexDirection:'column', gap:16, marginBottom:32}}>
              {careers.map((c, i) => (
                <div key={c.careerTitle || i} style={{width:'100%', background:'white', borderRadius:16, border: i===0 ? '2px solid #185FA5' : '1px solid #eee', padding: isMobile ? 16 : 24, boxShadow: i===0 ? '0 4px 20px rgba(24,95,165,0.1)' : 'none'}}>
                  <div style={{display:'flex', justifyContent:'space-between', alignItems:'flex-start', marginBottom:8}}>
                    <h3 style={{fontWeight:600, color:'#111', fontSize:16}}>{c.careerTitle}</h3>
                    <span style={{fontSize:13, fontWeight:700, padding:'4px 12px', borderRadius:20, background: i===0 ? '#E6F1FB' : '#f0f0f0', color: i===0 ? '#185FA5' : '#666'}}>{c.matchPercentage}% match</span>
                  </div>
                  <p style={{fontSize:14, color:'#666', marginBottom: c.missingSkills?.length ? 12 : 0}}>{c.description}</p>
                  {c.missingSkills && c.missingSkills.length > 0 && (
                    <div>
                      <p style={{fontSize:12, color:'#999', marginBottom:6}}>Skills to develop:</p>
                      <div style={{display:'flex', flexWrap:'wrap', gap:6}}>
                        {c.missingSkills.map(s => (
                          <span key={s} style={{fontSize:12, padding:'3px 10px', borderRadius:20, background:'#FFF7ED', color:'#D97706', border:'1px solid #FDC57B'}}>{s}</span>
                        ))}
                      </div>
                    </div>
                  )}
                </div>
              ))}
            </div>
            <div style={{background:'#f0f9f4', borderRadius:20, padding:24, textAlign:'center', border:'1px solid #6ee7b7'}}>
              <p style={{color:'#065f46', fontWeight:600, fontSize:15, marginBottom:8}}>✓ Full report unlocked</p>
              <button onClick={() => navigate('/dashboard')} style={{background:'#059669', color:'white', border:'none', padding:'10px 24px', borderRadius:12, fontWeight:600, fontSize:14, cursor:'pointer'}}>Go to dashboard →</button>
            </div>
          </>
        )}
      </div>
    </div>
  )
}
