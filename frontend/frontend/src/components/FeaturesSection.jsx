import { FileText, BarChart2, Briefcase } from 'lucide-react'
import useWindowWidth from '../hooks/useWindowWidth'
const features = [
  { icon: FileText, title: 'Resume intelligence', desc: 'Upload your resume and our AI extracts every skill, experience and strength automatically.' },
  { icon: BarChart2, title: 'Skill gap analysis', desc: 'See exactly which skills you have, which you are missing, and how close you are to your target role.' },
  { icon: Briefcase, title: 'Live job matches', desc: 'Get matched to real job listings with salary data pulled live from the job market.' },
]
export default function FeaturesSection() {
  const width = useWindowWidth()
  const isMobile = width < 768

  return (
    <section style={{padding: isMobile ? '40px 16px' : '80px 24px',background:'#f9f9f9'}}>
      <div style={{maxWidth:1000,margin:'0 auto'}}>
        <h2 style={{fontSize:34,fontWeight:700,textAlign:'center',color:'#111',marginBottom:12}}>AI Powered Features</h2>
        <p style={{textAlign:'center',color:'#666',marginBottom: isMobile ? 40 : 60,fontSize:16}}>Everything you need to navigate your career with confidence.</p>
        <div style={{display:'grid',gridTemplateColumns: isMobile ? '1fr' : 'repeat(auto-fit,minmax(280px,1fr))',gap:32}}>
          {features.map(f => (
            <div key={f.title} style={{background:'white',borderRadius:20,padding:36,border:'1px solid #eee'}}>
              <div style={{width:48,height:48,background:'#E6F1FB',borderRadius:12,display:'flex',alignItems:'center',justifyContent:'center',marginBottom:20}}>
                <f.icon size={22} color="#185FA5" />
              </div>
              <h3 style={{fontWeight:600,color:'#111',fontSize:18,marginBottom:8}}>{f.title}</h3>
              <p style={{color:'#666',fontSize:14,lineHeight:1.6}}>{f.desc}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  )
}