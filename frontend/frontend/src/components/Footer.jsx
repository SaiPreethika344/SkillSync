import { Link } from 'react-router-dom'

export default function Footer() {
  return (
    <footer style={{background:'#0f172a', color:'white', padding:'60px 40px 32px'}}>
      <div style={{maxWidth:1100, margin:'0 auto'}}>
        <div style={{display:'grid', gridTemplateColumns:'2fr 1fr 1fr 1fr', gap:40, marginBottom:48}}>
          
          {/* Brand column */}
          <div>
            <div style={{display:'flex', alignItems:'center', gap:8, marginBottom:16}}>
              <div style={{width:32, height:32, background:'#185FA5', borderRadius:8, display:'flex', alignItems:'center', justifyContent:'center'}}>
                <span style={{color:'white', fontSize:14, fontWeight:700}}>S</span>
              </div>
              <span style={{fontWeight:700, fontSize:18}}>SkillSync <span style={{color:'#60a5fa'}}>AI</span></span>
            </div>
            <p style={{color:'#94a3b8', fontSize:14, lineHeight:1.7, maxWidth:280}}>
              AI-powered career intelligence for students across all departments. Discover your ideal career path with personalised skill analysis.
            </p>
            <div style={{display:'flex', gap:12, marginTop:20}}>
              {['Engineering', 'Medical', 'Commerce', 'Arts'].map(d => (
                <span key={d} style={{fontSize:11, color:'#60a5fa', background:'rgba(96,165,250,0.1)', padding:'3px 8px', borderRadius:20, border:'1px solid rgba(96,165,250,0.2)'}}>{d}</span>
              ))}
            </div>
          </div>

          {/* Product */}
          <div>
            <h4 style={{fontWeight:600, fontSize:14, marginBottom:16, color:'white'}}>Product</h4>
            {[
              ['/analysis', 'Career Analysis'],
              ['/pricing', 'Pricing'],
              ['/#how-it-works', 'How it works'],
              ['/dashboard', 'Dashboard'],
            ].map(([to, label]) => (
              <div key={label} style={{marginBottom:10}}>
                <Link to={to} style={{color:'#94a3b8', fontSize:14, textDecoration:'none'}}
                  onMouseEnter={e => e.target.style.color='white'}
                  onMouseLeave={e => e.target.style.color='#94a3b8'}>
                  {label}
                </Link>
              </div>
            ))}
          </div>

          {/* Departments */}
          <div>
            <h4 style={{fontWeight:600, fontSize:14, marginBottom:16, color:'white'}}>Departments</h4>
            {['Engineering', 'Medical', 'Commerce', 'Arts & Design', 'Law', 'Science'].map(d => (
              <div key={d} style={{marginBottom:10}}>
                <span style={{color:'#94a3b8', fontSize:14}}>{d}</span>
              </div>
            ))}
          </div>

          {/* Company */}
          <div>
            <h4 style={{fontWeight:600, fontSize:14, marginBottom:16, color:'white'}}>Company</h4>
            {[
              ['/login', 'Log in'],
              ['/signup', 'Sign up free'],
              ['/pricing', 'Get Pro — ₹399'],
            ].map(([to, label]) => (
              <div key={label} style={{marginBottom:10}}>
                <Link to={to} style={{color:'#94a3b8', fontSize:14, textDecoration:'none'}}
                  onMouseEnter={e => e.target.style.color='white'}
                  onMouseLeave={e => e.target.style.color='#94a3b8'}>
                  {label}
                </Link>
              </div>
            ))}
          </div>
        </div>

        {/* Bottom bar */}
        <div style={{borderTop:'1px solid #1e293b', paddingTop:24, display:'flex', justifyContent:'space-between', alignItems:'center', flexWrap:'wrap', gap:12}}>
          <p style={{fontSize:13, color:'#475569'}}>© 2026 SkillSync AI. Built for students, by students.</p>
          <div style={{display:'flex', gap:6, alignItems:'center'}}>
            <span style={{fontSize:12, color:'#475569'}}>Powered by</span>
            <span style={{fontSize:12, color:'#60a5fa', fontWeight:500}}>Groq AI</span>
            <span style={{fontSize:12, color:'#475569'}}>·</span>
            <span style={{fontSize:12, color:'#60a5fa', fontWeight:500}}>Spring Boot</span>
            <span style={{fontSize:12, color:'#475569'}}>·</span>
            <span style={{fontSize:12, color:'#60a5fa', fontWeight:500}}>PostgreSQL</span>
          </div>
        </div>
      </div>
    </footer>
  )
}