import { useNavigate } from 'react-router-dom'
import useWindowWidth from '../hooks/useWindowWidth'
export default function CTASection() {
  const navigate = useNavigate()
  const width = useWindowWidth()
  const isMobile = width < 768
  return (
    <section style={{padding:isMobile ? '40px 16px' : '80px 24px',background:'#185FA5',textAlign:'center'}}>
      <div style={{maxWidth:700,margin:'0 auto'}}>
        <h2 style={{fontSize:34,fontWeight:700,color:'white',marginBottom:16}}>Ready to discover your ideal career?</h2>
        <p style={{color:'#B3D4F0',fontSize:18,marginBottom:isMobile ? 24 : 32}}>Join thousands of students and professionals who found clarity with SkillSync AI.</p>
        <button onClick={() => navigate('/analysis')} style={{background:'white',color:'#185FA5',border:'none',padding:isMobile ? '16px 18px' : '16px 36px',borderRadius:12,fontSize:16,fontWeight:600,cursor:'pointer',width:isMobile ? '100%' : 'auto'}}>
          Start for free — no sign up needed
        </button>
      </div>
    </section>
  )
}