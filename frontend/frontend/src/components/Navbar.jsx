import { useNavigate, Link } from 'react-router-dom'
import useWindowWidth from '../hooks/useWindowWidth'

export default function Navbar() {
  const navigate = useNavigate()
  const token = localStorage.getItem('token')
  const width = useWindowWidth()
  const isMobile = width < 768

  return (
    <nav style={{position:'fixed',top:0,left:0,right:0,zIndex:50,background:'white',borderBottom:'1px solid #f0f0f0',height:64,display:'flex',alignItems:'center',justifyContent:'space-between',padding: isMobile ? '0 16px' : '0 40px'}}>
      <Link to="/" style={{display:'flex',alignItems:'center',gap:8,textDecoration:'none'}}>
        <img src="/logo.svg" width="32" height="32" style={{borderRadius:8}} alt="SkillSync"/>
        <span style={{fontWeight:600,color:'#111',fontSize:isMobile ? 13 : 15}}>SkillSync <span style={{color:'#185FA5'}}>AI</span></span>
      </Link>
      <div style={{display:'flex',alignItems:'center',gap: isMobile ? 10 : 16}}>
        {token && !isMobile && (
          <button onClick={() => navigate('/dashboard')} style={{fontSize:14,color:'#555',background:'none',border:'none',cursor:'pointer'}}>Dashboard</button>
        )}
        {!token && (
          <Link to="/login" style={{fontSize:14,color:'#555',textDecoration:'none'}}>Log in</Link>
        )}
        <button onClick={() => navigate('/analysis')} style={{background:'#185FA5',color:'white',border:'none',padding:isMobile ? '8px 14px' : '10px 20px',borderRadius:10,fontSize:isMobile ? 12 : 14,fontWeight:500,cursor:'pointer'}}>
          Start Career Analysis
        </button>
      </div>
    </nav>
  )
}