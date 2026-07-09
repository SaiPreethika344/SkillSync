import { useNavigate, useLocation } from 'react-router-dom'
import { useEffect, useState, useRef } from 'react'
import useWindowWidth from '../hooks/useWindowWidth'
import { getDashboard, getRoadmap, completeStep, undoStep } from '../api'
import ChatBot from '../components/ChatBot'

function RoadmapStep({ step, onComplete, onUndo, topCareer }) {
  const width = useWindowWidth()
  const isMobile = width < 768
  const [expanded, setExpanded] = useState(false)
  const [aiDetails, setAiDetails] = useState(null)
  const [loadingAi, setLoadingAi] = useState(false)

  const cleanText = (text) => {
    if (!text) return ''
    return text
      .replace(/\*\*(.*?)\*\*/g, '$1')
      .replace(/\*(.*?)\*/g, '$1')
      .replace(/#{1,6}\s/g, '')
      .trim()
  }

  const handleExpand = async () => {
    if (!expanded && !aiDetails) {
      setLoadingAi(true)
      try {
        const res = await fetch('http://localhost:8080/api/chat', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          },
          body: JSON.stringify({
            message: `For the learning roadmap step "${step.title}" needed to become a ${topCareer}, give me:
1. Why this skill matters (1 sentence)
2. How to learn it (2-3 specific resources or methods)
3. Estimated time to learn (e.g. "2-3 weeks")
4. A quick tip to get started today

Keep it concise and practical. Format with clear labels.`,
            context: `Career roadmap step: ${step.title}`
          })
        })
        if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`)
        const data = await res.json()
        const raw = data.reply || data.response || data.message || 'Could not load details.'
        setAiDetails(cleanText(raw))
      } catch (error) {
        setAiDetails('Unable to load AI details. Please try again.')
      } finally {
        setLoadingAi(false)
      }
    }
    setExpanded(prev => !prev)
  }

  return (
    <div style={{borderRadius:10, border:'1px solid #f0f0f0', background: step.isComplete ? '#f0f9f4' : 'white', transition:'background 0.3s', overflow:'hidden'}}>
      {/* Main row — always horizontal, buttons always on right */}
      <div style={{display:'flex', alignItems:'center', gap:14, padding:14}}>
        {/* Step number circle */}
        <div style={{width:36, height:36, borderRadius:'50%', background: step.isComplete ? '#d1fae5' : '#E6F1FB', display:'flex', alignItems:'center', justifyContent:'center', fontSize:14, fontWeight:700, color: step.isComplete ? '#065f46' : '#185FA5', flexShrink:0}}>
          {step.isComplete ? '✓' : String(step.stepOrder || 1).padStart(2,'0')}
        </div>

        {/* Title — takes all available space */}
        <div style={{flex:1, minWidth:0}}>
          <p style={{fontSize: isMobile ? 12 : 13, fontWeight:500, color: step.isComplete ? '#065f46' : '#111', textDecoration: step.isComplete ? 'line-through' : 'none', margin:0, wordBreak:'break-word'}}>{step.title}</p>
          <p style={{fontSize:11, color: step.isComplete ? '#059669' : '#999', marginTop:2, marginBottom:0}}>{step.isComplete ? 'Completed ✓' : 'Upcoming'}</p>
        </div>

        {/* Buttons — always on right, never wrap */}
        <div style={{display:'flex', gap:8, alignItems:'center', flexShrink:0, marginLeft:'auto'}}>
          <button
            onClick={handleExpand}
            style={{fontSize:12, color:'#185FA5', background:'#f0f5fb', border:'none', padding:'6px 12px', borderRadius:8, cursor:'pointer', fontWeight:500, whiteSpace:'nowrap'}}>
            {expanded ? 'Hide ▲' : '✨ Details'}
          </button>
          {!step.isComplete && (
            <button
              onClick={() => onComplete(step.id)}
              style={{fontSize:12, color:'#185FA5', background:'#E6F1FB', border:'none', padding:'8px 14px', borderRadius:8, cursor:'pointer', fontWeight:500, whiteSpace:'nowrap'}}>
              Mark done
            </button>
          )}
          {step.isComplete && (
            <button
              onClick={() => onUndo(step.id)}
              style={{fontSize:11, color:'#64748b', background:'#f1f5f9', border:'1px solid #e2e8f0', padding:'5px 10px', borderRadius:8, cursor:'pointer', fontWeight:500, whiteSpace:'nowrap'}}>
              Undo
            </button>
          )}
        </div>
      </div>

      {/* Expanded AI details */}
      {expanded && (
        <div style={{padding: isMobile ? '12px' : '0 14px 16px 64px', borderTop:'1px solid #f0f0f0'}}>
          {loadingAi ? (
            <div style={{display:'flex', alignItems:'center', gap:10, padding:'14px 0'}}>
              <div style={{width:16, height:16, border:'2px solid #185FA5', borderTop:'2px solid transparent', borderRadius:'50%', animation:'spin 0.8s linear infinite'}}></div>
              <p style={{fontSize:13, color:'#999'}}>Getting AI insights for this step...</p>
            </div>
          ) : (
            <div style={{paddingTop:14}}>
              <p style={{fontSize:13, color:'#444', lineHeight:1.8, whiteSpace:'pre-wrap'}}>{aiDetails}</p>
            </div>
          )}
        </div>
      )}
    </div>
  )
}

export default function DashboardPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const [dash, setDash] = useState(null)
  const [roadmap, setRoadmap] = useState([])
  const [loading, setLoading] = useState(true)
  const [activeNav, setActiveNav] = useState('Overview')
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const [selectedCareer, setSelectedCareer] = useState(null)
  const [dynamicRoadmap, setDynamicRoadmap] = useState(null)
  const [loadingRoadmap, setLoadingRoadmap] = useState(false)
  const roadmapRef = useRef(null)
  const width = useWindowWidth()
  const isMobile = width < 768

  useEffect(() => {
    const token = localStorage.getItem('token')
    if (!token) { navigate('/login'); return }
    Promise.all([getDashboard(), getRoadmap()])
      .then(([dashData, roadmapData]) => {
        setDash(dashData)
        setRoadmap(Array.isArray(roadmapData) ? roadmapData : [])
      })
      .catch(() => navigate('/login'))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    if (dash?.topCareerMatches && dash.topCareerMatches.length > 0 && !selectedCareer) {
      setSelectedCareer(dash.topCareerMatches[0])
    }
  }, [dash])

  useEffect(() => {
    if (!selectedCareer) return
    const careers = dash?.topCareerMatches || []
    if (!careers.length) return
    if (selectedCareer.careerTitle === careers[0]?.careerTitle) {
      setDynamicRoadmap(null)
      return
    }
    setLoadingRoadmap(true)
    const token = localStorage.getItem('token')
    fetch('http://localhost:8080/api/chat', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({
        message: `Generate a learning roadmap for someone who wants to become a ${selectedCareer.careerTitle}. Give exactly 6 specific actionable steps numbered 1-6. Format each step as just the step title, one per line, no extra text.`,
        context: `Career roadmap for ${selectedCareer.careerTitle}`
      })
    })
    .then(r => r.json())
    .then(data => {
      const text = data.reply || data.response || data.message || ''
      const steps = text.split('\n')
        .map(s => s.replace(/^\d+[\.\)]\s*/, '').replace(/\*\*/g, '').trim())
        .filter(s => s.length > 5)
        .slice(0, 6)
        .map((title, i) => ({
          id: `dynamic-${i}`,
          title,
          stepOrder: i + 1,
          isComplete: false
        }))
      setDynamicRoadmap(steps)
      setLoadingRoadmap(false)
    })
    .catch(() => setLoadingRoadmap(false))
  }, [selectedCareer])

  const handleComplete = async (id) => {
    await completeStep(id)
    setRoadmap(prev => prev.map(r => r.id === id ? {...r, isComplete: true} : r))
  }

  const handleUndo = async (id) => {
    await undoStep(id)
    setRoadmap(prev => prev.map(r => r.id === id ? { ...r, isComplete: false } : r))
  }

  const currentRoadmap = dynamicRoadmap || roadmap
  const completedSteps = currentRoadmap.filter(r => r.isComplete).length
  const totalSteps = currentRoadmap.length
  const progressPercent = totalSteps > 0 ? Math.round((completedSteps / totalSteps) * 100) : 0
  const radius = 45
  const circumference = 2 * Math.PI * radius
  const strokeOffset = circumference - (progressPercent / 100) * circumference

  if (loading) return (
    <div style={{minHeight:'100vh', display:'flex', alignItems:'center', justifyContent:'center', background:'#f9f9f9'}}>
      <p style={{color:'#666', fontSize:16}}>Loading your dashboard...</p>
    </div>
  )

  if (!dash) return (
    <div style={{minHeight:'100vh', display:'flex', alignItems:'center', justifyContent:'center', background:'#f9f9f9'}}>
      <div style={{textAlign:'center'}}>
        <p style={{color:'#666', fontSize:16, marginBottom:16}}>No analysis found yet.</p>
        <button onClick={() => navigate('/analysis')} style={{background:'#185FA5', color:'white', border:'none', padding:'12px 24px', borderRadius:12, fontSize:14, fontWeight:600, cursor:'pointer'}}>Start your first analysis</button>
      </div>
    </div>
  )

  const careers = dash.topCareerMatches || []
  const skills = dash.skillStrengths || []
  const navItems = ['Overview', 'Career paths', 'Skill gap']

  return (
    <div style={{display:'flex', minHeight:'100vh', background:'#f9f9f9'}}>
      <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>

      {/* Desktop sidebar */}
      {!isMobile && (
        <aside style={{width:220, background:'white', borderRight:'1px solid #f0f0f0', display:'flex', flexDirection:'column', position:'fixed', height:'100vh'}}>
          <div style={{padding:'20px 16px', borderBottom:'1px solid #f0f0f0'}}>
            <div style={{display:'flex', alignItems:'center', gap:8}}>
              <img src="/logo.svg" width="32" height="32" style={{borderRadius:8, flexShrink:0}} alt="SkillSync AI"/>
              <span style={{fontWeight:600, color:'#111', fontSize:14}}>SkillSync <span style={{color:'#185FA5'}}>AI</span></span>
            </div>
          </div>
          <nav style={{flex:1, padding:'12px 8px', display:'flex', flexDirection:'column', gap:4}}>
            {navItems.map((item) => (
              <button key={item} onClick={() => setActiveNav(item)}
                style={{width:'100%', textAlign:'left', padding:'10px 12px', borderRadius:8, border:'none', cursor:'pointer', fontSize:13, background: activeNav === item ? '#f0f5fb' : 'transparent', color: activeNav === item ? '#185FA5' : '#555', fontWeight: activeNav === item ? 500 : 400}}>
                {item}
              </button>
            ))}
          </nav>
          <div style={{padding:'16px', borderTop:'1px solid #f0f0f0'}}>
            <div style={{display:'flex', alignItems:'center', gap:8, marginBottom:12}}>
              <div style={{width:32, height:32, borderRadius:'50%', background:'#E6F1FB', display:'flex', alignItems:'center', justifyContent:'center', fontSize:12, fontWeight:600, color:'#185FA5'}}>
                {dash.userName ? dash.userName[0].toUpperCase() : 'U'}
              </div>
              <div>
                <p style={{fontSize:12, fontWeight:500, color:'#111'}}>{dash.userName || 'User'}</p>
                <p style={{fontSize:11, color:'#999'}}>SkillSync Member</p>
              </div>
            </div>
            <button onClick={() => { localStorage.removeItem('token'); localStorage.removeItem('isPremium'); navigate('/') }}
              style={{fontSize:12, color:'#999', background:'none', border:'none', cursor:'pointer', padding:0}}>← Log out</button>
          </div>
        </aside>
      )}

      {/* Mobile sidebar overlay */}
      {isMobile && sidebarOpen && (
        <aside style={{position:'fixed', inset:0, background:'white', zIndex:1100, padding:'20px 16px', overflowY:'auto'}}>
          <div style={{display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:24}}>
            <div style={{display:'flex', alignItems:'center', gap:8}}>
              <img src="/logo.svg" width="32" height="32" style={{borderRadius:8, flexShrink:0}} alt="SkillSync AI"/>
              <span style={{fontWeight:600, color:'#111', fontSize:14}}>SkillSync <span style={{color:'#185FA5'}}>AI</span></span>
            </div>
            <button onClick={() => setSidebarOpen(false)} style={{fontSize:22, border:'none', background:'none', cursor:'pointer', color:'#333'}}>✕</button>
          </div>
          <nav style={{display:'flex', flexDirection:'column', gap:10, marginBottom:24}}>
            {navItems.map((item) => (
              <button key={item} onClick={() => { setActiveNav(item); setSidebarOpen(false) }}
                style={{width:'100%', textAlign:'left', padding:'14px 16px', borderRadius:12, border:'1px solid #f0f0f0', cursor:'pointer', fontSize:14, background: activeNav === item ? '#f0f5fb' : 'white', color: activeNav === item ? '#185FA5' : '#555', fontWeight: activeNav === item ? 600 : 500}}>
                {item}
              </button>
            ))}
          </nav>
          <div style={{padding:'16px', borderTop:'1px solid #f0f0f0'}}>
            <div style={{display:'flex', alignItems:'center', gap:8, marginBottom:12}}>
              <div style={{width:32, height:32, borderRadius:'50%', background:'#E6F1FB', display:'flex', alignItems:'center', justifyContent:'center', fontSize:12, fontWeight:600, color:'#185FA5'}}>
                {dash.userName ? dash.userName[0].toUpperCase() : 'U'}
              </div>
              <div>
                <p style={{fontSize:12, fontWeight:500, color:'#111'}}>{dash.userName || 'User'}</p>
                <p style={{fontSize:11, color:'#999'}}>SkillSync Member</p>
              </div>
            </div>
            <button onClick={() => { localStorage.removeItem('token'); localStorage.removeItem('isPremium'); navigate('/') }}
              style={{fontSize:12, color:'#999', background:'none', border:'none', cursor:'pointer', padding:0}}>← Log out</button>
          </div>
        </aside>
      )}

      <main style={{marginLeft: isMobile ? 0 : 220, flex:1, padding: isMobile ? 16 : 32}}>
        <div style={{maxWidth:900, margin:'0 auto'}}>

          {activeNav === 'Overview' && (
            <>
              {/* Header */}
              <div style={{display:'flex', flexDirection: isMobile ? 'column' : 'row', justifyContent:'space-between', alignItems: isMobile ? 'stretch' : 'center', marginBottom:32, gap:12}}>
                <div style={{display:'flex', flexDirection:'row', alignItems:'center', gap:12}}>
                  {isMobile && (
                    <button onClick={() => setSidebarOpen(true)}
                      style={{background:'#185FA5', color:'white', border:'none', padding:'10px 14px', borderRadius:10, fontSize:14, fontWeight:500, cursor:'pointer', flexShrink:0}}>
                      ☰ Menu
                    </button>
                  )}
                  <div>
                    <h1 style={{fontSize: isMobile ? 18 : 24, fontWeight:700, color:'#111', wordBreak:'break-word', whiteSpace:'normal', margin:0}}>
                      Good afternoon, {dash.userName || 'there'}
                    </h1>
                    <p style={{color:'#666', fontSize:14, marginTop:4, marginBottom:0}}>Here's your career intelligence summary.</p>
                  </div>
                </div>
                <button onClick={() => navigate('/analysis')}
                  style={{background:'#185FA5', color:'white', border:'none', padding:'10px 20px', borderRadius:10, fontSize:14, fontWeight:500, cursor:'pointer', width: isMobile ? '100%' : 'auto'}}>
                  + New analysis
                </button>
              </div>

              {/* Metrics */}
              <div style={{display:'grid', gridTemplateColumns: isMobile ? '1fr' : 'repeat(3,1fr)', gap:16, marginBottom:24}}>
                <div style={{background:'white', borderRadius:14, border:'1px solid #f0f0f0', padding:20}}>
                  <p style={{fontSize:12, color:'#999', marginBottom:4}}>Career match score</p>
                  <p style={{fontSize:30, fontWeight:700, color:'#111'}}>{dash.topCareerMatchScore || 0}%</p>
                  <p style={{fontSize:12, color:'#999', marginTop:4}}>{careers[0]?.careerTitle || 'Run an analysis'}</p>
                </div>
                <div style={{background:'white', borderRadius:14, border:'1px solid #f0f0f0', padding:20}}>
                  <p style={{fontSize:12, color:'#999', marginBottom:4}}>Skills identified</p>
                  <p style={{fontSize:30, fontWeight:700, color:'#111'}}>{skills.length}</p>
                  <p style={{fontSize:12, color:'#999', marginTop:4}}>From your analysis</p>
                </div>
                <div style={{background:'white', borderRadius:14, border:'1px solid #f0f0f0', padding:20}}>
                  <p style={{fontSize:12, color:'#999', marginBottom:4}}>Roadmap steps</p>
                  <p style={{fontSize:30, fontWeight:700, color:'#111'}}>{roadmap.length}</p>
                  <p style={{fontSize:12, color:'#999', marginTop:4}}>{completedSteps} completed</p>
                </div>
              </div>

              {/* Career matches + Skill strength */}
              <div style={{display:'grid', gridTemplateColumns: isMobile ? '1fr' : '1fr 1fr', gap:16, marginBottom:16}}>
                <div style={{background:'white', borderRadius:14, border:'1px solid #f0f0f0', padding:24}}>
                  <h2 style={{fontWeight:600, color:'#111', fontSize:14, marginBottom:16}}>Top career matches</h2>
                  {careers.length === 0 ? (
                    <div style={{textAlign:'center', padding:'20px 0'}}>
                      <p style={{color:'#999', fontSize:13, marginBottom:12}}>No analysis yet</p>
                      <button onClick={() => navigate('/analysis')} style={{background:'#185FA5', color:'white', border:'none', padding:'8px 16px', borderRadius:8, fontSize:13, cursor:'pointer'}}>Start analysis</button>
                    </div>
                  ) : (
                    <div style={{display:'flex', flexDirection:'column', gap:10}}>
                      {careers.map((c, i) => {
                        const isSelected = selectedCareer?.careerTitle === c.careerTitle
                        return (
                          <div key={c.careerTitle || i} onClick={() => setSelectedCareer(c)}
                            style={{padding:12, borderRadius:10, border: isSelected ? '2px solid #185FA5' : '1px solid #f0f0f0', background: isSelected ? '#f0f5fb' : 'white', cursor:'pointer', transition:'all 0.2s ease'}}>
                            <div style={{display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:2}}>
                              <div style={{display:'flex', alignItems:'center', gap:8}}>
                                <span style={{fontSize:13, fontWeight:500, color:'#111'}}>{c.careerTitle}</span>
                                {isSelected && <span style={{fontSize:10, fontWeight:600, padding:'2px 8px', borderRadius:12, background:'#185FA5', color:'white'}}>Selected</span>}
                              </div>
                              <span style={{fontSize:12, fontWeight:700, color:'#185FA5'}}>{c.matchPercentage}%</span>
                            </div>
                            <p style={{fontSize:12, color:'#999', margin:0}}>{c.description}</p>
                          </div>
                        )
                      })}
                    </div>
                  )}
                </div>

                <div style={{background:'white', borderRadius:14, border:'1px solid #f0f0f0', padding:24}}>
                  <h2 style={{fontWeight:600, color:'#111', fontSize:14, marginBottom:16}}>Skill strength</h2>
                  {skills.length === 0 ? (
                    <p style={{color:'#999', fontSize:13, textAlign:'center', padding:'20px 0'}}>Run an analysis to see your skill strengths</p>
                  ) : (
                    <div style={{display:'flex', flexDirection:'column', gap:14}}>
                      {skills.map((s, i) => (
                        <div key={s.skillName || i} style={{display:'flex', alignItems:'center', gap:10}}>
                          <span style={{fontSize:12, color:'#666', width:110, flexShrink:0}}>{s.skillName}</span>
                          <div style={{flex:1, background:'#f0f0f0', borderRadius:6, height:6}}>
                            <div style={{width:`${s.percentage}%`, height:6, borderRadius:6, background: s.percentage >= 70 ? '#185FA5' : s.percentage >= 50 ? '#EF9F27' : '#E24B4A'}}></div>
                          </div>
                          <span style={{fontSize:12, color:'#999', width:28, textAlign:'right'}}>{s.percentage}%</span>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </div>

              {/* Roadmap */}
              <div ref={roadmapRef} style={{background:'white', borderRadius:14, border:'1px solid #f0f0f0', padding:24}}>
                <div style={{display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:20}}>
                  <div>
                    <h2 style={{fontWeight:600, color:'#111', fontSize:14}}>Learning roadmap</h2>
                    <p style={{fontSize:12, color:'#999', marginTop:4}}>Click ✨ Details on any step for AI-powered guidance</p>
                    {selectedCareer && (
                      <p style={{fontSize:12, color:'#185FA5', fontWeight:500, marginTop:8}}>
                        📌 Showing roadmap for: <strong>{selectedCareer.careerTitle}</strong>
                      </p>
                    )}
                  </div>
                  {totalSteps > 0 && (
                    <div style={{display:'flex', alignItems:'center', gap:16}}>
                      <div style={{textAlign:'right'}}>
                        <p style={{fontSize:13, fontWeight:600, color:'#111'}}>{completedSteps} of {totalSteps} done</p>
                        <p style={{fontSize:11, color:'#999'}}>{progressPercent === 100 ? '🎉 All complete!' : `${100 - progressPercent}% remaining`}</p>
                      </div>
                      <svg width={isMobile ? 60 : 80} height={isMobile ? 60 : 80} viewBox="0 0 120 120">
                        <circle cx="60" cy="60" r={radius} fill="none" stroke="#f0f0f0" strokeWidth="10"/>
                        <circle cx="60" cy="60" r={radius} fill="none" stroke={progressPercent === 100 ? '#059669' : '#185FA5'} strokeWidth="10" strokeDasharray={circumference} strokeDashoffset={strokeOffset} strokeLinecap="round" transform="rotate(-90 60 60)" style={{transition:'stroke-dashoffset 0.6s ease'}}/>
                        <text x="60" y="56" textAnchor="middle" fontSize="22" fontWeight="700" fill="#111">{progressPercent}%</text>
                        <text x="60" y="74" textAnchor="middle" fontSize="11" fill="#999">progress</text>
                      </svg>
                    </div>
                  )}
                </div>
                {currentRoadmap.length === 0 ? (
                  <div style={{textAlign:'center', padding:'30px 0'}}>
                    <p style={{fontSize:28, marginBottom:12}}>🗺️</p>
                    <p style={{color:'#999', fontSize:14, marginBottom:16}}>Your personalized roadmap will appear after your first analysis</p>
                    <button onClick={() => navigate('/analysis')} style={{background:'#185FA5', color:'white', border:'none', padding:'10px 20px', borderRadius:10, fontSize:13, fontWeight:600, cursor:'pointer'}}>Start analysis →</button>
                  </div>
                ) : loadingRoadmap ? (
                  <div style={{textAlign:'center', padding:'20px 0'}}>
                    <p style={{color:'#999', fontSize:13}}>Generating roadmap for {selectedCareer?.careerTitle}...</p>
                  </div>
                ) : (
                  <div style={{display:'flex', flexDirection:'column', gap:10}}>
                    {currentRoadmap.map(r => (
                      <RoadmapStep
                        key={r.id}
                        step={r}
                        onComplete={r.id?.toString().startsWith('dynamic') ? () => {} : handleComplete}
                        onUndo={r.id?.toString().startsWith('dynamic') ? () => {} : handleUndo}
                        topCareer={selectedCareer?.careerTitle || careers[0]?.careerTitle || 'your target career'}
                      />
                    ))}
                  </div>
                )}
              </div>
            </>
          )}

          {activeNav === 'Career paths' && (
            <>
              {isMobile && (
                <button onClick={() => setSidebarOpen(true)}
                  style={{background:'#185FA5', color:'white', border:'none', padding:'10px 14px', borderRadius:10, fontSize:14, fontWeight:500, cursor:'pointer', marginBottom:20}}>
                  ☰ Menu
                </button>
              )}
              <h1 style={{fontSize: isMobile ? 20 : 24, fontWeight:700, color:'#111', marginBottom:8}}>Your Career Paths</h1>
              <p style={{color:'#666', fontSize:14, marginBottom:24}}>Detailed breakdown of your top career matches</p>
              {careers.length === 0 ? (
                <div style={{textAlign:'center', padding:'40px 0'}}>
                  <p style={{fontSize:28, marginBottom:12}}>📊</p>
                  <p style={{color:'#999', fontSize:14, marginBottom:16}}>No career analysis yet</p>
                  <button onClick={() => navigate('/analysis')} style={{background:'#185FA5', color:'white', border:'none', padding:'10px 20px', borderRadius:10, fontSize:13, fontWeight:600, cursor:'pointer'}}>Start analysis →</button>
                </div>
              ) : (
                <div style={{display:'flex', flexDirection:'column', gap:20}}>
                  {careers.map((c, i) => (
                    <div key={c.careerTitle || i} style={{background:'white', borderRadius:14, border:'1px solid #f0f0f0', padding: isMobile ? 16 : 24}}>
                      <div style={{display:'flex', justifyContent:'space-between', alignItems:'flex-start', marginBottom:16}}>
                        <div style={{flex:1, marginRight:16}}>
                          <h2 style={{fontSize: isMobile ? 16 : 18, fontWeight:700, color:'#111', marginBottom:8}}>{c.careerTitle}</h2>
                          <p style={{fontSize:14, color:'#666', lineHeight:1.6}}>{c.description}</p>
                        </div>
                        <div style={{textAlign:'right', minWidth:70, flexShrink:0}}>
                          <p style={{fontSize: isMobile ? 22 : 28, fontWeight:700, color:'#185FA5'}}>{c.matchPercentage}%</p>
                          <p style={{fontSize:12, color:'#999'}}>match</p>
                        </div>
                      </div>
                      <div style={{marginBottom:16}}>
                        <div style={{width:'100%', background:'#f0f0f0', borderRadius:6, height:8}}>
                          <div style={{width:`${c.matchPercentage}%`, height:8, borderRadius:6, background:'#185FA5'}}></div>
                        </div>
                      </div>
                      {c.requiredSkills && c.requiredSkills.length > 0 && (
                        <div style={{marginBottom:16}}>
                          <p style={{fontSize:12, fontWeight:600, color:'#666', marginBottom:8}}>Required skills</p>
                          <div style={{display:'flex', flexWrap:'wrap', gap:8}}>
                            {c.requiredSkills.map((skill, idx) => (
                              <span key={idx} style={{background:'#E6F1FB', color:'#185FA5', padding:'6px 12px', borderRadius:20, fontSize:12, fontWeight:500}}>{skill}</span>
                            ))}
                          </div>
                        </div>
                      )}
                      <button
                        onClick={() => {
                          setSelectedCareer(c)
                          setActiveNav('Overview')
                          setTimeout(() => {
                            roadmapRef.current?.scrollIntoView({ behavior:'smooth' })
                          }, 150)
                        }}
                        style={{background:'#185FA5', color:'white', border:'none', padding:'10px 20px', borderRadius:10, fontSize:14, fontWeight:500, cursor:'pointer', width: isMobile ? '100%' : 'auto'}}>
                        Explore roadmap →
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </>
          )}

          {activeNav === 'Skill gap' && (
            <>
              {isMobile && (
                <button onClick={() => setSidebarOpen(true)}
                  style={{background:'#185FA5', color:'white', border:'none', padding:'10px 14px', borderRadius:10, fontSize:14, fontWeight:500, cursor:'pointer', marginBottom:20}}>
                  ☰ Menu
                </button>
              )}
              <h1 style={{fontSize: isMobile ? 20 : 24, fontWeight:700, color:'#111', marginBottom:8}}>Your Skill Gap Analysis</h1>
              <p style={{color:'#666', fontSize:14, marginBottom:24}}>Compare your current skills with what you need</p>
              <div style={{display:'grid', gridTemplateColumns: isMobile ? '1fr' : '1fr 1fr', gap:24}}>
                <div>
                  <h2 style={{fontSize:16, fontWeight:600, color:'#065f46', marginBottom:16}}>✅ Skills You Have</h2>
                  <div style={{background:'white', borderRadius:14, border:'1px solid #f0f0f0', padding:20}}>
                    {skills.filter(s => s.percentage >= 60).length === 0 ? (
                      <p style={{color:'#999', fontSize:13, textAlign:'center', padding:'20px 0'}}>No strong skills identified yet</p>
                    ) : (
                      <div style={{display:'flex', flexWrap:'wrap', gap:8}}>
                        {skills.filter(s => s.percentage >= 60).map((s, i) => (
                          <span key={i} style={{background:'#dcfce7', color:'#065f46', padding:'8px 16px', borderRadius:20, fontSize:13, fontWeight:500}}>{s.skillName} ({s.percentage}%)</span>
                        ))}
                      </div>
                    )}
                  </div>
                </div>
                <div>
                  <h2 style={{fontSize:16, fontWeight:600, color:'#c2410c', marginBottom:16}}>📈 Skills to Develop</h2>
                  <div style={{background:'white', borderRadius:14, border:'1px solid #f0f0f0', padding:20}}>
                    {(() => {
                      const allMissing = careers.flatMap(c => c.missingSkills || [])
                      const unique = [...new Set(allMissing)]
                      return unique.length === 0 ? (
                        <p style={{color:'#999', fontSize:13, textAlign:'center', padding:'20px 0'}}>No missing skills identified</p>
                      ) : (
                        <>
                          <div style={{display:'flex', flexWrap:'wrap', gap:8, marginBottom:16}}>
                            {unique.map((skill, i) => (
                              <span key={i} style={{background:'#ffedd5', color:'#c2410c', padding:'8px 16px', borderRadius:20, fontSize:13, fontWeight:500}}>{skill}</span>
                            ))}
                          </div>
                          <p style={{fontSize:12, color:'#666', fontStyle:'italic'}}>Focus on these skills to improve your career match score</p>
                        </>
                      )
                    })()}
                  </div>
                </div>
              </div>
            </>
          )}

        </div>
      </main>

      <ChatBot
        userName={dash.userName}
        topCareer={careers[0]?.careerTitle || 'your target career'}
        roadmap={roadmap}
      />
    </div>
  )
}