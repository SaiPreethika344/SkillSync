import useWindowWidth from '../hooks/useWindowWidth'

const steps = [
  { num:'01', title:'Upload', desc:'Upload your resume PDF or manually select your skills from our library.' },
  { num:'02', title:'Analyze', desc:'Our AI processes your profile and maps it against thousands of career paths.' },
  { num:'03', title:'Discover', desc:'Get your personalized career match report with a learning roadmap.' },
]
export default function HowItWorks() {
  const width = useWindowWidth()
  const isMobile = width < 768

  return (
    <section id="how-it-works" style={{padding: isMobile ? '40px 16px' : '80px 24px',background:'white'}}>
      <div style={{maxWidth:1000,margin:'0 auto'}}>
        <h2 style={{fontSize:34,fontWeight:700,textAlign:'center',color:'#111',marginBottom:12}}>How it works</h2>
        <p style={{textAlign:'center',color:'#666',marginBottom:isMobile ? 40 : 60,fontSize:16}}>Three steps to finding your ideal career path.</p>
        <div style={{display:'grid',gridTemplateColumns:isMobile ? '1fr' : 'repeat(auto-fit,minmax(250px,1fr))',gap:40}}>
          {steps.map(s => (
            <div key={s.num} style={{textAlign:'center'}}>
              <div style={{width:56,height:56,background:'#185FA5',borderRadius:16,display:'flex',alignItems:'center',justifyContent:'center',margin:'0 auto 20px'}}>
                <span style={{color:'white',fontWeight:700,fontSize:18}}>{s.num}</span>
              </div>
              <h3 style={{fontWeight:600,color:'#111',fontSize:22,marginBottom:8}}>{s.title}</h3>
              <p style={{color:'#666',fontSize:14,lineHeight:1.6}}>{s.desc}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  )
}