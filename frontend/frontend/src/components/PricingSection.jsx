import { useNavigate } from 'react-router-dom'
const plans = [
  { name:'Free', price:'₹0', period:'forever', features:['3 career analyses','Basic skill gap report','Top 3 job matches','Learning roadmap'], cta:'Get started free', highlight:false },
  { name:'Pro', price:'₹299', period:'per month', features:['Unlimited analyses','Full skill gap report','All job matches with salary','Priority AI processing','Shareable profile link'], cta:'Start Pro', highlight:true },
]
export default function PricingSection() {
  const navigate = useNavigate()
  return (
    <section style={{padding:'80px 24px',background:'white'}}>
      <div style={{maxWidth:800,margin:'0 auto'}}>
        <h2 style={{fontSize:34,fontWeight:700,textAlign:'center',color:'#111',marginBottom:12}}>Simple pricing</h2>
        <p style={{textAlign:'center',color:'#666',marginBottom:60,fontSize:16}}>Start free. Upgrade when you need more.</p>
        <div style={{display:'grid',gridTemplateColumns:'repeat(auto-fit,minmax(300px,1fr))',gap:32}}>
          {plans.map(p => (
            <div key={p.name} style={{borderRadius:20,padding:36,border: p.highlight ? '2px solid #185FA5' : '1px solid #eee'}}>
              {p.highlight && <span style={{display:'inline-block',background:'#E6F1FB',color:'#185FA5',fontSize:12,fontWeight:500,padding:'4px 12px',borderRadius:20,marginBottom:16}}>Most popular</span>}
              <h3 style={{fontSize:22,fontWeight:700,color:'#111',marginBottom:4}}>{p.name}</h3>
              <div style={{display:'flex',alignItems:'baseline',gap:4,marginBottom:24}}>
                <span style={{fontSize:40,fontWeight:700,color:'#111'}}>{p.price}</span>
                <span style={{color:'#999',fontSize:14}}>/{p.period}</span>
              </div>
              <ul style={{listStyle:'none',marginBottom:32,display:'flex',flexDirection:'column',gap:12}}>
                {p.features.map(f => (
                  <li key={f} style={{display:'flex',alignItems:'center',gap:10,fontSize:14,color:'#555'}}>
                    <span style={{color:'#185FA5',fontWeight:700}}>✓</span>{f}
                  </li>
                ))}
              </ul>
              <button onClick={() => navigate('/analysis')} style={{width:'100%',padding:'14px',borderRadius:12,fontSize:14,fontWeight:500,cursor:'pointer',background: p.highlight ? '#185FA5' : 'white',color: p.highlight ? 'white' : '#333',border: p.highlight ? 'none' : '1px solid #ddd'}}>
                {p.cta}
              </button>
            </div>
          ))}
        </div>
      </div>
    </section>
  )
}