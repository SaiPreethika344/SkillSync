import { useNavigate } from 'react-router-dom'
import useWindowWidth from '../hooks/useWindowWidth'
export default function HeroSection() {
  const navigate = useNavigate()
  const width = useWindowWidth()
  const isMobile = width < 768
  const handleSmoothScroll = () => {
    const section = document.getElementById('how-it-works')
    if (!section) return
    const offset = 74
    const top = section.getBoundingClientRect().top + window.scrollY - offset
    window.scrollTo({ top, behavior: 'smooth' })
  }

  return (
    <section style={{paddingTop: isMobile ? 80 : 120, paddingBottom: isMobile ? 40 : 80, textAlign:'center', background:'white', padding: isMobile ? '80px 16px 40px' : '120px 24px 80px'}}>
      <div style={{maxWidth:700,margin:'0 auto'}}>
        <span style={{color:'#185FA5',fontSize:13,fontWeight:500,letterSpacing:2,textTransform:'uppercase',display:'block',marginBottom:16}}>AI Career Intelligence</span>
        <h1 style={{fontSize:isMobile ? 28 : 52,fontWeight:700,color:'#111',lineHeight:1.15,marginBottom:20}}>
          Discover your ideal <span style={{color:'#185FA5'}}>Career Path</span>
        </h1>
        <p style={{fontSize:isMobile ? 14 : 20,color:'#666',marginBottom:40,lineHeight:1.6}}>
          Upload your resume or select your skills and let AI recommend the best career opportunities based on your strengths.
        </p>
        <div style={{display:'flex',gap:16,justifyContent:'center',flexWrap:'wrap',flexDirection: isMobile ? 'column' : 'row'}}>
          <button onClick={() => navigate('/analysis')} style={{background:'#185FA5',color:'white',border:'none',padding:isMobile ? '14px 16px' : '14px 32px',borderRadius:12,fontSize:16,fontWeight:500,cursor:'pointer',width:isMobile ? '100%' : 'auto'}}>
            Start Career Analysis
          </button>
          <button onClick={handleSmoothScroll} style={{background:'white',color:'#333',border:'1px solid #ddd',padding:isMobile ? '14px 16px' : '14px 32px',borderRadius:12,fontSize:16,cursor:'pointer',width:isMobile ? '100%' : 'auto'}}>
            See how it works
          </button>
        </div>
        <p style={{fontSize:13,color:'#999',marginTop:20}}>No sign up needed to start · Free forever</p>
      </div>
    </section>
  )
}